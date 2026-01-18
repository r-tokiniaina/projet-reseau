package networking;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import logging.Log;
import model.Settings;

public class AppServer implements Runnable, Closeable {

    private ServerSocket server;
    private int port;
    private volatile boolean running;

    public AppServer() {
        this.port = Settings.getInstance().getServerPort();
        this.running = false;

        try {
            this.server = new ServerSocket(port);
            this.server.setSoTimeout(1000); // 1 second
            Log.info("AppServer started");
        }
        catch (IOException e) {
            Log.error("Error while creating AppServer: " + e.getMessage());
        }
    }


    @Override
    public void run() {
        running = true;
        Log.info("AppServer listening on port " + port);

        while (running) {
            try (Socket client = server.accept()) {
                handleClient(client);
                client.close();
            }
            catch (SocketTimeoutException e) {
                Log.info("Accept timed out");
            }
            catch (IOException e) {
                Log.error("Error while accepting socket: " + e.getMessage());
            }
        }

        try {
            this.server.close();
            Log.info("AppServer closed");
        }
        catch (IOException e) {
            Log.error("Error while closing AppServer: " + e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        this.running = false;
    }


    private void handleClient(Socket client) throws IOException {
        AppRequest request = AppProtocol.readRequest(client);

        if (request.getCommand().equals("LIST")) {
            handleListRequest(client, request);
        }
        else {
            Log.error("Unknown command: " + request.getCommand());
        }
    }


    private void handleListRequest(Socket client, AppRequest request) throws IOException {
        AppResponse response = new AppResponse(request);
        response.setResponse("ERROR");
        response.set("error", "Unimplemented request");
        AppProtocol.writeResponse(client, response);
    }
}
