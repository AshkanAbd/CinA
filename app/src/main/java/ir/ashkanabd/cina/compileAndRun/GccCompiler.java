package ir.ashkanabd.cina.compileAndRun;

import android.app.Activity;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import ir.ashkanabd.cina.project.Project;

import java.io.*;
import java.util.Scanner;

/*
 * Class for compiling source with GCC
 */
public class GccCompiler {
    private CompilerSetup compilerSetup;
    private Activity context;
    private File workspace;

    /*
     * Check compiler in test it
     */
    public GccCompiler(Activity context) throws IOException {
        this.context = context;
        this.compilerSetup = new CompilerSetup(this.context);
        Log.d("CinA", "Checking compiler");
        if (!this.compilerSetup.checkCompiler()) {
            Log.d("CinA", "Compiler not found...");
            this.compilerSetup.copyCompiler("gcc.zip");
            this.compilerSetup.extractCompiler(this.compilerSetup.getCompilerZip());
        }
        this.workspace = this.compilerSetup.getCompilerZip().getParentFile();
        Log.d("CinA", "Compiler setup successfully");
    }

    /*
     * Compile with given source files and link it to project dir
     */
    public Object[] compile(Project project) throws IOException {
        String compileParam = createCompileParam(project.getName(), project.getSourceAsFile());
        Log.e("CinA", compileParam);
        Process compileProcess = Runtime.getRuntime().exec(compileParam, null, new File(project.getOut()));
        Scanner escn = new Scanner(compileProcess.getErrorStream());
        Scanner sscn = new Scanner(compileProcess.getInputStream());
        StringBuilder sBuilder = new StringBuilder();
        StringBuilder eBuilder = new StringBuilder();
        while (sscn.hasNextLine()) {
            sBuilder.append(sscn.nextLine()).append("\n");
        }
        while (escn.hasNextLine()) {
            eBuilder.append(escn.nextLine()).append("\n");
        }
        return new Object[]{eBuilder.toString().isEmpty(), sBuilder.toString(), eBuilder.toString()};
    }

    /*
     * Run compiled program
     */
    public static Object[] run(AppCompatActivity context, Project project) throws IOException {
        String internalPath = project.getOut() + "/" + project.getName();
        File internalFile = new File(internalPath);
        if (!internalFile.exists()) return null;
        String externalPath = context.getFilesDir().getAbsolutePath() + "/projects/" + project.getName();
        File externalFile = new File(externalPath);
        externalFile.getParentFile().mkdirs();
        externalFile.createNewFile();
        externalFile.setExecutable(true, false);
        FileInputStream fileInStream = new FileInputStream(internalFile);
        FileOutputStream fileOutStream = new FileOutputStream(externalFile);
        byte[] buffer = new byte[4 * 1024];
        int count;
        while ((count = fileInStream.read(buffer)) != -1) {
            fileOutStream.write(buffer, 0, count);
        }
        fileInStream.close();
        fileOutStream.close();
        Process runProcess = Runtime.getRuntime().exec(externalFile.getAbsolutePath(), null, externalFile.getParentFile());
        PrintWriter spw = new PrintWriter(runProcess.getOutputStream(), true);
        Scanner escn = new Scanner(runProcess.getErrorStream());
        Scanner sscn = new Scanner(runProcess.getInputStream());
        StringBuilder sBuilder = new StringBuilder();
        StringBuilder eBuilder = new StringBuilder();
        while (sscn.hasNextLine()) {
            sBuilder.append(sscn.nextLine()).append("\n");
        }
        while (escn.hasNextLine()) {
            eBuilder.append(escn.nextLine()).append("\n");
        }
        return new Object[]{sBuilder.toString(), eBuilder.toString()};
    }

    /*
     * Create compile parameter from given source files
     */
    private String createCompileParam(String proejctName, File... inputFiles) {
        StringBuilder builder = new StringBuilder(this.workspace.getAbsolutePath() + "/gcc/bin/aarch64-linux-android-g++");
        for (File file : inputFiles) {
            if (!file.isFile()) continue;
            builder.append(" ");
            builder.append(file.getAbsoluteFile());
        }
        builder.append(" -o ").append(proejctName).append(" -pie");
        return builder.toString();
    }
}
