package ir.ashkanabd.cina;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;
import androidx.annotation.NonNull;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import ir.ashkanabd.cina.project.Project;
import ir.ashkanabd.cina.view.CodeEditor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class EditorActivity extends Activity {
    private CodeEditor editor;
    private Toolbar projectToolbar;
    private ActionBar projectActionBar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Project selectedProject;
    private File currentFile;
    private String currentFilePath;
    private boolean drawerIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        findViews();
        prepareActivity(getIntent().getExtras());
        setupActionBar();
        setupNavigationView();
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
     * Read given text file info
     */
    private String readTargetFile(File targetFile) throws IOException {
        Scanner fileReader = new Scanner(targetFile);
        StringBuilder builder = new StringBuilder();
        while (fileReader.hasNextLine()) {
            builder.append(fileReader.nextLine()).append("\n");
        }
        fileReader.close();
        return builder.toString();
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
            projectActionBar.setTitle(currentFile.getName());
            projectActionBar.setDisplayHomeAsUpEnabled(true);
            projectActionBar.setHomeAsUpIndicator(R.drawable.action_bar_menu);
        }
    }

    /*
     * setup navigation vir=ew and drawer layout
     */
    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.close_project) {
                EditorActivity.this.finish();
            }
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