package networking;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.Socket;
import java.util.List;

import logging.Log;
import model.File;
import model.Peer;

public class AppClient implements Closeable {

    private Peer target;
    private Socket client;

    public AppClient(Peer target) {
        this.target = target;
        try {
            this.client = new Socket(target.getAddress().getHostAddress(), target.getTcpPort());
        }
        catch (UnknownHostException e) {
            Log.error(String.format("There is no server on %s:%d", target.getAddress().getHostAddress(), target.getTcpPort()));
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
        else if (request.getCommand().equals("DELETE")) {
            return handleDeleteRequest(request);
        }
        else if (request.getCommand().equals("CREATE")) {
            return handleCreateRequest(request);
        }
        else if (request.getCommand().equals("UPLOAD")) {
            return handleUploadRequest(request);
        }
        else if (request.getCommand().equals("DOWNLOAD")) {
            return handleDownloadRequest(request);
        }

        Log.error("Unknown command: " + request.getCommand());
        return null;
    }

    @SuppressWarnings("unchecked")
    private Object handleListRequest(AppRequest request) throws IOException {
        AppProtocol.writeRequest(client, request);
        AppResponse response = AppProtocol.readResponse(client);

        return (List<File>) response.get("files");
    }

    private Object handleDeleteRequest(AppRequest request) throws IOException {
        AppProtocol.writeRequest(client, request);
        AppResponse response = AppProtocol.readResponse(client);

        return null;
    }

    private Object handleCreateRequest(AppRequest request) throws IOException {
        AppProtocol.writeRequest(client, request);
        AppResponse response = AppProtocol.readResponse(client);

        return null;
    }

    private Object handleUploadRequest(AppRequest request) throws IOException {
        AppProtocol.writeRequest(client, request);
        AppResponse response = AppProtocol.readResponse(client);

        return null;
    }

    private Object handleDownloadRequest(AppRequest request) throws IOException {
        AppProtocol.writeRequest(client, request);
        AppResponse response = AppProtocol.readResponse(client);

        return null;
    }
}
