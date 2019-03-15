package ir.ashkanabd.cina;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {

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
        File workspace = new File(Environment.getExternalStorageDirectory() + "/CinAProjects/");
        File likeProjects[] = workspace.listFiles();
        List<File> projectList = new ArrayList<>();
        for (File likeProject : likeProjects) {
            if (isProject(likeProject)) {

            }
        }
    }

    /*
    check given File(directory) is project or not
     */
    private boolean isProject(File likeProject) {
        if (!likeProject.isDirectory())
            return false;
        String[] subFiles = likeProject.list((path, name) -> name.endsWith("cina"));
        return subFiles.length == 1;
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
