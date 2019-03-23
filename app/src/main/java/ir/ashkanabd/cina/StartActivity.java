package ir.ashkanabd.cina;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartActivity extends AppCompatActivityFileBrowserSupport {
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
    private MaterialDialog loadingDialog;
    private MaterialDialog deleteProjectDialog;
    private SwipeToAction swipeToAction;
    private RecyclerView recyclerView;
    private ProjectAdapter adapter;
    private boolean isLoadingDialog = false;
    private SwipeRefreshLayout mainLayout;
    private FileBrowserDialog fileBrowserDialog;
    private TextView titleDeleteProject;
    private Project deletingProject = null;
    private ActivityTask activityStartTask;
    private boolean validProjectName = false;
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        setupLoadingProgress();
        setupActionBar();
        changeLoadingProgressStatus();
        TypefaceProvider.registerDefaultIconSets();
        activityStartTask = new ActivityTask(loadingDialog);
        activityStartTask.setOnTaskStarted(this::onActivityStart);
        activityStartTask.setOnPostTask(this::changeListView);
        activityStartTask.setOnBeforeTask(this::preActivityStart);
        new Handler().postDelayed(activityStartTask::execute, 2000);
    }

    /*
     * Tasks on starting activity before background task
     */
    private void preActivityStart() {
        findViews();
        setupNavigationView();
        setupNewProjectDialog();
        this.projectManager = new ProjectManager();
        this.projectList = new ArrayList<>();
        setupListView();
        setupBrowseProjectDialog();
        setupDeleteProjectDialog();
    }

    /*
     * Background tasks on starting activity
     */
    private Object onActivityStart(Object... o) {
        if (!checkStoragePermission()) {
            requestStoragePermission();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            while (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ;
        }
        checkCompiler();
        loadProjects();
        connection = new Connection(this);
        connection.connectDatabase();
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadProjects();
            changeListView(null);
        } catch (Exception ignored) {
        }
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
                openButton.setOnClickListener(StartActivity.this::onProjectOpenListener);
            }
        };
    }

    /*
     * When a project selected to open
     */
    private void onProjectOpenListener(View view) {
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
        BootstrapButton deleteButton = deleteProjectDialog.findViewById(R.id.delete_delete_file_layout);
        BootstrapButton cancelButton = deleteProjectDialog.findViewById(R.id.cancel_delete_file_layout);
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
    private void changeListView(Object o) {
        adapter = new ProjectAdapter(this.projectList);
        recyclerView.setAdapter(adapter);
        if (connection.getNeedNetwork()) {
            Toasty.error(this, this.getString(R.string.no_user_login), Toasty.LENGTH_LONG, true).show();
        }
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
                    span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0,
                            charCount.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    newProjectName.setHelper(span);
                } else {
                    span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), 0,
                            charCount.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    newProjectName.setHelper(span);
                }
            }
        });
    }

    /*
     * Load projects from workspace
     */
    private Object loadProjects(Object... o) {
        projectList.clear();
        this.projectManager.readPreviousProjects(projectList);
        return null;
    }

    /*
     * Find in XML views from there IDs
     */
    private void findViews() {
        this.drawerLayout = this.findViewById(R.id.drawer_layout);
        this.navigationView = this.findViewById(R.id.nav_view);
        this.recyclerView = this.findViewById(R.id.projects_recycler_view);
        this.mainLayout = this.findViewById(R.id.main_layout_start_activity);
        mainLayout.setColorSchemeColors(Color.BLUE, Color.RED);
        this.mainLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            ActivityTask st = new ActivityTask(null);
            st.setOnTaskStarted(this::loadProjects);
            st.setOnPostTask((c) -> {
                changeListView(c);
                mainLayout.setRefreshing(false);
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
     * Setup Navigation view Interfaces
     */
    private void setupNavigationView() {
        this.navigationView.setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.start_nav_open) {
                fileBrowserDialog.getBrowserDialog().show();
            }
            drawerLayout.closeDrawers();
            return true;
        });
        DrawerArrowDrawable drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        drawerToggle = new ActionBarDrawerToggleCompat(this, drawerLayout,
                drawerArrow, R.drawable.back_icon,
                R.drawable.action_bar_menu) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();

            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
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
