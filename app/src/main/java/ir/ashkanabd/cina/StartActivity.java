package ir.ashkanabd.cina;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import co.dift.ui.SwipeToAction;
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
import ir.ashkanabd.cina.compile.CompileGCC;
import ir.ashkanabd.cina.project.Project;
import ir.ashkanabd.cina.project.ProjectAdapter;
import ir.ashkanabd.cina.project.ProjectManager;

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
    private TextInputEditText newProjectName, newProjectDescription;
    private MaterialRadioButton cRadioBtn, cppRadioBtn;
    private boolean backPress = false;
    private boolean drawerOpen = false;
    private ProjectManager projectManager;
    private MaterialDialog newProjectDialog;
    private MaterialDialog loadingDialog;
    private SwipeToAction swipeToAction;
    private RecyclerView recyclerView;
    private ProjectAdapter adapter;
    private CompileGCC gcc;
    private boolean isLoadingDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        setupLoadingProgress();
        changeLoadingProgressStatus();
        if (!checkStoragePermission()) {
            requestStoragePermission();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            while (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ;
        }
        checkCompiler();
        findViews();
        setupActionBar();
        setupNavigationView();
        setupNewProjectDialog();
        this.projectManager = new ProjectManager();
        this.projectList = new ArrayList<>();
        loadProjects();
        setupListView();
        changeLoadingProgressStatus();
        Log.e("INFO", projectList.toString());
    }


    /*
     * Setup a loading progress dialog
     */
    private void setupLoadingProgress() {
        loadingDialog = new MaterialDialog(this);
        loadingDialog.setContentView(R.layout.loading_progress);
        loadingDialog.cancelable(false);
        isLoadingDialog = false;
    }

    /*
     * Change loading progress  status
     * If show => cancel,
     * If cancel => show
     */
    private void changeLoadingProgressStatus() {
        if (isLoadingDialog) {
            isLoadingDialog = false;
            loadingDialog.dismiss();
        } else {
            isLoadingDialog = true;
            loadingDialog.show();
        }
    }

    /*
     * Setup GCC
     */
    private void checkCompiler() {
        try {
            gcc = new CompileGCC(this);
        } catch (IOException e) {
            // TODO: 3/16/19 catch gcc setup exceptions
            Log.e("INFO", "Can't extract compiler");
        }
    }

    /*
     * Create and load ListView
     */
    private void setupListView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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
                new AlertDialog.Builder(StartActivity.this).setTitle("Remove Project?")
                        .setMessage("Are you sure to remove project " + itemData.getName() + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            ProjectManager.removeProject(itemData);
                            loadProjects();
                            setupListView();
                        })
                        .setNegativeButton("No", null).setCancelable(false).show();
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
    }

    /*
     * Load projects from workspace
     */
    private void loadProjects() {
        projectList.clear();
        this.projectManager.readPreviousProjects(projectList);
    }

    /*
     * Find in XML views from there IDs
     */
    private void findViews() {
        this.floatingActionButton = this.findViewById(R.id.new_project_float_btn);
        this.drawerLayout = this.findViewById(R.id.drawer_layout);
        this.navigationView = this.findViewById(R.id.nav_view);
        this.toolbar = this.findViewById(R.id.toolbar);
        this.recyclerView = this.findViewById(R.id.projects_recycler_view);
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
        try {
            Project project = this.projectManager.createNewProject(projectName, projectDescription, isC);
            if (project == null) {
                return;
            }
            File cinaFile = new File(project.getDir(), "." + projectName + ".cina");
            boolean tmp = cinaFile.createNewFile();
            if (!tmp)
                throw new IOException();
            ProjectManager.writeFile(project.toJson().toString(), cinaFile);
        } catch (IOException e) {
            // TODO: 3/15/19 Catch IOException
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
            finish();
        } else {
            this.backPress = true;
            Toast.makeText(this, "Press back again", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(() -> StartActivity.this.backPress = false, 2000);
        }
    }
}
