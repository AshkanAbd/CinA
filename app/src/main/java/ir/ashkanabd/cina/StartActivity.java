package ir.ashkanabd.cina;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import co.dift.ui.SwipeToAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;
import com.rey.material.widget.EditText;
import com.rey.material.widget.TextView;
import es.dmoral.toasty.Toasty;
import ir.ashkanabd.cina.backgroundTasks.ActivityTask;
import ir.ashkanabd.cina.compileAndRun.GccCompiler;
import ir.ashkanabd.cina.database.Connection;
import ir.ashkanabd.cina.database.UserData;
import ir.ashkanabd.cina.project.Project;
import ir.ashkanabd.cina.project.ProjectAdapter;
import ir.ashkanabd.cina.project.ProjectManager;
import ir.ashkanabd.cina.view.ActionBarDrawerToggleCompat;
import ir.ashkanabd.cina.view.filebrowser.FileBrowserDialog;
import ir.ashkanabd.cina.view.filebrowser.AppCompatActivityFileBrowserSupport;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.provider.ContactsContract.Directory.PACKAGE_NAME;

public class StartActivity extends AppCompatActivityFileBrowserSupport {

    public static Context resourcesContext;
    public static int CHANGE_THEME_REQUEST = 10;

    private List<Project> projectList;
    private ActionBarDrawerToggleCompat drawerToggle;
    private DrawerLayout drawerLayout;
    private ActionBar actionBar;
    private NavigationView navigationView;
    private EditText newProjectName, newProjectDescription;
    private MaterialRadioButton cRadioBtn, cppRadioBtn;
    private MaterialButton newProjectButton;
    private boolean backPress = false;
    private boolean drawerOpen = false;
    private ProjectManager projectManager;
    private MaterialDialog newProjectDialog;
    private MaterialDialog deleteProjectDialog;
    private SwipeToAction swipeToAction;
    private RecyclerView recyclerView;
    private ProjectAdapter adapter;
    private SwipeRefreshLayout mainLayout;
    private FileBrowserDialog fileBrowserDialog;
    private TextView titleDeleteProject;
    private Project deletingProject = null;
    private ActivityTask activityStartTask;
    private boolean validProjectName = false;
    private Connection connection;
    private TypedValue primaryColorTypedValue;
    private TypedValue errorColorTypedValue;
    private TextView startDrawerAccountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resourcesContext = this;
        if (isDarkTheme) {
            setTheme(R.style.StartActivityThemeDark);
        }
        setContentView(R.layout.splash_layout);
        getSupportActionBar().hide();
        TypefaceProvider.registerDefaultIconSets();
        this.projectManager = new ProjectManager();
        this.projectList = new ArrayList<>();
        activityStartTask = new ActivityTask(null);
        activityStartTask.setOnTaskStarted(o -> onActivityStart());
        activityStartTask.setOnPostTask(o -> postActivityTask());
        activityStartTask.execute();
    }

    private void postActivityTask() {
        setContentView(R.layout.start_activity);
        getSupportActionBar().show();
        findViews();
        setupNavigationView();
        setupNewProjectDialog();
        setupActionBar();
        setupListView();
        setupBrowseProjectDialog();
        setupDeleteProjectDialog();
        if (connection.getNeedNetwork()) {
            Toasty.error(this, getString(R.string.no_user_login), Toasty.LENGTH_LONG, true).show();
        } else if (!connection.isValid()) {
            Toasty.warning(this, this.getString(R.string.invalid_user), Toasty.LENGTH_LONG, true).show();
        }
        updateAccountView();
    }

    private void updateAccountView() {
        if (connection.getUserData() == null) {
            startDrawerAccountView.setText(this.getString(R.string.unknown_account));
            return;
        }
        Date expireDate = connection.getExpireTime(connection.getUserData());
        String msg = StartActivity.this.getString(R.string.expite_at) + UserData.getString(expireDate);
        startDrawerAccountView.setText(msg);
    }

    /*
     * Background tasks on starting activity
     */
    private Object onActivityStart() {
        if (!checkStoragePermission()) {
            requestStoragePermission();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            while (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ;
        }
        checkCompiler();
        loadProjects();
        connection = new Connection();
        connection.connectDatabase(this);
        return null;
    }


    @Override
    protected void onResume() {
        super.onResume();
//        try {
//            loadProjects();
//            changeListView();
//        } catch (Exception ignored) {
//        }
    }

    /*
     * Setup file browse dialog for searching and opening projects
     */
    private void setupBrowseProjectDialog() {
        this.fileBrowserDialog = new FileBrowserDialog(this, null,
                Environment.getExternalStorageDirectory().getAbsolutePath(), "cina") {
            @Override
            protected void setupDialogViewListeners(RelativeLayout view) {
                super.setupDialogViewListeners(view);
                LinearLayoutCompat linearLayout = view.findViewById(R.id.buttons_layout_browse_file_layout);
                BootstrapButton openButton = linearLayout.findViewById(R.id.open_browse_file_layout);
                BootstrapButton createFileButton = linearLayout.findViewById(R.id.new_file_browser_file_layout);
                BootstrapButton createDirButton = linearLayout.findViewById(R.id.new_folder_browse_file_layout);
                BootstrapButton deleteButton = linearLayout.findViewById(R.id.delete_file_browser_file_layout);
                createDirButton.setVisibility(View.INVISIBLE);
                createFileButton.setVisibility(View.INVISIBLE);
                deleteButton.setVisibility(View.INVISIBLE);
                openButton.setOnClickListener(v -> onProjectOpenListener());
            }
        };
    }

    /*
     * When a project selected to open
     */
    private void onProjectOpenListener() {
        try {
            File file = fileBrowserDialog.getFile(fileBrowserDialog.getListeners().getPreClickedView());
            if (file.isFile()) {
                Project project = new Project(ProjectManager.readFile(file));
                this.projectList.add(project);
                setupListView();
            }
        } catch (FileNotFoundException ignored) {
            /*
             * Ignore: a true will be selected...
             */
        } catch (JSONException e) {
            Toasty.error(this, this.getString(R.string.invalid_project_file_selecting), Toasty.LENGTH_SHORT, true).show();
        }
        fileBrowserDialog.getBrowserDialog().dismiss();
    }

    /*
     * Setup GCC
     */
    private void checkCompiler() {
        try {
            new GccCompiler(this);
        } catch (Exception e) {
            Toasty.error(this, this.getString(R.string.compiler_setup_exception), Toasty.LENGTH_SHORT).show();
        }
    }

    /*
     * Setup dialog for deleting project when swipe to right
     */
    private void setupDeleteProjectDialog() {
        deleteProjectDialog = new MaterialDialog(this);
        deleteProjectDialog.setContentView(R.layout.delete_file_layout);
        titleDeleteProject = deleteProjectDialog.findViewById(R.id.title_delete_file_layout);
        TextView deleteButton = deleteProjectDialog.findViewById(R.id.delete_delete_file_layout);
        TextView cancelButton = deleteProjectDialog.findViewById(R.id.cancel_delete_file_layout);
        deleteButton.setOnClickListener(view -> {
            if (deletingProject == null) return;
            ProjectManager.removeProject(deletingProject);
            loadProjects();
            setupListView();
            deletingProject = null;
            deleteProjectDialog.dismiss();
        });
        cancelButton.setOnClickListener(view -> deleteProjectDialog.dismiss());
    }

    /*
     * Update projects list in UI thread
     */
    private void changeListView() {
        adapter = new ProjectAdapter(this.projectList);
        recyclerView.setAdapter(adapter);
    }

    /*
     * Create and load ListView
     */
    private void setupListView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.addItemDecoration(divider);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new ProjectAdapter(this.projectList);
        recyclerView.setAdapter(adapter);
        swipeToAction = new SwipeToAction(recyclerView, new SwipeToAction.SwipeListener<Project>() {
            @Override
            public boolean swipeLeft(Project itemData) {
                /*
                 * Remove Item
                 */
                String msg = StartActivity.this.getString(R.string.remove_project) + itemData.getName() + "?";
                titleDeleteProject.setText(msg);
                deletingProject = itemData;
                deleteProjectDialog.show();
                return true;
            }

            @Override
            public boolean swipeRight(Project itemData) {
                return true;
            }

            @Override
            public void onClick(Project itemData) {
                Intent projectActivity = new Intent(StartActivity.this, EditorActivity.class);
                projectActivity.putExtra("project", itemData);
                projectActivity.putExtra("connection", connection);
                StartActivity.this.startActivity(projectActivity);
            }

            @Override
            public void onLongClick(Project itemData) {

            }
        });
    }

    /*
     * Load new project material dialog
     */
    private void setupNewProjectDialog() {
        newProjectDialog = new MaterialDialog(this);
        newProjectDialog.setContentView(R.layout.create_new_project);
        newProjectDialog.setCancelable(true);
        this.newProjectName = newProjectDialog.findViewById(R.id.create_new_project_name);
        this.newProjectDescription = newProjectDialog.findViewById(R.id.create_new_project_description);
        this.cRadioBtn = newProjectDialog.findViewById(R.id.create_new_project_c_radio);
        this.cppRadioBtn = newProjectDialog.findViewById(R.id.create_new_project_cpp_radio);
        this.newProjectButton = newProjectDialog.findViewById(R.id.create_new_project_button);
        this.newProjectButton.setOnClickListener(this::createNewProject);
        primaryColorTypedValue = new TypedValue();
        errorColorTypedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.primaryColor, primaryColorTypedValue, true);
        getTheme().resolveAttribute(R.attr.errorColor, errorColorTypedValue, true);
        this.newProjectName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            /*
             * Check for project name
             */
            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.toString();
                Pattern pattern = Pattern.compile("[a-zA-Z0-9_+-.]+");
                Matcher matcher = pattern.matcher(str);
                if (!matcher.matches()) {
                    validProjectName = false;
                    newProjectName.setError(StartActivity.this.getString(R.string.invalid_project_name));
                } else {
                    validProjectName = true;
                    newProjectName.clearError();
                }
                String charCount = str.length() + " / " + 20;
                SpannableString span = new SpannableString(charCount);
                if (str.length() > 20) {
                    span.setSpan(new ForegroundColorSpan(errorColorTypedValue.data), 0,
                            charCount.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    newProjectName.setHelper(span);
                } else {
                    span.setSpan(new ForegroundColorSpan(primaryColorTypedValue.data), 0,
                            charCount.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    newProjectName.setHelper(span);
                }
            }
        });
    }

    /*
     * Load projects from workspace
     */
    private void loadProjects() {
        projectList.clear();
        try {
            this.projectManager.readPreviousProjects(projectList);
        } catch (IOException e) {
            Toasty.error(this, getString(R.string.project_error) + getString(R.string.permission_error2)
                    , Toasty.LENGTH_SHORT, true).show();
        } catch (JSONException e) {
            Toasty.error(this, getString(R.string.project_error) + getString(R.string.project_structure_error)
                    , Toasty.LENGTH_SHORT, true).show();
        }
    }

    /*
     * Find in XML views from there IDs
     */
    private void findViews() {
        this.drawerLayout = this.findViewById(R.id.drawer_layout);
        this.navigationView = this.findViewById(R.id.nav_view);
        this.startDrawerAccountView = navigationView.getHeaderView(0).findViewById(R.id.account_info_start_drawer);
        this.recyclerView = this.findViewById(R.id.projects_recycler_view);
        this.mainLayout = this.findViewById(R.id.main_layout_start_activity);
        mainLayout.setColorSchemeColors(Color.BLUE, Color.RED);
        this.mainLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            ActivityTask st = new ActivityTask(null);
            st.setOnTaskStarted(o -> {
                loadProjects();
                connection = new Connection();
                connection.connectDatabase(this);
                return null;
            });
            st.setOnPostTask(c -> {
                changeListView();
                mainLayout.setRefreshing(false);
                if (connection.getNeedNetwork()) {
                    Toasty.error(this, getString(R.string.no_user_login), Toasty.LENGTH_LONG, true).show();
                }
                updateAccountView();
            });
            st.execute();
        }, 1000));

    }

    /*
     * Setup custom Action Bar and it callback
     */
    private void setupActionBar() {
        this.actionBar = getSupportActionBar();
        this.actionBar.setTitle(this.getString(R.string.start_activity_title));
        this.actionBar.setDisplayHomeAsUpEnabled(true);
        this.actionBar.setHomeButtonEnabled(true);
    }

    /*
     * Setup Setup Navigation item selection
     */
    private boolean itemSelectedListener(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.start_nav_open) {
            fileBrowserDialog.getBrowserDialog().show();
        }
        if (menuItem.getItemId() == R.id.start_nav_setting) {
            startActivityForResult(new Intent(this, SettingActivity.class), CHANGE_THEME_REQUEST);
        }
        if (menuItem.getItemId() == R.id.start_nav_purchase) {
            if (connection.getNeedNetwork()) {
                Toasty.error(this, this.getString(R.string.no_user_login), Toasty.LENGTH_LONG, true).show();
                return true;
            }
            Intent projectActivity = new Intent(this, PurchaseActivity.class);
            projectActivity.putExtra("connection", connection);
            startActivity(projectActivity);
        }
        if (menuItem.getItemId() == R.id.start_nav_about) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("bazaar://collection?slug=by_author&aid=569432754687"));
            intent.setPackage("com.farsitel.bazaar");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toasty.warning(this, this.getString(R.string.need_bazaar), Toasty.LENGTH_SHORT, true).show();
            }
        }
        if (menuItem.getItemId() == R.id.start_nav_rate) {
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setData(Uri.parse("bazaar://details?id=" + PACKAGE_NAME));
            intent.setPackage("com.farsitel.bazaar");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toasty.warning(this, this.getString(R.string.need_bazaar), Toasty.LENGTH_SHORT, true).show();
            }
        }
        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_THEME_REQUEST) {
            if (resultCode == 1) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }

    /*
     * Setup Navigation view Interfaces
     */
    private void setupNavigationView() {
        this.navigationView.setNavigationItemSelectedListener(this::itemSelectedListener);

        DrawerArrowDrawable drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }

            @Override
            public void setColor(int resourceId) {
                this.mPaint.setColor(StartActivity.this.getResources().getColor(R.color.primaryTextColor));
            }
        };
        drawerArrow.setColor(0);
        drawerToggle = new ActionBarDrawerToggleCompat(this, drawerLayout, drawerArrow, R.drawable.back_icon, R.drawable.action_bar_menu) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();

            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                StartActivity.this.backPress = false;
                StartActivity.this.drawerOpen = true;
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                StartActivity.this.drawerOpen = false;
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });
    }

    /*
     * Show create project dialog
     */
    public void addProject(View view) {
        this.newProjectName.setText("");
        this.newProjectDescription.setText("");
        this.cRadioBtn.setChecked(true);
        this.cppRadioBtn.setChecked(false);
        this.newProjectDialog.show();
    }

    /*
     * Create new Project after checking dialog
     */
    public void createNewProject(View view) {
        String projectName = newProjectName.getText().toString();
        String projectDescription = newProjectDescription.getText().toString();
        boolean isC = cRadioBtn.isChecked();
        if (projectName.length() > 20 || projectName.trim().isEmpty() || !validProjectName) {
            return;
        }
        try {
            Project project = this.projectManager.createNewProject(projectName, projectDescription, isC);
            if (project == null) {
                return;
            }
            if (this.projectList.contains(project)) {
                return;
            }
            File cinaFile = project.getProjectFile();
            boolean tmp = cinaFile.createNewFile();
            if (!tmp)
                throw new IOException();
            ProjectManager.writeFile(project.toJson().toString(), cinaFile);
        } catch (IOException e) {
            Toasty.error(this, this.getString(R.string.project_create_error), Toasty.LENGTH_SHORT, true).show();
        }
        this.newProjectDialog.cancel();
        loadProjects();
        setupListView();
    }

    /*
     * Storage read and write permission check
     */
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerOpen) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (this.drawerOpen) {
            this.drawerLayout.closeDrawers();
            return;
        }
        if (this.backPress) {
            finish();
        } else {
            this.backPress = true;
            Toasty.warning(this, this.getString(R.string.back_for_exit), Toast.LENGTH_LONG).show();
            new Handler().postDelayed(() -> StartActivity.this.backPress = false, 2000);
        }
    }
}
