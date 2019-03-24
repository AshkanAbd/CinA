package ir.ashkanabd.cina.view.filebrowser;

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
    private BootstrapButton cancelButton;
    private BootstrapButton openButton;
    private BootstrapButton createFileButton;
    private BootstrapButton createDirButton;
    private BootstrapButton deleteButton;

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
        fileBrowser.browse();
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
        cancelButton = linearLayout.findViewById(R.id.cancel_browse_file_layout);
        openButton = linearLayout.findViewById(R.id.open_browse_file_layout);
        createFileButton = linearLayout.findViewById(R.id.new_file_browser_file_layout);
        createDirButton = linearLayout.findViewById(R.id.new_folder_browse_file_layout);
        deleteButton = linearLayout.findViewById(R.id.delete_file_browser_file_layout);
        cancelButton.setPadding(0, 0, 0, 0);
        openButton.setPadding(0, 0, 0, 0);
        createFileButton.setPadding(0, 0, 0, 0);
        createDirButton.setPadding(0, 0, 0, 0);
        deleteButton.setPadding(0, 0, 0, 0);
        cancelButton.setOnClickListener(v->listeners.onCancelButtonClick());
        openButton.setOnClickListener(v -> listeners.onOpenButtonClick());
        createFileButton.setOnClickListener(v -> listeners.onNewFileButtonClick());
        createDirButton.setOnClickListener(v -> listeners.onNewDirButtonClick());
        deleteButton.setOnClickListener(v -> listeners.onDeleteButtonClick());

        this.getBrowserDialog().setOnDismissListener(o -> listeners.onFileBrowserDialogDismiss());
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

    public BootstrapButton getCancelButton() {
        return cancelButton;
    }

    public BootstrapButton getOpenButton() {
        return openButton;
    }

    public BootstrapButton getCreateFileButton() {
        return createFileButton;
    }

    public BootstrapButton getCreateDirButton() {
        return createDirButton;
    }

    public BootstrapButton getDeleteButton() {
        return deleteButton;
    }
}
