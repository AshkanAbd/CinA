package ir.ashkanabd.cina.view.FileBrowser;

import android.util.Log;
import com.unnamed.b.atv.model.TreeNode;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for browsing files from given root folder
 */
public class FileBrowser {

    private TreeNode root;
    private String[] projectLang;
    private FileStructure fileStructure;
    private Set<File> checkedFiles;
    private FileBrowserDialog dialog;

    /**
     * Create base {@link TreeNode} and set given file to it.
     * <br/>Set browsing file language
     *
     * @param rootFile   starting file, must be directory
     * @param fileFormat files format that searching for
     * @param dialog
     */
    public FileBrowser(File rootFile, FileBrowserDialog dialog, String... fileFormat) {
        try {
            this.projectLang = fileFormat;
            this.dialog = dialog;
            root = TreeNode.root();
            TreeNode tmp = new TreeNode(rootFile).setViewHolder(new FileView(dialog));
            root.addChild(tmp);
            fileStructure = new FileStructure(rootFile);
        } catch (IOException ignored) {
            // Never will happens
        }
    }

    /**
     * start for browsing in storage
     *
     * @param deep how much search inside {@link FileBrowser#root }
     */
    public void browse(int deep) {
        checkedFiles = new HashSet<>();
        root = browse(0, deep, root.getChildren().get(0), fileStructure);
        root = root.getRoot();
    }

    /**
     * DFS algorithm for browsing in storage
     *
     * @param deep
     * @param max
     * @param tree
     * @param structure
     * @return a {@link TreeNode} that contains file structure in given directory and searching language and max deep
     */
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
                            nextNode = new TreeNode(file).setViewHolder(new FileView(dialog));
                            tree.addChild(nextNode);
                            structure.changeDir(file);
                            browse(deep + 1, max, nextNode, fileStructure);
                        } else {
                            if (checkFileName(file.getName())) {
                                nextNode = new TreeNode(file).setViewHolder(new FileView(dialog));
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

    /**
     * Check fileName matches with {@link FileBrowser#projectLang}
     *
     * @param fileName
     * @return return true if fileName matches otherwise return false
     */
    private boolean checkFileName(String fileName) {
        for (String format : this.projectLang) {
            if (fileName.endsWith("." + format.toLowerCase()) || fileName.endsWith("." + format)) {
                return true;
            }
        }
        return false;
    }

    public TreeNode getRoot() {
        return root;
    }
}
