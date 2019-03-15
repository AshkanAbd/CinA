package ir.ashkanabd.cina.project;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/*
Class for controll Projects nad read and write theme
 */
public class ProjectFile {
    private File project;

    public ProjectFile(File file) {
        this.project = file;
    }

    /*
    Read project info from .cina file
     */
    @Deprecated
    public String readFile() throws IOException {
        Scanner scn = new Scanner(this.project);
        StringBuilder builder = new StringBuilder();
        while (scn.hasNextLine()) {
            builder.append(scn.nextLine());
        }
        scn.close();
        return builder.toString();
    }

    /*
    write project info in .cina file
     */
    @Deprecated
    public void writeFile(String json) throws IOException {
        PrintWriter pw = new PrintWriter(this.project);
        pw.println(json);
        pw.close();
    }
}
