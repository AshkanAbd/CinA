package ir.ashkanabd.cina;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.Toolbar;
import androidx.annotation.NonNull;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.widget.Toast;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import ir.ashkanabd.cina.project.Project;
import ir.ashkanabd.cina.project.ProjectFile;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StartActivity extends Activity {
    private List<Project> projectList;
    private FloatingActionButton floatingActionButton;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private NavigationView navigationView;
    private TextInputEditText newProjectName;
    private MaterialRadioButton cRadioBtn, cppRadioBtn;
    private boolean backPress = false;
    private boolean drawerOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        if (!checkStoragePermission()) {
            requestStoragePermission();
        }
        findViews();
        setupActionBar();
        setupNavigationView();
        readPreviousProjects();
        Log.e("INFO", projectList.toString());
    }

    /*
     * Find in XML views from there IDs
     */
    private void findViews() {
        this.floatingActionButton = this.findViewById(R.id.new_project_float_btn);
        this.drawerLayout = this.findViewById(R.id.drawer_layout);
        this.navigationView = this.findViewById(R.id.nav_view);
        this.toolbar = this.findViewById(R.id.toolbar);
        this.newProjectName = this.findViewById(R.id.create_new_project_name);
        this.cRadioBtn = this.findViewById(R.id.create_new_project_c_radio);
        this.cppRadioBtn = this.findViewById(R.id.create_new_project_cpp_radio);
    }

    /*
     * Setup custom Action Bar and it callback
     */
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.toolbar.setTitleTextColor(Color.WHITE);
            this.setActionBar(this.toolbar);
            this.actionBar = this.getActionBar();
            this.actionBar.setTitle("C/C++ compiler for Android");
            this.actionBar.setDisplayHomeAsUpEnabled(true);
            this.actionBar.setHomeAsUpIndicator(R.drawable.action_bar_menu);
        }
    }

    /*
     * Setup Navigation view Interfaces
     */
    private void setupNavigationView() {
        this.navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawers();
            return true;
        });
        this.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
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
        MaterialDialog materialDialog = new MaterialDialog(this);
        materialDialog.setContentView(R.layout.create_new_project);
        materialDialog.setCancelable(true);
        materialDialog.show();
    }

    /*
     * Create new Project after checking dialog
     */
    public void createNewProject(View view) {

    }

    /*
     * Read every directory in main workspace and find CinA projects
     */
    private void readPreviousProjects() {
        this.projectList = new ArrayList<>();
        File workspace = new File(Environment.getExternalStorageDirectory() + "/CinAProjects/");
        if (!workspace.exists()) {
            workspace.mkdir();
        }
        File likeProjects[] = workspace.listFiles();
        for (File likeProject : likeProjects) {
            Object[] objs = isProject(likeProject);
            if ((boolean) objs[0]) {
                ProjectFile projectFile = new ProjectFile((File) objs[1]);
                try {
                    this.projectList.add(new Project(projectFile.readFile()));
                } catch (JSONException | IOException ignored) {
                }
            }
        }
    }

    /*
     * Check given File(directory) is project or not
     */
    private Object[] isProject(File likeProject) {
        if (!likeProject.isDirectory())
            return new Object[]{false};
        File[] subFiles = likeProject.listFiles((path, name) -> name.endsWith("cina"));
        if (subFiles.length == 1) {
            return new Object[]{true, subFiles[0]};
        }
        return new Object[]{false};
    }

    /*
     * Storage read and write permission check
     */
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
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
                drawerLayout.openDrawer(Gravity.LEFT);
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
            System.exit(1);
        } else {
            this.backPress = true;
            Toast.makeText(this, "Press back again", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(() -> StartActivity.this.backPress = false, 2000);
        }
    }
}
