package ir.ashkanabd.cina;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.android.material.navigation.NavigationView;
import es.dmoral.toasty.Toasty;
import ir.ashkanabd.cina.backgroundTasks.ActivityTask;
import ir.ashkanabd.cina.backgroundTasks.GccTask;
import ir.ashkanabd.cina.compileAndRun.GccCompiler;
import ir.ashkanabd.cina.database.Connection;
import ir.ashkanabd.cina.project.Project;
import ir.ashkanabd.cina.project.ProjectManager;
import ir.ashkanabd.cina.view.filebrowser.AppCompatActivityFileBrowserSupport;
import ir.ashkanabd.cina.view.filebrowser.FileBrowserDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class EditorActivity extends AppCompatActivityFileBrowserSupport {

    private Toolbar projectToolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Project selectedProject;
    private File currentFile;
    private String currentFilePath;
    private TextView headerTextView;
    private boolean drawerIsOpen = false;
    private MaterialDialog loadingDialog;
    private boolean isLoadingDialog = false;
    private FileBrowserDialog fileBrowserDialog;
    private GccCompiler gccCompiler;
    private ActivityTask activityStartTask;
    private GccTask compileTask;
    private MaterialDialog compileDialog;
    private TextView compilerOutput;
    private boolean compiled = false;
    private Connection connection;
    private SharedPreferences sharedPreferences;
    private String compileParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isDarkTheme)
            setTheme(R.style.AppThemeDark);
        setContentView(R.layout.activity_editor);
        setupLoadingProgress();
        changeLoadingProgressStatus();
        activityStartTask = new ActivityTask(loadingDialog);
        activityStartTask.setOnTaskStarted(o -> onStartTask());
        activityStartTask.setOnPostTask(o -> onPostStartTask());
        new Handler().postDelayed(activityStartTask::execute, 1000);
    }

    /*
     * Background task for checking GCC on activity starts.
     */
    private Object onStartTask() {
        TypefaceProvider.registerDefaultIconSets();
        checkCompiler();
        return null;
    }

    /*
     * Calls on activity start after doing background.
     */
    private void onPostStartTask() {
        findViews();
        prepareActivity(Objects.requireNonNull(getIntent().getExtras()));
        fileBrowserDialog = new FileBrowserDialog(this, selectedProject, selectedProject.getDir(), selectedProject.getLang());
        setupActionBar();
        setupNavigationView();
        setupCompileDialog();
        getCompileParams();
    }

    /*
     * Get compile params from SharedPreferences
     */
    private void getCompileParams() {
        sharedPreferences = getSharedPreferences("compile_params", MODE_PRIVATE);
        String key;
        if (selectedProject.getLang().equalsIgnoreCase("c++")) {
            key = "c++";
        } else {
            key = "c";
        }
        compileParams = sharedPreferences.getString(key, "");
    }

    /*
     * Setup dialog for show compile progress.
     */
    private void setupCompileDialog() {
        compileDialog = new MaterialDialog(this);
        compileDialog.setContentView(R.layout.compile_layout);
        compilerOutput = compileDialog.findViewById(R.id.compiler_out_compile_layout);
        compilerOutput.setText("");
        compilerOutput.setMovementMethod(new ScrollingMovementMethod());
        compileDialog.setCancelable(false);
        compileDialog.setOnDismissListener((obj) -> {
            compilerOutput.setText("");
            compileDialog.setCancelable(false);
        });
    }

    private void checkCompiler() {
        try {
            gccCompiler = new GccCompiler(this);
        } catch (IOException e) {
            Toasty.error(this, this.getString(R.string.compiler_setup_exception), Toasty.LENGTH_SHORT, true).show();
        }
    }

    /*
     * Prepare activity for selected Project
     */
    private void prepareActivity(Bundle bundle) {
        connection = (Connection) bundle.get("connection");
        selectedProject = (Project) bundle.get("project");
        try {
            if (!Objects.requireNonNull(selectedProject).getSource().isEmpty()) {
                currentFilePath = selectedProject.getSource().get(0);
                currentFile = new File(currentFilePath);
                editor.setText(ProjectManager.readTargetFile(currentFile));
            } else {
                currentFile = null;
                currentFilePath = null;
            }
        } catch (IOException e) {
            Toasty.error(this, this.getString(R.string.invalid_source_file), Toasty.LENGTH_SHORT, true).show();
        }
        showProjectInfo();
    }

    /*
     * Show project info at start
     */
    public void showProjectInfo() {
        View navigationHeader = navigationView.getHeaderView(0);
        headerTextView = navigationHeader.findViewById(R.id.project_nav_header_text_view);
        String projectInfo = this.getString(R.string.project_name_show) + selectedProject.getName() + "\n" +
                this.getString(R.string.project_description_show) + selectedProject.getDescription() + "\n" +
                this.getString(R.string.project_language_show) + selectedProject.getLang() + "\n" +
                this.getString(R.string.project_files_show) + reformatFileStructure(getFileStructure(selectedProject.getSource())) + "\n" +
                this.getString(R.string.failed_build_status) + "\n";
        headerTextView.setText(projectInfo);
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
     * Call navigation layout menu items selected
     */
    private boolean navigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.project_nav_close) {
            this.finish();
        }
        if (item.getItemId() == R.id.project_nav_browse) {
            fileBrowserDialog.getBrowserDialog().show();
        }
        if (item.getItemId() == R.id.project_nav_save) {
            if (currentFile != null) {
                try {
                    String fileInfo = Objects.requireNonNull(editor.getText()).toString();
                    ProjectManager.writeTargetFile(currentFile, fileInfo);
                    Toasty.success(this, this.getString(R.string.save_successful), Toasty.LENGTH_SHORT, true).show();
                } catch (IOException e) {
                    Toasty.error(this, this.getString(R.string.save_failed), Toasty.LENGTH_SHORT, true).show();
                }
            }
        }
        if (item.getItemId() == R.id.project_nav_compile) {
            if (connection.getNeedNetwork()) {
                Toasty.error(this, this.getString(R.string.no_user_login), Toasty.LENGTH_LONG, true).show();
                return true;
            }
            if (!connection.isValid()) {
                Toasty.error(this, this.getString(R.string.invalid_user), Toasty.LENGTH_LONG, true).show();
                return true;
            }

            /*
             * Start compile project in background thread
             */
            compileTask = new GccTask(compileDialog);
            compileTask.setOnTaskStarted((o) -> compileTask());
            compileTask.setOnPostTask(this::compileTaskResult);
            compileTask.setOnUpdateTask(this::compileProgress);
            compileTask.execute();
        }
        if (item.getItemId() == R.id.project_nav_run) {
            /*
             * Check for project compiled or not.
             * If compiled then run.
             */
            if (compiled) {
                Intent runIntent = new Intent(this, RunActivity.class);
                runIntent.putExtra("project", selectedProject);
                startActivity(runIntent);
            } else {
                Toasty.error(this, this.getString(R.string.not_compiled_project), Toasty.LENGTH_LONG, true).show();
            }
        }
        drawerLayout.closeDrawers();
        return true;
    }

    /*
     * compile task in background
     */
    private Object compileTask() {
        gccCompiler.setCompileParams(compileParams);
        compileTask.updateProgress("Linking source...\n");
        long startTime = 0;
        StringBuilder builder = new StringBuilder();
        boolean success = false;
        try {
            Thread.sleep(500);
            compileTask.updateProgress("Starting compile...\n");
            Thread.sleep(500);
            startTime = System.currentTimeMillis();
            Object[] objs = gccCompiler.compile(selectedProject);
            String stdout = (String) objs[1];
            String stderr = (String) objs[2];
            if ((boolean) objs[0]) {
                success = true;
                if (!stdout.isEmpty()) {
                    builder.append("Compiled with warnings: ").append(stdout).append("\n");
                } else {
                    builder.append("Compiled successfully.\n");
                }
            } else {
                builder.append("Compile error: ").append(stderr).append("\n");
            }
        } catch (Exception e) {
            String err = e.getMessage();
            builder.append("Unknown error: ").append(err).append("\n");
        }
        long endTime = System.currentTimeMillis();
        return new Object[]{builder.toString(), (endTime - startTime) / 1000.0, success};
    }

    /*
     * Show compile result in UI thread
     */
    private void compileTaskResult(Object result) {
        Object[] objs = (Object[]) result;
        double endTime = (double) objs[1];
        String compileMsg = (String) objs[0];
        String headerText = headerTextView.getText().toString();
        if ((boolean) objs[2]) {
            headerText = headerText.replace(this.getString(R.string.failed_build_status), this.getString(R.string.success_build_status));
            this.compiled = true;
        } else {
            headerText = headerText.replace(this.getString(R.string.success_build_status), this.getString(R.string.failed_build_status));
            this.compiled = false;
        }
        headerTextView.setText(headerText);
        String msg = compilerOutput.getText().toString() + compileMsg + "Task ends in " + Double.toString(endTime) + "s";
        compilerOutput.setText(msg);
    }

    /*
     * Show GCC output while compiling in UI thread
     */
    private void compileProgress(Object... o) {
        String msg = compilerOutput.getText().toString() + o[0];
        compilerOutput.setText(msg);
    }

    /*
     * prepare file structure for show in view
     */
    private String reformatFileStructure(String[] fileStructure) {
        StringBuilder builder = new StringBuilder();
        for (String str : fileStructure) {
            builder.append(str).append("\n");
        }
        return builder.toString();
    }

    /*
     * Create file structure from files in project structure
     */
    private String[] getFileStructure(ArrayList<String> src) {
        String[] fileStructure = new String[src.size()];
        for (int i = 0; i < src.size(); i++) {
            String tmp = src.get(i);
            int index = tmp.indexOf("src");
            if (index == -1)
                continue;
            fileStructure[i] = tmp.substring(index, tmp.length());
        }
        return fileStructure;
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
        setSupportActionBar(projectToolbar);
        projectActionBar = getSupportActionBar();
        projectActionBar.setTitle(currentFile.getName());
        projectActionBar.setDisplayHomeAsUpEnabled(true);
        projectActionBar.setHomeAsUpIndicator(R.drawable.action_bar_menu);
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    /*
     * setup navigation view and drawer layout
     */
    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(this::navigationItemSelected);

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
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        if (drawerIsOpen) {
            drawerLayout.closeDrawers();
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }
}
