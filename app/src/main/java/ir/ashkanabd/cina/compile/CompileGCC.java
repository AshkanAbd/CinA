package ir.ashkanabd.cina.compile;

import android.app.Activity;
import android.util.Log;

import java.util.Scanner;

import java.io.File;
import java.io.IOException;

public class CompileGCC {
    private CompilerSetup compilerSetup;
    private Process compileProcess;
    private Activity context;
    private File workspace;

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

    public void Compile(File... inputFiles) throws IOException {
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
