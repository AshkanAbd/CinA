package ir.ashkanabd.cina.Files;

import java.util.List;
import java.util.Arrays;

import java.io.File;
import java.io.IOException;

public class FileStructure {

    private File baseFile;

    public FileStructure(File file) throws IOException {
        this.baseFile = file;
        this.checkFile(this.baseFile);
    }

    public FileStructure(String filename) throws IOException {
        this.baseFile = new File(filename);
        this.checkFile(this.baseFile);
    }

    public File[] getListAsFile() throws IOException {
        if (!this.baseFile.canRead()) {
            throw new IOException("Can't read folder,Permission Error");
        }
        return this.baseFile.listFiles();
    }

    public String[] getListAsString() throws IOException {
        if (!this.baseFile.canRead()) {
            throw new IOException("Can't read folder,Permission Error");
        }
        return this.baseFile.list();
    }

    public boolean changeDir(String filename) throws IOException {
        List<String> fileListString = Arrays.asList(this.baseFile.list());
        List<File> fileList = Arrays.asList(this.baseFile.listFiles());
        if (fileListString.contains(filename)) {
            File tmp = fileList.get(fileListString.indexOf(filename));
            if (this.checkFile(tmp)) {
                this.baseFile = tmp;
                return true;
            }
        }
        throw new IOException("Directory don't exist");
    }

    public boolean changeDir(int index) throws IOException {
        List<File> fileList = Arrays.asList(this.baseFile.listFiles());
        if (fileList.size() > index && index > -1) {
            File tmp = fileList.get(index);
            if (this.checkFile(tmp)) {
                this.baseFile = tmp;
                return true;
            }
        }
        throw new IOException("Directory don't exist");
    }

    public boolean changeDir(File file) throws IOException {
        List<File> fileList = Arrays.asList(this.baseFile.listFiles());
        if (fileList.contains(file)) {
            File tmp = fileList.get(fileList.indexOf(file));
            if (this.checkFile(tmp)) {
                this.baseFile = tmp;
                return true;
            }
        }
        throw new IOException("Directory don't exist");
    }

    private boolean checkFile(File file) throws IOException {
        if (!file.exists())
            throw new IOException("Directory doesn't exist");
        if (!file.isDirectory())
            throw new IOException("Only directory allowed");
        return true;
    }
}
