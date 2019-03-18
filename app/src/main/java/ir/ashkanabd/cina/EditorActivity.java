package ir.ashkanabd.cina;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.navigation.NavigationView;
import ir.ashkanabd.cina.project.Project;
import ir.ashkanabd.cina.view.FileBrowser.AppCompatActivityFileBrowserSupport;
import ir.ashkanabd.cina.view.FileBrowser.FileBrowserDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EditorActivity extends AppCompatActivityFileBrowserSupport {

    private Toolbar projectToolbar;
    private ActionBar projectActionBar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Project selectedProject;
    private File currentFile;
    private String currentFilePath;
    private boolean drawerIsOpen = false;
    private MaterialDialog loadingDialog;
    private boolean isLoadingDialog = false;
    private FileBrowserDialog fileBrowserDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        setupLoadingProgress();
        changeLoadingProgressStatus();
        findViews();
        prepareActivity(getIntent().getExtras());
        fileBrowserDialog = new FileBrowserDialog(this, selectedProject);
        setupActionBar();
        setupNavigationView();
        changeLoadingProgressStatus();
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
                editor.setText(readTargetFile(currentFile));
            } else {
                currentFile = null;
                currentFilePath = null;
            }
        } catch (IOException e) {
            // TODO: 3/16/19 Catch project file reading error
        }
        View navigationHeader = navigationView.getHeaderView(0);
        TextView headerTextView = navigationHeader.findViewById(R.id.project_nav_header_text_view);
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
            EditorActivity.this.finish();
        }
        if (item.getItemId() == R.id.project_nav_browse) {
            fileBrowserDialog.getBrowserDialog().show();
        }
        drawerLayout.closeDrawers();
        return true;
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
