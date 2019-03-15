package ir.ashkanabd.cina.project;

import android.util.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public abstract class ProjectFile {
    private File project;

    public ProjectFile(File file) {
        this.project = file;
    }

    protected String readFile() throws IOException {
        Scanner scn = new Scanner(this.project);
        StringBuilder builder = new StringBuilder();
        while (scn.hasNextLine()) {
            builder.append(scn.nextLine());
        }
        scn.close();
        return builder.toString();
    }

    protected void writeFile(String json) throws IOException {
        PrintWriter pw = new PrintWriter(this.project);
        pw.println(json);
        pw.close();
    }
}
