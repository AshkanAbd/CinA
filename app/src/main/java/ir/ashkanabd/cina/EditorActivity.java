package ir.ashkanabd.cina;

import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.navigation.NavigationView;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;
import ir.ashkanabd.cina.project.Project;
import ir.ashkanabd.cina.view.CodeEditor;
import ir.ashkanabd.cina.Files.FileBrowser;
import ir.ashkanabd.cina.view.FileView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class EditorActivity extends AppCompatActivity {
    private CodeEditor editor;
    private Toolbar projectToolbar;
    private ActionBar projectActionBar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Project selectedProject;
    private File currentFile;
    private String currentFilePath;
    private boolean drawerIsOpen = false;
    private AndroidTreeView androidTreeView;
    private FileBrowser fileBrowser;
    private MaterialDialog loadingDialog;
    private MaterialDialog browserDialog;
    private boolean isLoadingDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        setupLoadingProgress();
        changeLoadingProgressStatus();
        findViews();
        prepareActivity(getIntent().getExtras());
        readProjectStructure();
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
     * Read project file structure
     */
    private void readProjectStructure() {
        fileBrowser = new FileBrowser(new File(selectedProject.getDir()), selectedProject.getLang(), this);
        fileBrowser.browse(3);
        TreeNode tree = fileBrowser.getRoot();
        androidTreeView = new AndroidTreeView(this, tree);
        browserDialog = new MaterialDialog(this);
        browserDialog.setContentView(R.layout.browse_file_layout);
        RelativeLayout mainLayout = browserDialog.findViewById(R.id.browse_file_main_layout);
        mainLayout.addView(androidTreeView.getView());
        browserDialog.cancelable(true);
    }

    /*
     * Call navigation layout menu items selected
     */
    private boolean navigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.project_nav_close) {
            EditorActivity.this.finish();
        }
        if (item.getItemId() == R.id.project_nav_browse) {
            browserDialog.show();
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

    /**
     * Change file status image
     *
     * @param imageView {@link AppCompatImageView} that image should changes
     * @param expanded  {@link TreeNode#isExpanded()} whether Node is expanded or not
     */
    private void changeFileStatus(AppCompatImageView imageView, boolean expanded) {
        if (expanded) {
            imageView.setImageResource(R.drawable.close_folder_icon);
        } else {
            imageView.setImageResource(R.drawable.open_folder_icon);
        }
    }

    /**
     * Call when On a {@link TreeNode} clicked
     *
     * @param node  {@link TreeNode} clicked tree node
     * @param value {@link File} the file that {@link TreeNode} points to it
     */
    public void onNodeClick(TreeNode node, Object value) {
        File file = (File) value;
        FileView fileView = (FileView) node.getViewHolder();
        if (file.isDirectory()) {
            changeFileStatus(fileView.getView().findViewById(R.id.file_statue_file_layout), node.isExpanded());
        } else {
            try {
                editor.setText(readTargetFile(file));
            } catch (IOException ignored) {
            }
            browserDialog.dismiss();
        }
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
