package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AppProtocol {

    public static AppRequest readRequest(Socket socket) throws IOException {
        JsonObject requestObj = readJson(socket);
        AppRequest request = new AppRequest(requestObj);

        return request;
    }

    public static AppResponse readResponse(Socket socket) throws IOException {
        JsonObject responseObj = readJson(socket);
        AppResponse response = new AppResponse(responseObj);

        return response;
    }

    private static JsonObject readJson(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());

        int length = in.readInt();
        byte[] jsonBytes = in.readNBytes(length);
        String json = new String(jsonBytes, StandardCharsets.UTF_8);

        return JsonParser.parseString(json).getAsJsonObject();
    }


    public static void writeRequest(Socket socket, AppRequest request) throws IOException {
        JsonObject requestObj = request.toJson();
        writeJson(socket, requestObj);
    }

    public static void writeResponse(Socket socket, AppResponse response) throws IOException {
        JsonObject responseObj = response.toJson();
        writeJson(socket, responseObj);
    }

    private static void writeJson(Socket socket, JsonObject json) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        byte[] jsonBytes = json.toString().getBytes(StandardCharsets.UTF_8);
        out.writeInt(jsonBytes.length);
        out.write(jsonBytes, 0, jsonBytes.length);
    }
}
