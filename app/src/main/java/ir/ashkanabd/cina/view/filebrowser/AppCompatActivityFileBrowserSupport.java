package ir.ashkanabd.cina.view.filebrowser;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
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
    protected SharedPreferences appearancePreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppAppearance();
    }

    /*
     * Get app appearance from shared preferences and set it.
     */
    protected void setAppAppearance() {
        appearancePreferences = getSharedPreferences("appearance", MODE_PRIVATE);
        String lang = appearancePreferences.getString("lang", "EN");
        String theme = appearancePreferences.getString("theme", "light");
        // TODO: 3/23/19 Set lang to App language and theme to App theme
    }

    public CodeEditor getEditor() {
        return editor;
    }

    public ActionBar getProjectActionBar() {
        return projectActionBar;
    }
}
