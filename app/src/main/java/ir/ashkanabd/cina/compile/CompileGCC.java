package ir.ashkanabd.cina.compile;

import android.app.Activity;
import android.util.Log;

import java.util.Scanner;

import java.io.File;
import java.io.IOException;

/*
 * Class for compiling source with GCC
 */
public class CompileGCC {
    private CompilerSetup compilerSetup;
    private Process compileProcess;
    private Activity context;
    private File workspace;

    /*
     * Check compiler in test it
     */
    public CompileGCC(Activity context) throws IOException {
        this.context = context;
        this.compilerSetup = new CompilerSetup(this.context);
        if (this.compilerSetup.checkCompiler()) {
            this.compilerSetup.copyCompiler("gcc.zip");
            this.compilerSetup.extractCompiler(this.compilerSetup.getCompilerZip());
        }
        this.workspace = this.compilerSetup.getCompilerZip().getParentFile();
        Log.i("CinA", "Compiler setup successfully");
    }

    /*
     * Compile with given source files and link it to project dir
     */
    public void Compile(File... inputFiles) throws IOException {
        // TODO: 3/15/19 Check compile output and try to link output binary and run program
        String compileParam = createCompileParam(inputFiles);
        this.compileProcess = Runtime.getRuntime().exec(compileParam, null, this.workspace);
        Scanner escn = new Scanner(this.compileProcess.getErrorStream());
        Scanner sscn = new Scanner(this.compileProcess.getInputStream());
        Log.e("STD", "---------------------");
        while (sscn.hasNextLine()) {
            Log.e("STD", sscn.nextLine());
        }
        Log.e("ERR", "---------------------");
        while (escn.hasNextLine()) {
            Log.e("ERR", escn.nextLine());
        }
        Log.e("ERR", "---------------------");
    }

    /*
     * Create compile parameter from given source files
     */
    private String createCompileParam(File... inputFiles) {
        StringBuilder builder = new StringBuilder(this.workspace.getAbsolutePath() + "/gcc/bin/aarch64-linux-android-g++");
        for (File file : inputFiles) {
            builder.append(" ");
            builder.append(file.getAbsoluteFile());
        }
        builder.append(" -o output");
        return builder.toString();
    }
}
