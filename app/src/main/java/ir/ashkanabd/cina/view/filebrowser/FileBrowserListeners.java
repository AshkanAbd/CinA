package ir.ashkanabd.cina.view.filebrowser;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import androidx.appcompat.widget.AppCompatImageView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.rey.material.widget.EditText;
import com.rey.material.widget.TextView;
import com.unnamed.b.atv.model.TreeNode;
import es.dmoral.toasty.Toasty;
import ir.ashkanabd.cina.EditorActivity;
import ir.ashkanabd.cina.R;
import ir.ashkanabd.cina.StartActivity;
import ir.ashkanabd.cina.project.ProjectManager;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileBrowserListeners {

    private static int NONE = 0;
    private static int CREATE_FILE = 1;
    private static int CREATE_FOLDER = 2;

    private View preClickedView;
    private FileBrowserDialog fileBrowserDialog;
    private MaterialDialog fileNameDialog;
    private MaterialDialog deleteFileDialog;
    private EditText fileNameEditText;
    private TextView deleteFileTitle;
    private boolean validFileName = false;
    private int createMode = NONE;
    private Drawable openFolderDrawable, closeFolderDrawable;

    FileBrowserListeners(FileBrowserDialog browserDialog) {
        openFolderDrawable = StartActivity.resourcesContext.getResources().getDrawable(R.drawable.open_folder_icon);
        closeFolderDrawable = StartActivity.resourcesContext.getResources().getDrawable(R.drawable.close_folder_icon);
        TypedValue primaryValue = new TypedValue();
        StartActivity.resourcesContext.getTheme().resolveAttribute(R.attr.primaryColor, primaryValue, true);
        openFolderDrawable.setColorFilter(primaryValue.data,PorterDuff.Mode.SRC_IN);
        closeFolderDrawable.setColorFilter(primaryValue.data,PorterDuff.Mode.SRC_IN);
        this.fileBrowserDialog = browserDialog;
        setupFileNameDialog();
        setupDeleteFileDialog();
    }

    private void setupDeleteFileDialog() {
        deleteFileDialog = new MaterialDialog(fileBrowserDialog.getActivity());
        deleteFileDialog.setContentView(R.layout.delete_file_layout);
        TextView deleteButton = deleteFileDialog.findViewById(R.id.delete_delete_file_layout);
        TextView cancelButton = deleteFileDialog.findViewById(R.id.cancel_delete_file_layout);
        deleteFileTitle = deleteFileDialog.findViewById(R.id.title_delete_file_layout);
        deleteButton.setOnClickListener(v -> onDeleteFileButtonClick());
        cancelButton.setOnClickListener(v -> onCancelFileButtonClick());
    }

    private void setupFileNameDialog() {
        this.fileNameDialog = new MaterialDialog(fileBrowserDialog.getActivity());
        this.fileNameDialog.setContentView(R.layout.new_file_name_layout);
        this.fileNameDialog.setCancelable(true);
        TextView createButton = fileNameDialog.findViewById(R.id.create_button_new_file_name);
        TextView cancelButton = fileNameDialog.findViewById(R.id.cancel_button_new_file_name);
        fileNameEditText = fileNameDialog.findViewById(R.id.edit_new_file_name);
        createButton.setOnClickListener(v -> onFileCreateButtonClick());
        cancelButton.setOnClickListener(v -> onFileCancelButtonClick());
        fileNameEditText.addTextChangedListener(new FileNameTextWatcher());
        this.fileNameDialog.setOnDismissListener(o -> onCreateNewFileDialogDismiss());
    }

    /**
     * Call when On a {@link TreeNode} clicked
     *
     * @param node {@link TreeNode} clicked tree node
     */
    void onNodeClick(TreeNode node) {
        FileView fileView = (FileView) node.getViewHolder();
        if (preClickedView != null) {
            preClickedView.setBackgroundColor(Color.WHITE);
        }
        preClickedView = fileView.getView().findViewById(R.id.main_layout_file_layout);
        preClickedView.setBackgroundColor(fileBrowserDialog.getActivity().getResources().getColor(R.color.file_browser_clicked_node_background));
        setButtonsColor(node);
    }

    private void setButtonsColor(TreeNode node) {
        File file = (File) node.getValue();
        if (fileBrowserDialog.getSelectedProject() != null) {
            if (fileBrowserDialog.getSelectedProject().getDir().equals(file.getAbsolutePath())) {
                fileBrowserDialog.getDeleteButton().setEnabled(false);
                fileBrowserDialog.getDeleteButton().setBootstrapBrand(DefaultBootstrapBrand.REGULAR);
            } else {
                fileBrowserDialog.getDeleteButton().setEnabled(true);
                fileBrowserDialog.getDeleteButton().setBootstrapBrand(DefaultBootstrapBrand.DANGER);
            }
        }
        if (file.isFile()) {
            fileBrowserDialog.getCreateDirButton().setBootstrapBrand(DefaultBootstrapBrand.REGULAR);
            fileBrowserDialog.getCreateDirButton().setEnabled(false);
            fileBrowserDialog.getCreateFileButton().setBootstrapBrand(DefaultBootstrapBrand.REGULAR);
            fileBrowserDialog.getCreateFileButton().setEnabled(false);
        } else {
            fileBrowserDialog.getCreateDirButton().setBootstrapBrand(DefaultBootstrapBrand.PRIMARY);
            fileBrowserDialog.getCreateDirButton().setEnabled(true);
            fileBrowserDialog.getCreateFileButton().setBootstrapBrand(DefaultBootstrapBrand.INFO);
            fileBrowserDialog.getCreateFileButton().setEnabled(true);
        }
        if (file.isDirectory()) {
            fileBrowserDialog.getOpenButton().setBootstrapBrand(DefaultBootstrapBrand.REGULAR);
            fileBrowserDialog.getOpenButton().setEnabled(false);
        } else {
            fileBrowserDialog.getOpenButton().setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);
            fileBrowserDialog.getOpenButton().setEnabled(true);
        }
        fileBrowserDialog.getCreateFileButton().setPadding(0, 0, 0, 0);
        fileBrowserDialog.getCreateDirButton().setPadding(0, 0, 0, 0);
        fileBrowserDialog.getCancelButton().setPadding(0, 0, 0, 0);
        fileBrowserDialog.getOpenButton().setPadding(0, 0, 0, 0);
        fileBrowserDialog.getDeleteButton().setPadding(0, 0, 0, 0);
    }

    void onFileStatusClick(View view) {
        AppCompatImageView imageView = (AppCompatImageView) view;
        TreeNode node = (TreeNode) ((View) imageView.getParent()).getTag();
        if (imageView.getVisibility() == View.VISIBLE) {
            boolean isOpen = "open".equals(imageView.getTag());
            if (isOpen) {
                imageView.setImageDrawable(closeFolderDrawable);
                imageView.setTag("close");
                fileBrowserDialog.getAndroidTreeView().toggleNode(node);
            } else {
                imageView.setImageDrawable(openFolderDrawable);
                imageView.setTag("open");
                fileBrowserDialog.getAndroidTreeView().toggleNode(node);
            }
        }
    }

    private void onFileCreateButtonClick() {
        try {
            if (!validFileName) return;
            String fileName = this.fileNameEditText.getText().toString();
            File file = fileBrowserDialog.getFile(preClickedView);
            File newFile = new File(file, fileName);
            if (this.createMode == CREATE_FILE) {
                newFile.createNewFile();
            }
            if (this.createMode == CREATE_FOLDER) {
                newFile.mkdirs();
            }
            fileBrowserDialog.getSelectedProject().addSource(newFile.getAbsolutePath());
            effectingChangesOnProject();
        } catch (IOException e) {
            Toasty.error(fileBrowserDialog.getActivity(), StartActivity.resourcesContext.getResources().getString(R.string.permission_error1)
                    , Toasty.LENGTH_SHORT, true).show();
        }
        fileBrowserDialog.getBrowserDialog().dismiss();
        fileBrowserDialog.load();
        this.fileNameDialog.dismiss();
        fileBrowserDialog.getBrowserDialog().show();
    }

    private void onFileCancelButtonClick() {
        this.fileNameDialog.dismiss();
    }

    private void effectingChangesOnProject() {
        try {
            ProjectManager.writeFile(fileBrowserDialog.getSelectedProject().toString(), fileBrowserDialog.getSelectedProject().getProjectFile());
            if (fileBrowserDialog.getActivity() instanceof EditorActivity) {
                ((EditorActivity) fileBrowserDialog.getActivity()).showProjectInfo();
            }
        } catch (IOException e) {
            Toasty.error(fileBrowserDialog.getActivity(), StartActivity.resourcesContext.getResources().getString(R.string.invalid_source_file)
                    , Toasty.LENGTH_SHORT, true).show();
        }
    }

    private void onDeleteFileButtonClick() {
        File file = fileBrowserDialog.getFile(preClickedView);
        ProjectManager.remove(file);
        fileBrowserDialog.getSelectedProject().removeSource(file.getAbsolutePath());
        effectingChangesOnProject();
        fileBrowserDialog.getBrowserDialog().dismiss();
        fileBrowserDialog.load();
        deleteFileDialog.dismiss();
        fileBrowserDialog.getBrowserDialog().show();
    }

    private void onCancelFileButtonClick() {
        deleteFileDialog.dismiss();
    }

    void onNewFileButtonClick() {
        if (preClickedView == null) return;
        File file = fileBrowserDialog.getFile(preClickedView);
        if (!file.isDirectory()) return;
        createMode = CREATE_FILE;
        fileNameEditText.setHint(StartActivity.resourcesContext.getResources().getString(R.string.get_new_file_name));
        this.fileNameDialog.show();
    }

    void onNewDirButtonClick() {
        if (preClickedView == null) return;
        File file = fileBrowserDialog.getFile(preClickedView);
        if (!file.isDirectory()) return;
        createMode = CREATE_FOLDER;
        fileNameEditText.setHint(StartActivity.resourcesContext.getResources().getString(R.string.get_new_folder_name));
        this.fileNameDialog.show();
    }

    void onDeleteButtonClick() {
        if (preClickedView == null) return;
        File file = fileBrowserDialog.getFile(preClickedView);
        if (fileBrowserDialog.getSelectedProject().getDir().equals(file.getAbsolutePath()))
            return;
        String msg = StartActivity.resourcesContext.getResources().getString(R.string.delete_file) + file.getName() + "?";
        this.deleteFileTitle.setText(msg);
        this.deleteFileDialog.show();
    }

    void onOpenButtonClick() {
        if (preClickedView == null) return;
        File file = fileBrowserDialog.getFile(preClickedView);
        if (file.isDirectory()) return;
        try {
            if (fileBrowserDialog.getActivity() instanceof EditorActivity) {
                EditorActivity ea = (EditorActivity) fileBrowserDialog.getActivity();
                ea.getEditor().setText(ProjectManager.readTargetFile(file));
                ea.getProjectActionBar().setTitle(file.getName());
                ea.setCurrentFile(file);
            }
        } catch (IOException e) {
            Toasty.error(fileBrowserDialog.getActivity(), StartActivity.resourcesContext.getResources().getString(R.string.invalid_source_file)
                    , Toasty.LENGTH_SHORT, true).show();
        }
        fileBrowserDialog.getBrowserDialog().dismiss();
    }

    void onCancelButtonClick() {
        fileBrowserDialog.getBrowserDialog().dismiss();
    }

    void onFileBrowserDialogDismiss() {
        if (preClickedView != null)
            preClickedView.setBackgroundColor(Color.WHITE);
        preClickedView = null;
        createMode = NONE;
    }

    public void onCreateNewFileDialogDismiss() {
        fileNameEditText.setError("");
        fileNameEditText.setText("");
        fileNameEditText.setHelper("");
    }

    protected class FileNameTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String str = editable.toString();
            int len = str.length();
            Object[] objs = checkFileName(str);
            if (!(boolean) objs[0]) {
                FileBrowserListeners.this.fileNameEditText.setError(StartActivity.resourcesContext.getResources().getString(R.string.error) + objs[1]);
                FileBrowserListeners.this.validFileName = false;
                return;
            }
            if (len > 30) {
                FileBrowserListeners.this.fileNameEditText.setError(StartActivity.resourcesContext.getResources().getString(R.string.too_long_name) + len);
                FileBrowserListeners.this.validFileName = false;
                return;
            }
            FileBrowserListeners.this.fileNameEditText.clearError();
            FileBrowserListeners.this.validFileName = true;
        }

        private Object[] checkFileName(String str) {
            Pattern fileNamePattern = null;
            if (FileBrowserListeners.this.createMode == FileBrowserListeners.CREATE_FILE)
                fileNamePattern = Pattern.compile("[a-zA-Z0-9_]+\\.[a-zA-Z]+");
            if (FileBrowserListeners.this.createMode == FileBrowserListeners.CREATE_FOLDER)
                fileNamePattern = Pattern.compile("[a-zA-Z0-9_]+");
            if (fileNamePattern == null)
                return new Object[]{true, StartActivity.resourcesContext.getResources().getString(R.string.unknown_error)};
            Matcher fileNameMatcher = fileNamePattern.matcher(str);
            if (!fileNameMatcher.matches()) {
                return new Object[]{false, StartActivity.resourcesContext.getResources().getString(R.string.invalid_file_name)};
            }
            if (FileBrowserListeners.this.createMode == FileBrowserListeners.CREATE_FILE)
                if (!FileBrowserListeners.this.fileBrowserDialog.getFileBrowser().checkFileName(str))
                    return new Object[]{false, StartActivity.resourcesContext.getResources().getString(R.string.invalid_file_format)};
            return new Object[]{true};
        }
    }

    public View getPreClickedView() {
        return preClickedView;
    }
}
