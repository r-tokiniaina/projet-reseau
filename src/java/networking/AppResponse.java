package networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import model.File;

public class AppResponse {

    private String response;
    private Map<String, Object> others;


    public AppResponse() {
        this.response = null;
        this.others = new HashMap<>();
    }

    public AppResponse(JsonObject json) {
        this.response = json.get("response").getAsString();
        this.others = new HashMap<>();

        if (json.has("error")) {
            this.others.put("error", json.get("error").getAsString());
        }

        if (json.has("path")) {
            this.others.put("path", json.get("path").getAsString());
        }
        if (json.has("checksum")) {
            this.others.put("checksum", json.get("checksum").getAsString());
        }

        if (json.has("size")) {
            this.others.put("size", json.get("size").getAsNumber());
        }

        if (json.has("files")) {
            File parent = new File((String) this.others.get("path"), File.Type.DIRECTORY);
            List<File> files = new ArrayList<>();
            for (JsonElement fileElement : json.getAsJsonArray("files")) {
                JsonObject fileObject = fileElement.getAsJsonObject();
                File f = new File();
                f.setName(fileObject.get("name").getAsString());
                f.setType(fileObject.get("type").equals("file") ? File.Type.FILE : File.Type.DIRECTORY);
                files.add(f);
            }
            this.others.put("files", files);
        }
        // TODO: ajouter les autres attributs dans others
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Object get(String key) {
        return others.get(key);
    }

    public void set(String key, Object value) {
        others.put(key, value);
    }


    @SuppressWarnings("unchecked")
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("response", response);

        if (others.containsKey("error")) {
            json.addProperty("error", (String) others.get("error"));
        }

        if (others.containsKey("path")) {
            json.addProperty("path", (String) others.get("path"));
        }

        if (others.containsKey("checksum")) {
            json.addProperty("checksum", (String) others.get("checksum"));
        }

        if (others.containsKey("size")) {
            json.addProperty("size", (Number) others.get("size"));
        }

        if (others.containsKey("files")) {
            JsonArray files = new JsonArray();
            for (File file : (List<File>) others.get("files")) {
                JsonObject fileObject = new JsonObject();
                fileObject.addProperty("name", file.getName());
                fileObject.addProperty("type", file.getType().equals(File.Type.FILE) ? "file" : "directory");
                files.add(fileObject);
            }
            json.add("files", files);
        }

        return json;
    }
}
