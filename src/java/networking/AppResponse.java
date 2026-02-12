package networking;

import java.util.HashMap;
import java.util.Map;

public class AppResponse {

    private String response;
    private Map<String, Object> others;


    public AppResponse() {
        this.response = null;
        this.others = new HashMap<>();
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
}
