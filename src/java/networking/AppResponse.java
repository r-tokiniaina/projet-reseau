package networking;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonObject;

public class AppResponse {

    private String response;
    private long requestId;
    private long responseId;
    private Map<String, Object> others;


    public AppResponse(AppRequest request) {
        this.response = null;
        this.requestId = request.getRequestId();
        this.responseId = new Random().nextLong();
        this.others = new HashMap<>();
    }

    public AppResponse(AppResponse response) {
        this.response = null;
        this.requestId = response.getRequestId();
        this.responseId = response.getResponseId();
        this.others = new HashMap<>();
    }

    public AppResponse(JsonObject json) {
        this.response = json.get("response").getAsString();
        this.requestId = json.get("request-id").getAsLong();
        this.responseId = json.get("response-id").getAsLong();
        this.others = new HashMap<>();

        if (json.has("error")) {
            this.others.put("error", json.get("error").getAsString());
        }
        // TODO: ajouter les autres attributs dans others
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public long getRequestId() {
        return requestId;
    }

    public long getResponseId() {
        return responseId;
    }

    public Object get(String key) {
        return others.get(key);
    }

    public void set(String key, Object value) {
        others.put(key, value);
    }


    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("response", response);
        json.addProperty("request-id", requestId);
        json.addProperty("response-id", responseId);

        if (others.containsKey("error")) {
            json.addProperty("error", (String) others.get("error"));
        }

        return json;
    }
}
