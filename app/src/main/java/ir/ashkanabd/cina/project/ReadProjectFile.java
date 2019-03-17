package ir.ashkanabd.cina.project;

import ir.ashkanabd.cina.Files.ProjectFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

@Deprecated
public class ReadProjectFile extends ProjectFile {
    private JSONObject projectJson;

    public ReadProjectFile(File file) throws IOException, JSONException {
        super(file);
        this.projectJson = new JSONObject(this.readFile());
    }

    public String getProjectName() {
        try {
            return this.projectJson.getString("name");
        } catch (JSONException ignored) {
        }
        return null;
    }

    public String getProjectLang() {
        try {
            return this.projectJson.getString("lang");
        } catch (JSONException ignored) {
        }
        return null;
    }

    public String getProjectDir() {
        try {
            return this.projectJson.getString("dir");
        } catch (JSONException ignored) {
        }
        return null;
    }

    public String[] getSourceFiles() {
        try {
            JSONArray srcFiles = this.projectJson.getJSONArray("src");
            String fileName[] = new String[srcFiles.length()];
            for (int i = 0; i < srcFiles.length(); i++) {
                fileName[i] = srcFiles.getString(i);
            }
            return fileName;
        } catch (JSONException ignored) {
        }
        return null;
    }
}
