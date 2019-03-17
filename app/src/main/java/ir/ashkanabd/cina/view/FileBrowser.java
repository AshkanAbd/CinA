package ir.ashkanabd.cina.view;

import android.util.Log;
import com.unnamed.b.atv.model.TreeNode;
import ir.ashkanabd.cina.compile.FileStructure;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FileBrowser {

    private TreeNode root;
    private FileStructure fileStructure;
    private Set<File> checkedFiles;

    public FileBrowser(File rootFile) {
        try {
            root = TreeNode.root();
            TreeNode tmp = new TreeNode(rootFile);
            root.addChild(tmp);
            fileStructure = new FileStructure(rootFile);
        } catch (IOException ignored) {
        }
    }

    public void browse(int deep) {
        checkedFiles = new HashSet<>();
        root = browse(0, deep, root.getChildren().get(0), fileStructure);
        root = root.getRoot();
    }

    private TreeNode browse(int deep, int max, TreeNode tree, FileStructure structure) {
        if (max < 0 || deep < max) {
            try {
                File[] childFiles = structure.getListAsFile();
                if (childFiles == null || childFiles.length == 0) {
                    return tree;
                }
                for (File file : childFiles) {
                    if (!checkedFiles.contains(file)) {
                        TreeNode nextNode = new TreeNode(file);
                        tree.addChild(nextNode);
                        checkedFiles.add(file);
                        boolean tmp = structure.changeDir(file);
                        if (tmp) {
                            browse(deep + 1, max, nextNode, fileStructure);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("CinA", "Permission denied");
            }
        }
        return tree;
    }

    public TreeNode getRoot() {
        return root;
    }
}
