package ir.ashkanabd.cina;

import android.content.Intent;
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
import ir.ashkanabd.cina.backgroundTasks.ActivityStartTask;
import ir.ashkanabd.cina.backgroundTasks.GccTask;
import ir.ashkanabd.cina.compileAndRun.GccCompiler;
import ir.ashkanabd.cina.project.Project;
import ir.ashkanabd.cina.project.ProjectManager;
import ir.ashkanabd.cina.view.filebrowser.AppCompatActivityFileBrowserSupport;
import ir.ashkanabd.cina.view.filebrowser.FileBrowserDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
    private ActivityStartTask activityStartTask;
    private GccTask compileTask;
    private MaterialDialog compileDialog;
    private TextView compilerOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        setupLoadingProgress();
        changeLoadingProgressStatus();
        activityStartTask = new ActivityStartTask(loadingDialog);
        activityStartTask.setOnTaskStarted(this::onStartTask);
        activityStartTask.setOnPostTask(this::onPostStartTask);
        new Handler().postDelayed(activityStartTask::execute, 1000);
    }

    private Object onStartTask(Object... o) {
        TypefaceProvider.registerDefaultIconSets();
        checkCompiler();
        return null;
    }

    private void onPostStartTask(Object o) {
        findViews();
        prepareActivity(getIntent().getExtras());
        fileBrowserDialog = new FileBrowserDialog(this, selectedProject, selectedProject.getDir(), selectedProject.getLang());
        setupActionBar();
        setupNavigationView();
        setupCompileDialog();
    }

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
            // TODO: 3/20/19 Handle exception
            e.printStackTrace();
        }
    }

    /*
     * Prepare activity for selected Project
     */
    private void prepareActivity(Bundle bundle) {
        selectedProject = (Project) bundle.get("project");
        try {
            if (!selectedProject.getSource().isEmpty()) {
                currentFilePath = selectedProject.getSource().get(0);
                currentFile = new File(currentFilePath);
                editor.setText(ProjectManager.readTargetFile(currentFile));
            } else {
                currentFile = null;
                currentFilePath = null;
            }
        } catch (IOException e) {
            // TODO: 3/16/19 Catch project file reading error
        }
        showProjectInfo();
    }

    public void showProjectInfo() {
        View navigationHeader = navigationView.getHeaderView(0);
        headerTextView = navigationHeader.findViewById(R.id.project_nav_header_text_view);
        String projectInfo = "Project name: " + selectedProject.getName() + "\n" +
                "Description: " + selectedProject.getDescription() + "\n" +
                "Language: " + selectedProject.getLang() + "\n" +
                "Files: " + reformatFileStructure(getFileStructure(selectedProject.getSource())) + "\n" +
                "Build state: Failed\n";
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
                    String fileInfo = editor.getText().toString();
                    ProjectManager.writeTargetFile(currentFile, fileInfo);
                    Toasty.success(this, "Saved successfully", Toasty.LENGTH_SHORT, true).show();
                } catch (IOException e) {
                    // TODO: 3/19/19 Handle this exception
                }
            }
        }
        if (item.getItemId() == R.id.project_nav_compile) {
            compileTask = new GccTask(compileDialog);
            compileTask.setOnTaskStarted(this::compileTask);
            compileTask.setOnPostTask(this::compileTaskResult);
            compileTask.setOnUpdateTask(this::compileProgress);
            compileTask.execute();
        }
        if (item.getItemId() == R.id.project_nav_run) {
            Intent runIntent = new Intent(this, RunActivity.class);
            runIntent.putExtra("project", selectedProject);
            startActivity(runIntent);
        }
        drawerLayout.closeDrawers();
        return true;
    }

    private Object compileTask(Object... o) {
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

    private void compileTaskResult(Object result) {
        Object[] objs = (Object[]) result;
        double endTime = (double) objs[1];
        String compileMsg = (String) objs[0];
        String headerText = headerTextView.getText().toString();
        if ((boolean) objs[2]) {
            headerText = headerText.replace("Build state: Failed", "Build state: Success");
        } else {
            headerText = headerText.replace("Build state: Success", "Build state: Failed");
        }
        headerTextView.setText(headerText);
        compilerOutput.setText(compilerOutput.getText().toString() + compileMsg);
        compilerOutput.setText(compilerOutput.getText().toString() + "Task ends in " + Double.toString(endTime) + "s");
    }

    private void compileProgress(Object... o) {
        String msg = (String) o[0];
        compilerOutput.setText(compilerOutput.getText().toString() + msg);
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
