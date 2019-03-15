package ir.ashkanabd.cina.project;

import androidx.annotation.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Class for controll project info and pars it to json or String
 */
public class Project {
    private String name;
    private String lang;
    private String dir;
    private List<String> source;
    private Map<String, Object> jsonMap;

    public Project() {
        this.source = new ArrayList<>();
        this.jsonMap = new HashMap<>();
    }

    public Project(String name, String lang, String dir, List<String> source) {
        this.name = name;
        this.lang = lang;
        this.dir = dir;
        this.source = source;
        this.jsonMap = new HashMap<>();
        this.jsonMap.put("name", name);
        this.jsonMap.put("lang", lang);
        this.jsonMap.put("dir", dir);
        this.jsonMap.put("src", source);
    }

    public Project(String json) throws JSONException {
        this(new JSONObject(json));
    }

    public Project(JSONObject jsonObject) throws JSONException {
        try {
            this.name = jsonObject.getString("name");
            this.lang = jsonObject.getString("lang");
            this.dir = jsonObject.getString("dir");
            this.source = new ArrayList<>();
            JSONArray jsonArr = jsonObject.getJSONArray("src");
            for (int i = 0; i < jsonArr.length(); i++) {
                this.source.add(jsonArr.getString(i));
            }
            this.jsonMap = new HashMap<>();
            this.jsonMap.put("name", name);
            this.jsonMap.put("lang", lang);
            this.jsonMap.put("dir", dir);
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

    public void setSource(List<String> source) {
        this.source = source;
        this.jsonMap.put("src", source);
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

    public List<String> getSource() {
        return source;
    }

    public JSONObject toJson() {
        return new JSONObject(this.jsonMap);
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
        if (!p.source.equals(this.source))
            return false;
        if (!p.dir.equals(this.dir))
            return false;
        if (!p.lang.equals(this.lang))
            return false;
        if (!p.name.equals(this.name))
            return false;
        return true;
    }
}
