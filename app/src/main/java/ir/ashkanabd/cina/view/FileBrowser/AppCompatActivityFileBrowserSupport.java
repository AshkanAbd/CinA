package ir.ashkanabd.cina.view.FileBrowser;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import ir.ashkanabd.cina.view.CodeEditor;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * A class that support {@link FileBrowserDialog}
 * Every activity want to use it should extends from this
 */
public abstract class AppCompatActivityFileBrowserSupport extends AppCompatActivity {

    protected CodeEditor editor;
    protected ActionBar projectActionBar;

    /*
     * Read given text file info
     */
    public String readTargetFile(File targetFile) throws IOException {
        Scanner fileReader = new Scanner(targetFile);
        StringBuilder builder = new StringBuilder();
        while (fileReader.hasNextLine()) {
            builder.append(fileReader.nextLine()).append("\n");
        }
        fileReader.close();
        return builder.toString();
    }

    public CodeEditor getEditor() {
        return editor;
    }

    public ActionBar getProjectActionBar() {
        return projectActionBar;
    }
}
