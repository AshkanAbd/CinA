package ir.ashkanabd.cina.project;

import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProjectManager {
    private File workspace;

    public File getWorkspace() {
        return workspace;
    }

    public ProjectManager() {
        workspace = null;
    }

    /*
     * Check given File(directory) is project or not
     */
    public static Object[] isProject(@NonNull File likeProject) {
        if (!likeProject.isDirectory())
            return new Object[]{false};
        File[] subFiles = likeProject.listFiles((path, name) -> name.endsWith("cina"));
        if (subFiles.length == 1) {
            return new Object[]{true, subFiles[0]};
        }
        return new Object[]{false};
    }

    /*
     * Read every directory in main workspace and find CinA projects
     */
    public void readPreviousProjects(@NonNull List<Project> projectList) {
        workspace = new File(Environment.getExternalStorageDirectory() + "/CinAProjects/");
        if (!workspace.exists()) {
            workspace.mkdir();
        }
        File likeProjects[] = workspace.listFiles();
        for (File likeProject : likeProjects) {
            Object[] objs = isProject(likeProject);
            if ((boolean) objs[0]) {
                ProjectFile projectFile = new ProjectFile((File) objs[1]);
                try {
                    projectList.add(new Project(projectFile.readFile()));
                } catch (JSONException | IOException ignored) {
                }
            }
        }
    }

    /*
     * Create new Project after checking dialog
     */
    @Nullable
    public Project createNewProject(@NonNull String projectName, boolean isC) throws IOException {
        if (projectName.isEmpty())
            return null;
        Project newProject = new Project();
        newProject.setName(projectName);
        if (isC) {
            newProject.setLang("c");
        } else {
            newProject.setLang("c++");
        }
        if (!this.initializeProject(projectName, newProject))
            return null;
        return newProject;
    }

    /*
     * Initialize project and create files
     */
    private boolean initializeProject(@NonNull String projectName, @NonNull Project project) throws IOException {
        boolean isC = project.getLang().equals("c");
        File projectDir = new File(workspace, projectName);
        if (projectDir.exists()) return false;
        boolean tmp = projectDir.mkdir();
        if (!tmp) return false;
        project.setDir(projectDir.getAbsolutePath());
        File main = isC ? new File(projectDir, "main.c") : new File(projectDir, "main.cpp");
        tmp = main.createNewFile();
        if (!tmp) return false;
        if (isC) {
            putSampleCode(main, true);
        } else {
            putSampleCode(main, false);
        }
        List<String> src = new ArrayList<>();
        src.add(main.getAbsolutePath());
        project.setSource(src);
        return true;
    }

    /*
     * Put sample "Hello world" project in main file
     */
    private void putSampleCode(File main, boolean isC) throws IOException {
        PrintWriter writer = new PrintWriter(main);
        if (isC) {
            writer.println("#include <stdio.h>");
            writer.println("");
            writer.println("int main(int argc, char** argv){");
            writer.println("\tprintf(\"Hello world\");");
            writer.println("}");
        } else {
            writer.println("#include <iostream>");
            writer.println("");
            writer.println("using namespace std");
            writer.println("");
            writer.println("int main(int argc, char** argv){");
            writer.println("\tcout << \"Hello world\" << endl;");
            writer.println("}");
        }
        writer.close();
    }

    /*
     * Read project info from .cina file
     */
    public static String readFile(File project) throws IOException {
        Scanner scn = new Scanner(project);
        StringBuilder builder = new StringBuilder();
        while (scn.hasNextLine()) {
            builder.append(scn.nextLine());
        }
        scn.close();
        return builder.toString();
    }

    /*
     * write project info in .cina file
     */
    public static void writeFile(String json, File project) throws IOException {
        PrintWriter pw = new PrintWriter(project);
        pw.println(json);
        pw.close();
    }

}
