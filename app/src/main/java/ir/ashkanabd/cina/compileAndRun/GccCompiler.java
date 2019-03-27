package ir.ashkanabd.cina.compileAndRun;

import android.app.Activity;
import android.util.Log;
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
    private String compileParams;

    /*
     * Check compiler in test it
     */
    public GccCompiler(Activity context) throws IOException {
        this.compileParams = "";
        this.context = context;
        this.compilerSetup = new CompilerSetup(this.context);
        Log.d("CinA", "Checking compiler");
        if (!this.compilerSetup.checkCompiler()) {
            Log.d("CinA", "Compiler not found...");
            this.compilerSetup.copyCompiler("gcc.zip");
            this.compilerSetup.extractCompiler(this.compilerSetup.getCompilerZip());
            this.compilerSetup.getCompilerZip().delete();
        }
        this.workspace = this.compilerSetup.getCompilerZip().getParentFile();
        Log.d("CinA", "Compiler setup successfully");
    }

    /*
     * Compile with given source files and link it to project dir
     */
    public Object[] compile(Project project) throws IOException {
        String compileParam = createCompileParam(project);
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
     * Set compile params tp add in compile time
     */
    public void setCompileParams(String compileParams) {
        this.compileParams = compileParams;
    }

    /*
     * Create compile parameter from given source files
     */
    private String createCompileParam(Project project) {
        StringBuilder builder = new StringBuilder();
        if (project.getLang().equalsIgnoreCase("C++")) {
            builder.append(this.workspace.getAbsolutePath()).append("/gcc/bin/aarch64-linux-android-g++");
        } else {
            builder.append(this.workspace.getAbsolutePath()).append("/gcc/bin/aarch64-linux-android-gcc");
        }
        for (File file : project.getSourceAsFile()) {
            if (!file.isFile()) continue;
            builder.append(" ");
            builder.append(file.getAbsoluteFile());
        }
        builder.append(" -o ").append(project.getName()).append(" -pie ");
        if (project.getLang().equalsIgnoreCase("C++")) {
            builder.append("-std=c++11 ");
        }
        builder.append(compileParams);
        return builder.toString();
    }
}
