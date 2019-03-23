package ir.ashkanabd.cina.view.filebrowser;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import ir.ashkanabd.cina.view.CodeEditor;


/**
 * A class that support {@link FileBrowserDialog}
 * Every activity want to use it should extends from this
 */
public abstract class AppCompatActivityFileBrowserSupport extends AppCompatActivity {

    protected CodeEditor editor;
    protected ActionBar projectActionBar;

    public CodeEditor getEditor() {
        return editor;
    }

    public ActionBar getProjectActionBar() {
        return projectActionBar;
    }
}
