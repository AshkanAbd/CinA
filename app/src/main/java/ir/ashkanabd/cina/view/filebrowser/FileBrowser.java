package ir.ashkanabd.cina.view.filebrowser;

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
            createFormats(fileFormat);
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
     * Change file for format in needed formats.
     * <br/>For example C++ -> Cpp
     *
     * @param fileFormat
     */
    private void createFormats(String... fileFormat) {
        this.projectLang = new String[fileFormat.length];
        for (int i = 0; i < fileFormat.length; i++) {
            String str = fileFormat[i];
            projectLang[i] = str.replace('+', 'p');
        }
        System.out.println();
    }

    /**
     * start for browsing in storage
     */
    public void browse() {
        checkedFiles = new HashSet<>();
        root = browse(root.getChildren().get(0), fileStructure);
        root = root.getRoot();
    }

    /**
     * DFS algorithm for browsing in storage
     *
     * @param tree
     * @param structure
     * @return a {@link TreeNode} that contains file structure in given directory and searching language and max deep
     */
    private TreeNode browse(TreeNode tree, FileStructure structure) {
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
                        if (file.isHidden())
                            continue;
                        if (dialog.getSelectedProject() != null && file.getAbsolutePath().equals(dialog.getSelectedProject().getOut()))
                            continue;
                        nextNode = new TreeNode(file).setViewHolder(new FileView(dialog));
                        tree.addChild(nextNode);
                        FileStructure fs = (FileStructure) structure.clone();
                        fs.changeDir(file);
                        browse(nextNode, fs);
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
            // Just for log...
        }
        return tree;
    }

    /**
     * Check fileName matches with {@link FileBrowser#projectLang}
     *
     * @param fileName check with format
     * @return return true if fileName matches otherwise return false
     */
    boolean checkFileName(String fileName) {
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
