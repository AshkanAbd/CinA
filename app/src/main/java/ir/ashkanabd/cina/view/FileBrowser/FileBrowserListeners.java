package ir.ashkanabd.cina.view.FileBrowser;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.appcompat.widget.AppCompatImageView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.rey.material.widget.EditText;
import com.rey.material.widget.TextView;
import com.unnamed.b.atv.model.TreeNode;
import ir.ashkanabd.cina.EditorActivity;
import ir.ashkanabd.cina.R;
import ir.ashkanabd.cina.project.ProjectManager;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FileBrowserListeners {

    static int NONE = 0;
    static int CREATE_FILE = 1;
    static int CREATE_FOLDER = 2;

    private View preClickedView;
    private FileBrowserDialog fileBrowserDialog;
    private MaterialDialog fileNameDialog;
    private MaterialDialog deleteFileDialog;
    private EditText fileNameEditText;
    private TextView deleteFileTitle;
    private boolean validFileName = false;
    private int createMode = NONE;

    FileBrowserListeners(FileBrowserDialog browserDialog) {
        this.fileBrowserDialog = browserDialog;
        setupFileNameDialog();
        setupDeleteFileDialog();
    }

    private void setupDeleteFileDialog() {
        deleteFileDialog = new MaterialDialog(fileBrowserDialog.getActivity());
        deleteFileDialog.setContentView(R.layout.delete_file_layout);
        BootstrapButton deleteButton = deleteFileDialog.findViewById(R.id.delete_delete_file_layout);
        BootstrapButton cancelButton = deleteFileDialog.findViewById(R.id.cancel_delete_file_layout);
        deleteFileTitle = deleteFileDialog.findViewById(R.id.title_delete_file_layout);
        deleteButton.setOnClickListener(this::onDeleteFileButtonClick);
        cancelButton.setOnClickListener(this::onCancelFileButtonClick);
    }

    private void setupFileNameDialog() {
        this.fileNameDialog = new MaterialDialog(fileBrowserDialog.getActivity());
        this.fileNameDialog.setContentView(R.layout.new_file_name_layout);
        this.fileNameDialog.setCancelable(true);
        BootstrapButton createButton = fileNameDialog.findViewById(R.id.create_button_new_file_name);
        BootstrapButton cancelButton = fileNameDialog.findViewById(R.id.cancel_button_new_file_name);
        fileNameEditText = fileNameDialog.findViewById(R.id.edit_new_file_name);
        createButton.setOnClickListener(this::onFileCreateButtonClick);
        cancelButton.setOnClickListener(this::onFileCancelButtonClick);
        fileNameEditText.addTextChangedListener(new FileNameTextWatcher());
        this.fileNameDialog.setOnDismissListener(this::onCreateNewFileDialogDismiss);
    }

    /**
     * Call when On a {@link TreeNode} clicked
     *
     * @param node  {@link TreeNode} clicked tree node
     * @param value {@link File} the file that {@link TreeNode} points to it
     */
    void onNodeClick(TreeNode node, Object value) {
        FileView fileView = (FileView) node.getViewHolder();
        if (preClickedView != null) {
            preClickedView.setBackgroundColor(Color.WHITE);
        }
        preClickedView = fileView.getView().findViewById(R.id.main_layout_file_layout);
        preClickedView.setBackgroundColor(Color.parseColor("#FFFFFAD6"));
    }

    void onFileStatusClick(View view) {
        AppCompatImageView imageView = (AppCompatImageView) view;
        TreeNode node = (TreeNode) ((View) imageView.getParent()).getTag();
        if (imageView.getVisibility() == View.VISIBLE) {
            boolean isOpen = "open".equals(imageView.getTag());
            if (isOpen) {
                imageView.setImageDrawable(fileBrowserDialog.getActivity().getResources().getDrawable(R.drawable.close_folder_icon));
                imageView.setTag("close");
                fileBrowserDialog.getAndroidTreeView().toggleNode(node);
            } else {
                imageView.setImageDrawable(fileBrowserDialog.getActivity().getResources().getDrawable(R.drawable.open_folder_icon));
                imageView.setTag("open");
                fileBrowserDialog.getAndroidTreeView().toggleNode(node);
            }
        }
    }

    private void onFileCreateButtonClick(View view) {
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
            e.printStackTrace();
        }
        fileBrowserDialog.getBrowserDialog().dismiss();
        fileBrowserDialog.load();
        this.fileNameDialog.dismiss();
        fileBrowserDialog.getBrowserDialog().show();
    }

    private void onFileCancelButtonClick(View view) {
        this.fileNameDialog.dismiss();
    }

    private void effectingChangesOnProject() {
        try {
            ProjectManager.writeFile(fileBrowserDialog.getSelectedProject().toString(), fileBrowserDialog.getSelectedProject().getProjectFile());
            if (fileBrowserDialog.getActivity() instanceof EditorActivity) {
                ((EditorActivity) fileBrowserDialog.getActivity()).showProjectInfo();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onDeleteFileButtonClick(View view) {
        File file = fileBrowserDialog.getFile(preClickedView);
        ProjectManager.remove(file);
        fileBrowserDialog.getSelectedProject().removeSource(file.getAbsolutePath());
        effectingChangesOnProject();
        fileBrowserDialog.getBrowserDialog().dismiss();
        fileBrowserDialog.load();
        deleteFileDialog.dismiss();
        fileBrowserDialog.getBrowserDialog().show();
    }

    private void onCancelFileButtonClick(View view) {
        deleteFileDialog.dismiss();
    }

    void onNewFileButtonClick(View view) {
        if (preClickedView == null) return;
        File file = fileBrowserDialog.getFile(preClickedView);
        if (!file.isDirectory()) return;
        createMode = CREATE_FILE;
        fileNameEditText.setHint("Enter file name");
        this.fileNameDialog.show();
    }

    void onNewDirButtonClick(View view) {
        if (preClickedView == null) return;
        File file = fileBrowserDialog.getFile(preClickedView);
        if (!file.isDirectory()) return;
        createMode = CREATE_FOLDER;
        fileNameEditText.setHint("Enter folder name");
        this.fileNameDialog.show();
    }

    void onDeleteButtonClick(View view) {
        if (preClickedView == null) return;
        File file = fileBrowserDialog.getFile(preClickedView);
        if (fileBrowserDialog.getSelectedProject().getDir().equals(file.getAbsolutePath()))
            return;
        this.deleteFileTitle.setText("Delete " + file.getName() + "?");
        this.deleteFileDialog.show();
    }

    void onOpenButtonClick(View view) {
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
            e.printStackTrace();
        }
        fileBrowserDialog.getBrowserDialog().dismiss();
    }

    void onCancelButtonClick(View view) {
        ((Dialog) fileBrowserDialog.getBrowserDialog()).dismiss();
    }

    void onFileBrowserDialogDismiss(DialogInterface dialogInterface) {
        if (preClickedView != null)
            preClickedView.setBackgroundColor(Color.WHITE);
        preClickedView = null;
        createMode = NONE;
    }

    public void onCreateNewFileDialogDismiss(DialogInterface dialog) {
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
                FileBrowserListeners.this.fileNameEditText.setHelper("Error: \"" + objs[1] + "\"");
                FileBrowserListeners.this.validFileName = false;
                return;
            }
            if (len > 30) {
                FileBrowserListeners.this.fileNameEditText.setHelper("too long name: " + len + " characters");
                FileBrowserListeners.this.validFileName = false;
                return;
            }
            FileBrowserListeners.this.fileNameEditText.setHelper("");
            FileBrowserListeners.this.validFileName = true;
        }

        private Object[] checkFileName(String str) {
            Pattern fileNamePattern = null;
            if (FileBrowserListeners.this.createMode == FileBrowserListeners.CREATE_FILE)
                fileNamePattern = Pattern.compile("[a-zA-Z0-9_]+\\.[a-zA-Z]+");
            if (FileBrowserListeners.this.createMode == FileBrowserListeners.CREATE_FOLDER)
                fileNamePattern = Pattern.compile("[a-zA-Z0-9_]+");
            if (fileNamePattern == null) return new Object[]{true, "Unknown error"};
            Matcher fileNameMatcher = fileNamePattern.matcher(str);
            if (!fileNameMatcher.matches()) {
                return new Object[]{false, "Invalid name"};
            }
            if (FileBrowserListeners.this.createMode == FileBrowserListeners.CREATE_FILE)
                if (!FileBrowserListeners.this.fileBrowserDialog.getFileBrowser().checkFileName(str))
                    return new Object[]{false, "Invalid file format"};
            return new Object[]{true};
        }
    }
}
