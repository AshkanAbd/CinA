package ir.ashkanabd.cina.view;

import android.graphics.Color;
import android.view.View;
import com.unnamed.b.atv.model.TreeNode;
import ir.ashkanabd.cina.R;

import java.io.File;
import java.io.IOException;

public class FileBrowserListeners {

    private View preClickedView;
    private FileBrowserDialog fileBrowserDialog;

    FileBrowserListeners(FileBrowserDialog browserDialog) {
        this.fileBrowserDialog = browserDialog;
    }

    /**
     * Call when On a {@link TreeNode} clicked
     *
     * @param node  {@link TreeNode} clicked tree node
     * @param value {@link File} the file that {@link TreeNode} points to it
     */
    public void onNodeClick(TreeNode node, Object value) {
        File file = (File) value;
        FileView fileView = (FileView) node.getViewHolder();
        if (preClickedView != null) {
            preClickedView.setBackgroundColor(Color.WHITE);
        }
        preClickedView = fileView.getView().findViewById(R.id.main_layout_file_layout);
        preClickedView.setBackgroundColor(Color.parseColor("#FFFFFAD6"));
        if (file.isDirectory()) {
            fileBrowserDialog.changeFileStatus(fileView.getView().findViewById(R.id.file_statue_file_layout), node.isExpanded());
        } else {
            try {
                fileBrowserDialog.getActivity().getEditor().setText(fileBrowserDialog.getActivity().readTargetFile(file));
            } catch (IOException ignored) {
            }
            fileBrowserDialog.getBrowserDialog().dismiss();
        }
    }
}
