package networking;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.net.Socket;
import java.util.List;

import logging.Log;
import model.File;
import model.Peer;
import utils.FileUtils;

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


    @SuppressWarnings("unchecked")
    public List<File> sendListRequest(File file) throws IOException {
        AppRequest request = new AppRequest();
        request.setCommand("LIST");
        request.set("path", file.getPath() + file.getName());

        AppProtocol.writeRequest(client, request);
        AppResponse response = AppProtocol.readResponse(client);

        return (List<File>) response.get("files");
    }

    public void sendDeleteRequest(File file) throws IOException {
        AppRequest request = new AppRequest();
        request.setCommand("DELETE");
        request.set("path", file.getPath() + file.getName());

        AppProtocol.writeRequest(client, request);
        AppResponse response = AppProtocol.readResponse(client);
    }

    public void sendCreateRequest(File file) throws IOException {
        AppRequest request = new AppRequest();
        request.setCommand("CREATE");
        request.set("path", file.getPath() + file.getName());

        AppProtocol.writeRequest(client, request);
        AppResponse response = AppProtocol.readResponse(client);
    }

    public void sendUploadRequest(java.io.File source, File dest) throws IOException {
        AppRequest request = new AppRequest();
        request.setCommand("UPLOAD");
        request.set("path", dest.getPath() + dest.getName());
        request.set("checksum", FileUtils.computeChecksum(source));
        request.set("size", source.length());

        AppProtocol.writeRequest(client, request);
        AppResponse response = AppProtocol.readResponse(client);

        if (response.getResponse().equals("WAITING")) {
            try (InputStream in = new FileInputStream(source)) {
                AppProtocol.writeTo(in, client);
            }

            response = AppProtocol.readResponse(client);
        }
    }

    public java.io.File sendDownloadRequest(File file) throws IOException {
        AppRequest request = new AppRequest();
        request.setCommand("DOWNLOAD");
        request.set("path", file.getPath() + file.getName());

        AppProtocol.writeRequest(client, request);
        AppResponse response = AppProtocol.readResponse(client);

        if (response.getResponse().equals("SENDING")) {
            String checksum = (String) response.get("checksum");
            Number size = (Number) response.get("size");

            java.io.File dest = new java.io.File(System.getProperty("user.home") + "/Téléchargements", file.getName());

            try (OutputStream out = new FileOutputStream(dest)) {
                AppProtocol.writeTo(client, out, size.longValue());
            }

            response = new AppResponse(response);
            response.setResponse("SUCCESS");
            AppProtocol.writeResponse(client, response);

            return dest;
        }

        return null;
    }
}
