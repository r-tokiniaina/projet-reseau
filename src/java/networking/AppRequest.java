package networking;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonObject;

public class AppRequest {

    private String command;
    private long requestId;
    private Map<String, Object> others;


    public AppRequest() {
        this.command = null;
        this.requestId = new Random().nextLong();
        this.others = new HashMap<>();
    }

    public AppRequest(JsonObject json) {
        this.command = json.get("command").getAsString();
        this.requestId = json.get("request-id").getAsLong();
        this.others = new HashMap<>();

        if (json.has("path")) {
            this.others.put("path", json.get("path").getAsString());
        }
        // TODO: ajouter les autres attributs dans others
    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public long getRequestId() {
        return requestId;
    }

    public Object get(String key) {
        return others.get(key);
    }

    public void set(String key, Object value) {
        others.put(key, value);
    }


    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("command", command);
        json.addProperty("request-id", requestId);

        if (others.containsKey("path")) {
            json.addProperty("path", (String) others.get("path"));
        }

        return json;
    }
}
