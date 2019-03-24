package ir.ashkanabd.cina.project;

import androidx.annotation.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Class for control project info and pars it to json or String
 */
public class Project implements Serializable {
    private String name;
    private String lang;
    private String description;
    private String dir;
    private String out;
    private ArrayList<String> source;
    private HashMap<String, Object> jsonMap;

    public Project() {
        this.source = new ArrayList<>();
        this.jsonMap = new HashMap<>();
    }

    public Project(String name, String lang, String description, String dir, String out, ArrayList<String> source) {
        this.name = name;
        this.lang = lang;
        this.description = description;
        this.dir = dir;
        this.out = out;
        this.source = source;
        this.jsonMap = new HashMap<>();
        this.jsonMap.put("name", name);
        this.jsonMap.put("lang", lang);
        this.jsonMap.put("description", description);
        this.jsonMap.put("dir", dir);
        this.jsonMap.put("src", source);
        this.jsonMap.put("out", out);
    }

    public Project(String json) throws JSONException {
        this(new JSONObject(json));
    }

    public Project(JSONObject jsonObject) throws JSONException {
        try {
            this.name = jsonObject.getString("name");
            this.lang = jsonObject.getString("lang");
            this.dir = jsonObject.getString("dir");
            this.out = jsonObject.getString("out");
            if (jsonObject.has("description")) {
                this.description = jsonObject.getString("description");
            } else {
                this.description = "";
            }
            this.source = new ArrayList<>();
            JSONArray jsonArr = jsonObject.getJSONArray("src");
            for (int i = 0; i < jsonArr.length(); i++) {
                this.source.add(jsonArr.getString(i));
            }
            this.jsonMap = new HashMap<>();
            this.jsonMap.put("name", name);
            this.jsonMap.put("lang", lang);
            this.jsonMap.put("dir", dir);
            this.jsonMap.put("out", out);
            this.jsonMap.put("description", description);
            this.jsonMap.put("src", source);
        } catch (Exception ignored) {
            throw new JSONException("Invalid Project structure");
        }
    }

    public void setName(String name) {
        this.name = name;
        this.jsonMap.put("name", name);
    }

    public void setLang(String lang) {
        this.lang = lang;
        this.jsonMap.put("lang", lang);
    }

    public void setDir(String dir) {
        this.dir = dir;
        this.jsonMap.put("dir", dir);
    }

    public void setSource(ArrayList<String> source) {
        this.source = source;
        this.jsonMap.put("src", source);
    }

    public void addSource(String src) {
        this.source.add(src);
        this.jsonMap.put("src", this.source);
    }

    public void removeSource(String src) {
        if (this.source.remove(src))
            this.jsonMap.put("src", this.source);
    }

    public void setDescription(String description) {
        this.description = description;
        this.jsonMap.put("description", description);
    }

    public void setOut(String out) {
        this.out = out;
        this.jsonMap.put("out", out);
    }

    public String getOut() {
        return out;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getLang() {
        return lang;
    }

    public String getDir() {
        return dir;
    }

    public ArrayList<String> getSource() {
        return source;
    }

    public File[] getSourceAsFile() {
        File[] files = new File[source.size()];
        for (int i = 0; i < source.size(); i++) {
            files[i] = new File(source.get(i));
        }
        return files;
    }

    public JSONObject toJson() {
        return new JSONObject(this.jsonMap);
    }

    public File getProjectFile() {
        return new File(getDir(), "." + getName() + ".cina");
    }

    @NonNull
    @Override
    public String toString() {
        return toJson().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Project))
            return false;
        Project p = (Project) obj;
        if (!p.dir.equals(this.dir))
            return false;
        if (!p.lang.equals(this.lang))
            return false;
        return p.name.equals(this.name);
    }
}
