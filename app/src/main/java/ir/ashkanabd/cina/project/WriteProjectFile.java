package ir.ashkanabd.cina.project;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WriteProjectFile extends ProjectFile {
    private Map<String, Object> jsonMap;

    public WriteProjectFile(File file) {
        super(file);
        this.jsonMap = new HashMap<>();
    }

    public void addProjectName(String name) {
        this.jsonMap.put("name", name);
    }

    public void addProjectLang(String lang) {
        this.jsonMap.put("lang", lang);
    }

    public void addProjectDir(String dir) {
        this.jsonMap.put("dir", dir);
    }

    public void addSourceFiles(String... src) {
        List<String> srcList = Arrays.asList(src);
        this.jsonMap.put("src", srcList);
    }

    public void writeToFile() {
        try {
            JSONObject jsonObj = new JSONObject(this.jsonMap);
            this.writeFile(jsonObj.toString());
        } catch (IOException ignored) {
        }
    }
}
