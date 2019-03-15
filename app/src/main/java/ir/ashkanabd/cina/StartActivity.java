package ir.ashkanabd.cina;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import ir.ashkanabd.cina.project.Project;
import ir.ashkanabd.cina.project.ProjectFile;
import org.json.JSONException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    private List<Project> projectList;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        if (!checkStoragePermission()) {
            requestStoragePermission();
        }

    }

    /*
    Read every directory in main workspace and find CinA projects
     */
    private void readPreviousProjects() {
        this.projectList = new ArrayList<>();
        File workspace = new File(Environment.getExternalStorageDirectory() + "/CinAProjects/");
        File likeProjects[] = workspace.listFiles();
        for (File likeProject : likeProjects) {
            File projectInfoFile = null;
            if (isProject(likeProject, projectInfoFile)) {
                ProjectFile projectFile = new ProjectFile(projectInfoFile);
                try {
                    this.projectList.add(new Project(projectFile.readFile()));
                } catch (JSONException | IOException ignored) {
                }
            }
        }
    }

    /*
    check given File(directory) is project or not
     */
    private boolean isProject(File likeProject, File projectInfoFile) {
        if (!likeProject.isDirectory())
            return false;
        File[] subFiles = likeProject.listFiles((path, name) -> name.endsWith("cina"));
        if (subFiles.length == 1) {
            projectInfoFile = subFiles[0];
            return true;
        }
        projectInfoFile = null;
        return false;
    }

    /*
    Storage read and write permission check
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
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }
}
