package ir.ashkanabd.cina.view.FileBrowser;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import androidx.appcompat.widget.LinearLayoutCompat;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.BootstrapButton;
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
    private String[] fileFormat;
    private String root;

    /*
     * Read project file structure
     */
    public FileBrowserDialog(AppCompatActivityFileBrowserSupport activity, Project selectedProject, String root, String... fileFormat) {
        this.activity = activity;
        this.root = root;
        this.selectedProject = selectedProject;
        this.fileFormat = fileFormat;
        this.listeners = new FileBrowserListeners(this);
        load();
    }

    public void load() {
        fileBrowser = new FileBrowser(new File(root), this, fileFormat);
        fileBrowser.browse(3);
        androidTreeView = new AndroidTreeView(activity, fileBrowser.getRoot());
        androidTreeView.setUseAutoToggle(false);
        setupDialog();
    }

    private void setupDialog() {
        browserDialog = new MaterialDialog(activity);
        browserDialog.setContentView(R.layout.browse_file_layout1);
        RelativeLayout mainLayout = browserDialog.findViewById(R.id.browse_file_main_layout);
        mainLayout.addView(setupDialogView());
        setupDialogViewListeners(mainLayout);
        browserDialog.cancelable(true);
    }

    private View setupDialogView() {
        ScrollView subLayout = (ScrollView) androidTreeView.getView();
        RelativeLayout.LayoutParams params;
        if (subLayout.getLayoutParams() == null) {
            params = new RelativeLayout.LayoutParams(-1, -1);
        } else {
            params = new RelativeLayout.LayoutParams(subLayout.getLayoutParams());
        }
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.browser_browse_file_layout);
        params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.browser_browse_file_layout);
        subLayout.setLayoutParams(params);
        return subLayout;
    }

    protected void setupDialogViewListeners(RelativeLayout view) {
        LinearLayoutCompat linearLayout = view.findViewById(R.id.buttons_layout_browse_file_layout);
        BootstrapButton cancelButton = linearLayout.findViewById(R.id.cancel_browse_file_layout);
        BootstrapButton openButton = linearLayout.findViewById(R.id.open_browse_file_layout);
        BootstrapButton createFileButton = linearLayout.findViewById(R.id.new_file_browser_file_layout);
        BootstrapButton createDirButton = linearLayout.findViewById(R.id.new_folder_browse_file_layout);
        BootstrapButton deleteButton = linearLayout.findViewById(R.id.delete_file_browser_file_layout);
        cancelButton.setPadding(0, 0, 0, 0);
        openButton.setPadding(0, 0, 0, 0);
        createFileButton.setPadding(0, 0, 0, 0);
        createDirButton.setPadding(0, 0, 0, 0);
        deleteButton.setPadding(0, 0, 0, 0);
        cancelButton.setOnClickListener(listeners::onCancelButtonClick);
        openButton.setOnClickListener(listeners::onOpenButtonClick);
        createFileButton.setOnClickListener(listeners::onNewFileButtonClick);
        createDirButton.setOnClickListener(listeners::onNewDirButtonClick);
        deleteButton.setOnClickListener(listeners::onDeleteButtonClick);

        this.getBrowserDialog().setOnDismissListener(listeners::onFileBrowserDialogDismiss);
    }

    public File getFile(View view) {
        TreeNode treeNode = (TreeNode) view.getTag();
        return (File) treeNode.getValue();
    }

    public Project getSelectedProject() {
        return selectedProject;
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

    public FileBrowser getFileBrowser() {
        return fileBrowser;
    }
}
