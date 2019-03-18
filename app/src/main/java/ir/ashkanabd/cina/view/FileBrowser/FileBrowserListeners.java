package ir.ashkanabd.cina.view.FileBrowser;

import android.graphics.Color;
import android.view.View;
import androidx.appcompat.widget.AppCompatImageView;
import com.unnamed.b.atv.model.TreeNode;
import ir.ashkanabd.cina.R;

import java.io.File;

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
//        File file = (File) value;
        FileView fileView = (FileView) node.getViewHolder();
        if (preClickedView != null) {
            preClickedView.setBackgroundColor(Color.WHITE);
        }
        preClickedView = fileView.getView().findViewById(R.id.main_layout_file_layout);
        preClickedView.setBackgroundColor(Color.parseColor("#FFFFFAD6"));
        /*if (file.isDirectory()) {
            fileBrowserDialog.changeFileStatus(fileView.getView().findViewById(R.id.file_statue_file_layout), node.isExpanded());
        } else {
            try {
                fileBrowserDialog.getActivity().getEditor().setText(fileBrowserDialog.getActivity().readTargetFile(file));
            } catch (IOException ignored) {
            }
            fileBrowserDialog.getBrowserDialog().dismiss();
        }*/
    }

    public void onFileStatusClick(View view) {
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
}
