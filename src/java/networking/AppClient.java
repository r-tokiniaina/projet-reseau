package networking;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.Socket;

import logging.Log;
import model.User;

public class AppClient implements Closeable {

    private User target;
    private Socket client;

    public AppClient(User target) {
        this.target = target;
        try {
            this.client = new Socket(target.getIp(), target.getPort());
        }
        catch (UnknownHostException e) {
            Log.error(String.format("There is no server on %s:%d", target.getIp(), target.getPort()));
        }
        catch (IOException e) {
            Log.error("Error while creating AppClient: " + e.getMessage());
        }
    }


    @Override
    public void close() throws IOException {
        client.close();
    }


    public Object sendRequest(AppRequest request) throws IOException {
        if (request.getCommand().equals("LIST")) {
            return handleListRequest(request);
        }

        Log.error("Unknown command: " + request.getCommand());
        return null;
    }

    private Object handleListRequest(AppRequest request) throws IOException {
        AppProtocol.writeRequest(client, request);
        AppResponse response = AppProtocol.readResponse(client);

        return response;
    }
}
