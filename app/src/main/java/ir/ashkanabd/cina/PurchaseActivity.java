package ir.ashkanabd.cina;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.poliveira.parallaxrecyclerview.ParallaxRecyclerAdapter;
import com.rey.material.widget.RelativeLayout;
import es.dmoral.toasty.Toasty;
import ir.ashkanabd.cina.backgroundTasks.ActivityTask;
import ir.ashkanabd.cina.database.Connection;
import ir.ashkanabd.cina.database.UserData;
import ir.ashkanabd.cina.util.*;
import ir.ashkanabd.cina.view.PurchaseViewHolder;

import java.io.ObjectInputStream;
import java.util.*;

public class PurchaseActivity extends AppCompatActivity {

    private MaterialDialog loadingDialog;
    private SharedPreferences appearancePreferences;
    private IabHelper iabHelper;
    private static String publicKey = "";
    private List<String> SKUList;
    private IabResult setupResult;
    private RecyclerView recyclerView;
    private ParallaxRecyclerAdapter<SkuDetails> adapter;
    private List<SkuDetails> itemsList;
    private ActivityTask activityTask;
    private Connection connection;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setAppAppearance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        setupLoadingProgress();
        activityTask = new ActivityTask(loadingDialog) {
            @Override
            protected void onPostExecute(Void aVoid) {
                onPostTask.onPost(aVoid);
            }
        };
        activityTask.setOnTaskStarted(o -> task());
        activityTask.setOnPostTask(o -> postTask());
        activityTask.execute();
    }

    private void findView() {
        recyclerView = findViewById(R.id.recycle_view_purchase);
    }

    private void getConnection() {
        connection = (Connection) getIntent().getExtras().get("connection");
    }

    private void setupRecycleView() {
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(divider);
        adapter = new ParallaxRecyclerAdapter<SkuDetails>(itemsList) {
            @Override
            public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, ParallaxRecyclerAdapter<SkuDetails> parallaxRecyclerAdapter, int i) {
                SkuDetails details = itemsList.get(i);
                ((PurchaseViewHolder) viewHolder).getItemName().setText(details.getTitle());
                ((PurchaseViewHolder) viewHolder).getItemPrice().setText(details.getPrice());
                ((PurchaseViewHolder) viewHolder).getItemButton().setTag(details);
                ((PurchaseViewHolder) viewHolder).getItemButton().setOnClickListener(PurchaseActivity.this::onButtonClick);
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup viewGroup, ParallaxRecyclerAdapter<SkuDetails> parallaxRecyclerAdapter, int i) {
                LayoutInflater inflater = (LayoutInflater) PurchaseActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                RelativeLayout fileLayout = (RelativeLayout) inflater.inflate(R.layout.purchase_item, null);
                return new PurchaseViewHolder(fileLayout);
            }

            @Override
            public int getItemCountImpl(ParallaxRecyclerAdapter<SkuDetails> parallaxRecyclerAdapter) {
                return itemsList.size();
            }
        };
        adapter.setParallaxHeader(new View(this), recyclerView);
        recyclerView.setAdapter(adapter);
    }

    private void onButtonClick(View view) {
        if (connection.isValid()) {
            Toasty.info(this, this.getString(R.string.active_account), Toasty.LENGTH_LONG, true).show();
            return;
        }
        SkuDetails details = (SkuDetails) view.getTag();
        iabHelper.launchPurchaseFlow(this, details.getSku(), 1010, this::activePurchase, connection.getUserData().toString());
    }

    private boolean updateData(Purchase purchase) {
        String time;
        if (purchase.getSku().equals("oneMonth")) {
            time = Integer.toString(30);
        } else if (purchase.getSku().equals("threeMonth")) {
            time = Integer.toString(30 * 3);
        } else if (purchase.getSku().equals("sixMonth")) {
            time = Integer.toString(30 * 6);
        } else {
            time = Integer.toString(30 * 12);
        }
        UserData userData = new UserData(UserData.getDeviceID(this), UserData.getString(Calendar.getInstance().getTime()), time);
        userData = UserData.encryptData(userData);
        if (connection.updateDataBase(this, userData)) {
            iabHelper.consumeAsync(purchase, null);
            return true;
        }
        return false;
    }

    public void activePurchase(IabResult result, Purchase info) {
        if (result.isFailure()) {
            Toasty.error(this, this.getString(R.string.purchase_error), Toasty.LENGTH_SHORT, true).show();
        } else {
            ActivityTask activityTask = new ActivityTask(loadingDialog);
            activityTask.setOnTaskStarted(o -> updateData(info));
            activityTask.setOnPostTask(o -> {
                boolean b = (boolean) o;
                if (b) {
                    Toasty.success(this, this.getString(R.string.activating_account), Toasty.LENGTH_LONG, true).show();
                } else {
                    Toasty.error(this, this.getString(R.string.activating_error), Toasty.LENGTH_LONG, true).show();
                }
            });
            activityTask.setOnBeforeTask(() -> Toasty.info(this, this.getString(R.string.wait_activating)
                    , Toasty.LENGTH_SHORT, true).show());
            activityTask.execute();
        }
    }


    private Object task() {
        publicKey = readPublicKey(this);
        SKUList = Arrays.asList("oneMonth", "threeMonth", "sixMonth", "oneYear");
        iabHelper = new IabHelper(this, publicKey);
        iabHelper.enableDebugLogging(true);
        iabHelper.startSetup(this::IabSetupFinish);
        try {
            while (true) {
                if (setupResult != null)
                    break;
                Thread.sleep(100);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void postTask() {
        if (setupResult.isFailure()) {
            Toasty.error(this, this.getString(R.string.need_bazaar), Toasty.LENGTH_SHORT, true).show();
            return;
        }
        iabHelper.queryInventoryAsync(true, SKUList, this::queryResult);
    }

    public void queryResult(IabResult result, Inventory inv) {
        if (result.isFailure()) {
            finish();
            return;
        }
        itemsList = new ArrayList<>();
        for (int i = 0; i < SKUList.size(); i++) {
            if (inv.hasDetails(SKUList.get(i)))
                itemsList.add(inv.getSkuDetails(SKUList.get(i)));
        }
        findView();
        setupRecycleView();
        getConnection();
        loadingDialog.dismiss();
    }

    private void IabSetupFinish(IabResult result) {
        setupResult = result;
    }

    private static String readPublicKey(Context context) {
        try {
            ObjectInputStream ois = new ObjectInputStream(context.getAssets().open("bk"));
            Map<Integer, Character> map = (HashMap<Integer, Character>) ois.readObject();
            byte[] buf = new byte[map.size()];
            for (Map.Entry<Integer, Character> e : map.entrySet()) {
                buf[e.getKey()] = (byte) ((char) e.getValue());
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * Get app appearance from shared preferences and set it.
     */
    protected void setAppAppearance() {
        appearancePreferences = getSharedPreferences("appearance", MODE_PRIVATE);
        String lang = appearancePreferences.getString("lang", "EN");
        String theme = appearancePreferences.getString("theme", "light");
        boolean isDarkTheme = Objects.requireNonNull(theme).equalsIgnoreCase("dark");
        if (isDarkTheme)
            setTheme(R.style.StartActivityThemeDark);
    }

    /*
     * Setup a loading progress dialog
     */
    private void setupLoadingProgress() {
        loadingDialog = new MaterialDialog(this);
        loadingDialog.setContentView(R.layout.loading_progress);
        loadingDialog.cancelable(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (iabHelper != null)
                iabHelper.dispose();
            iabHelper = null;
        } catch (Exception ignored) {
        }
    }

}
