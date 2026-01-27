package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.File;

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


    public static void writeTo(Socket socket, OutputStream out, long totalSize) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        long totalBytesRead = 0;

        InputStream in = socket.getInputStream();
        while (totalBytesRead < totalSize) {
            int toRead = (int) Math.min(buffer.length, totalSize - totalBytesRead);
            bytesRead = in.read(buffer, 0, toRead);
            if (bytesRead == -1) {
                break;
            }
            out.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
    }

    public static void writeTo(InputStream in, Socket socket) throws IOException {
        in.transferTo(socket.getOutputStream());
    }
}
