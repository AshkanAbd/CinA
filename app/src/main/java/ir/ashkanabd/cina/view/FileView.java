package ir.ashkanabd.cina.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import com.unnamed.b.atv.model.TreeNode;
import ir.ashkanabd.cina.EditorActivity;
import ir.ashkanabd.cina.R;

import java.io.File;

public class FileView extends TreeNode.BaseNodeViewHolder<File> {
    private EditorActivity context;

    public FileView(EditorActivity context) {
        super(context);
        this.context = context;
    }

    @Override
    public View createNodeView(TreeNode node, File file) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View fileLayout = inflater.inflate(R.layout.file_layout, null);
        AppCompatTextView fileName = fileLayout.findViewById(R.id.file_name_file_layout);
        AppCompatImageView fileType = fileLayout.findViewById(R.id.file_type_file_layout);
        AppCompatImageView fileStatus = fileLayout.findViewById(R.id.file_statue_file_layout);
        fileName.setText(file.getName());
        if (file.isDirectory()) {
            fileType.setImageResource(R.drawable.folder_icon);
        } else {
            fileType.setImageResource(R.drawable.file_icon);
            fileStatus.setVisibility(View.INVISIBLE);
        }
        fileStatus.setPaddingRelative((node.getLevel() - 1) * 50, 0, 0, 0);
        node.setClickListener(context::onNodeClick);
        return fileLayout;
    }
}
