package ir.ashkanabd.cina.view;

import android.util.Log;
import com.unnamed.b.atv.model.TreeNode;
import ir.ashkanabd.cina.EditorActivity;
import ir.ashkanabd.cina.compile.FileStructure;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FileBrowser {

    private TreeNode root;
    private String projectLang;
    private FileStructure fileStructure;
    private Set<File> checkedFiles;
    private EditorActivity context;

    public FileBrowser(File rootFile, String projectLang, EditorActivity context) {
        try {
            this.projectLang = projectLang.equalsIgnoreCase("c") ? "c" : "cpp";
            this.context = context;
            root = TreeNode.root();
            TreeNode tmp = new TreeNode(rootFile).setViewHolder(new FileView(context));
            root.addChild(tmp);
            fileStructure = new FileStructure(rootFile);
        } catch (IOException ignored) {
            // Never will happens
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
                        TreeNode nextNode;
                        checkedFiles.add(file);
                        if (file.isDirectory()) {
                            nextNode = new TreeNode(file).setViewHolder(new FileView(context));
                            tree.addChild(nextNode);
                            structure.changeDir(file);
                            browse(deep + 1, max, nextNode, fileStructure);
                        } else {
                            if (file.getName().endsWith("." + projectLang)) {
                                nextNode = new TreeNode(file).setViewHolder(new FileView(context));
                                tree.addChild(nextNode);
                            }
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
