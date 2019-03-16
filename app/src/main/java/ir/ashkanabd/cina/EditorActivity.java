package ir.ashkanabd.cina;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;
import androidx.annotation.NonNull;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import ir.ashkanabd.cina.view.CodeEditor;

public class EditorActivity extends Activity {
    private CodeEditor editor;
    private Toolbar projectToolbar;
    private ActionBar projectActionBar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private boolean drawerIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        findViews();
        setupActionBar();
        setupNavigationView();
    }

    /*
     * Find views by there IDs
     */
    private void findViews() {
        projectToolbar = findViewById(R.id.project_toolbar);
        editor = findViewById(R.id.code_editor);
        navigationView = findViewById(R.id.project_nav_view);
        drawerLayout = findViewById(R.id.project_drawer_layout);
    }

    /*
     * setup ActionBar
     */
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setActionBar(projectToolbar);
            projectActionBar = getActionBar();
            // TODO: 3/16/19 Add file name in actionbar title
            projectActionBar.setTitle("Hello world");
            projectActionBar.setDisplayHomeAsUpEnabled(true);
            projectActionBar.setHomeAsUpIndicator(R.drawable.action_bar_menu);
        }
    }

    /*
     * setup navigation vir=ew and drawer layout
     */
    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawers();
            return true;
        });
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                drawerIsOpen = true;
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                drawerIsOpen = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e("INFO", item.toString());
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(Gravity.LEFT);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        if (drawerIsOpen) {
            drawerLayout.closeDrawers();
        }
    }
}
