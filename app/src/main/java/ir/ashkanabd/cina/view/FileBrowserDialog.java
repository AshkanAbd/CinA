package ir.ashkanabd.cina.view;

import android.widget.RelativeLayout;
import androidx.appcompat.widget.AppCompatImageView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;
import ir.ashkanabd.cina.AppCompatActivityFileBrowserSupport;
import ir.ashkanabd.cina.Files.FileBrowser;
import ir.ashkanabd.cina.R;
import ir.ashkanabd.cina.project.Project;

import java.io.File;

public class FileBrowserDialog {

    private AppCompatActivityFileBrowserSupport activity;
    private FileBrowser fileBrowser;
    private AndroidTreeView androidTreeView;
    private MaterialDialog browserDialog;
    private FileBrowserListeners listeners;

    /*
     * Read project file structure
     */
    public FileBrowserDialog(AppCompatActivityFileBrowserSupport activity, Project selectedProject) {
        this.activity = activity;
        this.listeners = new FileBrowserListeners(this);
        fileBrowser = new FileBrowser(new File(selectedProject.getDir()), this, selectedProject.getLang());
        fileBrowser.browse(3);
        TreeNode tree = fileBrowser.getRoot();
        androidTreeView = new AndroidTreeView(activity, tree);
        browserDialog = new MaterialDialog(activity);
        browserDialog.setContentView(R.layout.browse_file_layout);
        RelativeLayout mainLayout = browserDialog.findViewById(R.id.browse_file_main_layout);
        mainLayout.addView(androidTreeView.getView());
        browserDialog.cancelable(true);
    }

    /**
     * Change file status image
     *
     * @param imageView {@link AppCompatImageView} that image should changes
     * @param expanded  {@link TreeNode#isExpanded()} whether Node is expanded or not
     */
    void changeFileStatus(AppCompatImageView imageView, boolean expanded) {
        if (expanded) {
            imageView.setImageResource(R.drawable.close_folder_icon);
        } else {
            imageView.setImageResource(R.drawable.open_folder_icon);
        }
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
}
