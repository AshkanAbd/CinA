package ir.ashkanabd.cina.view.filebrowser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import com.unnamed.b.atv.model.TreeNode;
import ir.ashkanabd.cina.R;

import java.io.File;

/**
 * Class for containing each node of {@link com.unnamed.b.atv.view.AndroidTreeView}
 */
public class FileView extends TreeNode.BaseNodeViewHolder<File> {
    private AppCompatActivityFileBrowserSupport context;
    private FileBrowserListeners listeners;

    public FileView(FileBrowserDialog dialog) {
        super(dialog.getActivity());
        this.context = dialog.getActivity();
        this.listeners = dialog.getListeners();
    }

    /**
     * Creates a view for node that passes into method
     *
     * @param node that view creates for
     * @param file object that node points to it
     * @return view that create for node with given file info
     */
    @Override
    public View createNodeView(TreeNode node, File file) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout fileLayout = (RelativeLayout) inflater.inflate(R.layout.file_layout, null);
        AppCompatTextView fileName = fileLayout.findViewById(R.id.file_name_file_layout);
        AppCompatImageView fileType = fileLayout.findViewById(R.id.file_type_file_layout);
        AppCompatImageView fileStatus = fileLayout.findViewById(R.id.file_statue_file_layout);
        fileName.setText(file.getName());
        if (file.isDirectory()) {
            fileType.setImageResource(R.drawable.folder_icon);
            fileStatus.setTag("close");
            if (node.isLeaf())
                fileStatus.setVisibility(View.INVISIBLE);
        } else {
            fileType.setImageResource(R.drawable.file_icon);
            fileStatus.setVisibility(View.INVISIBLE);
        }
        fileStatus.setPaddingRelative((node.getLevel() - 1) * 50, 0, 0, 0);
        node.setClickListener((n, v) -> listeners.onNodeClick(n));
        fileStatus.setOnClickListener(listeners::onFileStatusClick);
        fileLayout.setTag(node);
        return fileLayout;
    }
}
