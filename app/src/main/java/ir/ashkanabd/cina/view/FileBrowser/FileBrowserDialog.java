package ir.ashkanabd.cina.view.FileBrowser;

import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import androidx.appcompat.widget.AppCompatImageView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;
import ir.ashkanabd.cina.R;
import ir.ashkanabd.cina.project.Project;
import ir.ashkanabd.cina.view.CodeEditor;

import java.io.File;

public class FileBrowserDialog {

    private AppCompatActivityFileBrowserSupport activity;
    private FileBrowser fileBrowser;
    private Project selectedProject;
    private AndroidTreeView androidTreeView;
    private MaterialDialog browserDialog;
    private FileBrowserListeners listeners;

    /*
     * Read project file structure
     */
    public FileBrowserDialog(AppCompatActivityFileBrowserSupport activity, Project selectedProject) {
        this.activity = activity;
        this.selectedProject = selectedProject;
        this.listeners = new FileBrowserListeners(this);

        fileBrowser = new FileBrowser(new File(selectedProject.getDir()), this, selectedProject.getLang());
        fileBrowser.browse(3);
        TreeNode tree = fileBrowser.getRoot();

        androidTreeView = new AndroidTreeView(activity, tree);
        androidTreeView.setUseAutoToggle(false);

        browserDialog = new MaterialDialog(activity);
        browserDialog.setContentView(R.layout.browse_file_layout);
        RelativeLayout mainLayout = browserDialog.findViewById(R.id.browse_file_main_layout);

        ScrollView subLayout = (ScrollView) androidTreeView.getView();
        RelativeLayout.LayoutParams params;
        if (subLayout.getLayoutParams() == null) {
            params = new RelativeLayout.LayoutParams(-1, -1);
        } else {
            params = new RelativeLayout.LayoutParams(subLayout.getLayoutParams());
        }
        params.setMargins(0, (int) CodeEditor.pxFromDp(getActivity(), 10), 0, 0);
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.browser_browse_file_layout);
        params.addRule(RelativeLayout.ABOVE, R.id.buttons_layout_browse_file_layout);
        subLayout.setLayoutParams(params);
        mainLayout.addView(subLayout);
        browserDialog.cancelable(true);
    }

    public void reload() {
        fileBrowser = new FileBrowser(new File(selectedProject.getDir()), this, selectedProject.getLang());
        fileBrowser.browse(3);
        TreeNode tree = fileBrowser.getRoot();
        androidTreeView.setRoot(tree);
    }

    public MaterialDialog getBrowserDialog() {
        return browserDialog;
    }

    public AppCompatActivityFileBrowserSupport getActivity() {
        return activity;
    }

    public FileBrowserListeners getListeners() {
        return listeners;
    }

    public AndroidTreeView getAndroidTreeView() {
        return androidTreeView;
    }
}
