package ir.ashkanabd.cina.compile;

import android.app.Activity;
import android.content.res.AssetManager;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
Class for extract and setup compiler e
 */
public class CompilerSetup {
    private File compilerZipFile;
    private Activity context;
    private String compilerZipPath;

    /*
    Create compiler path and file
     */
    public CompilerSetup(Activity context) {
        this.context = context;
        this.compilerZipPath = context.getFilesDir().getAbsolutePath() + "/compiler/gcc.zip";
        this.compilerZipFile = new File(this.compilerZipPath);
    }

    /*
    check and test compiler
     */
    public boolean checkCompiler() {
        if (!this.compilerZipFile.exists())
            return false;
        File gccDir = new File(this.compilerZipFile.getParent() + "/gcc");
        if (!gccDir.exists())
            return false;
        if (!gccDir.isDirectory())
            return false;
        return true;
    }

    /*
    copy from given asset name to phone memory
     */
    public void copyCompiler(String assetName) throws IOException {
        if (this.compilerZipFile.exists()) {
            boolean delResult = this.compilerZipFile.delete();
            if (!delResult) {
                throw new IOException("Can't delete invalid compiler");
            }
        }
        File parent1 = this.compilerZipFile.getParentFile();
        if (!parent1.exists()) {
            boolean createParent1 = parent1.mkdirs();
            if (!createParent1) {
                throw new IOException("Can't create compiler path");
            }
        }
        boolean createResult = this.compilerZipFile.createNewFile();
        if (!createResult) {
            throw new IOException("Can't create new compiler");
        }
        AssetManager assetManager = this.context.getAssets();
        InputStream inStream = assetManager.open(assetName);
        FileOutputStream outSteam = new FileOutputStream(this.compilerZipFile);
        byte bytes[] = new byte[4 * 1024];
        int count;
        while ((count = inStream.read(bytes)) != -1) {
            outSteam.write(bytes, 0, count);
        }
        inStream.close();
        outSteam.close();
    }

    /*
    extract compiler .zip file to internal storage
     */
    public void extractCompiler(File zipFile) throws IOException {
        ZipInputStream zipInStream = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry;
        String fileName;
        byte[] bytes = new byte[4 * 1024];
        int count;
        while ((zipEntry = zipInStream.getNextEntry()) != null) {
            fileName = zipEntry.getName();
            File tmp = new File(zipFile.getParentFile(), fileName);
            tmp.setReadable(true, false);
            tmp.setWritable(true, false);
            tmp.setExecutable(true, false);
            if (zipEntry.isDirectory()) {
                tmp.mkdirs();
                continue;
            }
            FileOutputStream fileOutStream = new FileOutputStream(tmp);
            while ((count = zipInStream.read(bytes)) != -1) {
                fileOutStream.write(bytes, 0, count);
            }
            fileOutStream.close();
            zipInStream.closeEntry();
        }
        zipInStream.close();
    }


    File getCompilerZip() {
        return this.compilerZipFile;
    }
}
