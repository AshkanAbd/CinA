package ir.ashkanabd.cina.project;

import androidx.annotation.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Class for controll project info and pars it to json or String
 */
public class Project implements Serializable {
    private String name;
    private String lang;
    private String description;
    private String dir;
    private ArrayList<String> source;
    private HashMap<String, Object> jsonMap;

    public Project() {
        this.source = new ArrayList<>();
        this.jsonMap = new HashMap<>();
    }

    public Project(String name, String lang, String description, String dir, ArrayList<String> source) {
        this.name = name;
        this.lang = lang;
        this.description = description;
        this.dir = dir;
        this.source = source;
        this.jsonMap = new HashMap<>();
        this.jsonMap.put("name", name);
        this.jsonMap.put("lang", lang);
        this.jsonMap.put("description", description);
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

    public void setDescription(String description) {
        this.description = description;
        this.jsonMap.put("description", description);
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
