package ir.ashkanabd.cina.project;

import android.os.Environment;
import android.util.Log;
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
     * Remove given Project
     */
    public static void removeProject(Project project) {
        File projectDir = new File(project.getDir());
        remove(projectDir);
        Log.e("INFO", "REMOVE PROJECT");
    }

    public static void remove(File file) {
        if (!file.isDirectory()) {
            file.delete();
            return;
        }
        File[] sub = file.listFiles();
        for (File f : sub) {
            remove(f);
        }
        file.delete();
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
        if (likeProjects == null) {
            Log.e("INFO", "Error while reading project");
            return;
        }
        for (File likeProject : likeProjects) {
            Object[] objs = isProject(likeProject);
            if ((boolean) objs[0]) {
                try {
                    projectList.add(new Project(ProjectManager.readFile((File) objs[1])));
                } catch (JSONException | IOException ignored) {
                    Log.e("INFO", "Error while reading project");
                    // TODO: 3/15/19 Catch project reading error
                }
            }
        }
    }

    /*
     * Read given text file info
     */
    public static String readTargetFile(File targetFile) throws IOException {
        Scanner fileReader = new Scanner(targetFile);
        StringBuilder builder = new StringBuilder();
        while (fileReader.hasNextLine()) {
            builder.append(fileReader.nextLine()).append("\n");
        }
        fileReader.close();
        return builder.toString();
    }

    /*
     * Write string to given file
     */
    public static void writeTargetFile(File targetFile, String fileInfo) throws IOException {
        PrintWriter pw = new PrintWriter(targetFile);
        pw.print(fileInfo);
        pw.close();
    }

    /*
     * Create new Project after checking dialog
     */
    @Nullable
    public Project createNewProject(@NonNull String projectName, @Nullable String description, boolean isC) throws IOException {
        if (projectName.isEmpty())
            return null;
        Project newProject = new Project();
        newProject.setName(projectName);
        newProject.setDescription(description);
        if (isC) {
            newProject.setLang("C");
        } else {
            newProject.setLang("C++");
        }
        if (!this.initializeProject(projectName, newProject))
            return null;
        return newProject;
    }

    /*
     * Initialize project and create files
     */
    private boolean initializeProject(@NonNull String projectName, @NonNull Project project) throws IOException {
        boolean isC = project.getLang().equals("C");
        File projectDir = new File(workspace, projectName);
        if (projectDir.exists()) return false;
        boolean tmp = projectDir.mkdir();
        if (!tmp) return false;
        project.setDir(projectDir.getAbsolutePath());
        File srcDir = new File(projectDir, "src/");
        tmp = srcDir.mkdir();
        if (!tmp) return false;
        File main = isC ? new File(srcDir, "main.c") : new File(srcDir, "main.cpp");
        tmp = main.createNewFile();
        if (!tmp) return false;
        if (isC) {
            putSampleCode(main, true);
        } else {
            putSampleCode(main, false);
        }
        ArrayList<String> src = new ArrayList<>();
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
            writer.println("    printf(\"Hello world\");");
            writer.println("}");
        } else {
            writer.println("#include <iostream>");
            writer.println("");
            writer.println("using namespace std;");
            writer.println("");
            writer.println("int main(int argc, char** argv){");
            writer.println("    cout << \"Hello world\" << endl;");
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
