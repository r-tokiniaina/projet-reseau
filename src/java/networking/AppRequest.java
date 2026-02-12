package networking;

import java.util.HashMap;
import java.util.Map;

public class AppRequest {

    private String command;
    private Map<String, Object> others;


    public AppRequest() {
        this.command = null;
        this.others = new HashMap<>();
    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Object get(String key) {
        return others.get(key);
    }

    public void set(String key, Object value) {
        others.put(key, value);
    }
}
