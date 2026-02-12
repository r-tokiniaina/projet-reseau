package p2pshare.network.tcp;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import p2pshare.model.RemoteFile;
import p2pshare.model.Peer;
import p2pshare.config.AppConfig;

public class TcpClient implements Closeable {
    private static final String CLIENT_SCRIPT = "scripts/network/p2p_tcp_client.sh";
    private Peer target;

    public TcpClient(Peer target) throws IOException {
        this.target = target;
    }

    @Override
    public void close() {
    }

    public List<RemoteFile> sendListRequest(RemoteFile file) throws IOException {
        String remotePath = file.getPath() + file.getName();
        List<String> lines = runClientScript("LIST", remotePath);
        if (lines.isEmpty()) {
            throw new IOException("Empty response from remote peer");
        }
        if (lines.get(0).startsWith("ERROR")) {
            throw new IOException(lines.get(0));
        }

        List<RemoteFile> files = new ArrayList<>();
        for (String line : lines) {
            if (!line.startsWith("ENTRY|")) {
                continue;
            }
            String[] parts = line.split("\\|", 3);
            if (parts.length < 3) {
                continue;
            }

            RemoteFile listedFile = new RemoteFile();
            listedFile.setName(parts[2]);
            listedFile.setParent(file);
            listedFile.setType("DIRECTORY".equals(parts[1]) ? RemoteFile.Type.DIRECTORY : RemoteFile.Type.FILE);
            files.add(listedFile);
        }

        return files;
    }

    public void sendDeleteRequest(RemoteFile file) throws IOException {
        String remotePath = file.getPath() + file.getName();
        List<String> lines = runClientScript("DELETE", remotePath);
        if (lines.isEmpty() || !"OK".equals(lines.get(0))) {
            throw new IOException(lines.isEmpty() ? "Delete failed" : lines.get(0));
        }
    }

    public void sendCreateRequest(RemoteFile file) throws IOException {
        String remotePath = file.getPath() + file.getName();
        List<String> lines = runClientScript("CREATE", remotePath);
        if (lines.isEmpty() || !"OK".equals(lines.get(0))) {
            throw new IOException(lines.isEmpty() ? "Create failed" : lines.get(0));
        }
    }

    public void sendUploadRequest(java.io.File source, RemoteFile dest) throws IOException {
        String remotePath = dest.getPath() + dest.getName();
        List<String> lines = runClientScript("UPLOAD", remotePath, source.getAbsolutePath());
        if (lines.isEmpty() || !"OK".equals(lines.get(0))) {
            throw new IOException(lines.isEmpty() ? "Upload failed" : lines.get(0));
        }
    }

    public java.io.File sendDownloadRequest(RemoteFile file) throws IOException {
        java.io.File downloadDir = AppConfig.DOWNLOAD_DIR;
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        java.io.File dest = new java.io.File(downloadDir, file.getName());
        int counter = 1;
        String baseName = file.getName();
        String extension = "";
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = baseName.substring(dotIndex);
            baseName = baseName.substring(0, dotIndex);
        }
        while (dest.exists()) {
            dest = new java.io.File(downloadDir, baseName + "_" + counter + extension);
            counter++;
        }

        String remotePath = file.getPath() + file.getName();
        List<String> lines = runClientScript("DOWNLOAD", remotePath, dest.getAbsolutePath());
        if (lines.isEmpty() || !lines.get(0).startsWith("OK|")) {
            throw new IOException(lines.isEmpty() ? "Download failed" : lines.get(0));
        }

        return dest;
    }

    private List<String> runClientScript(String command, String remotePath, String... extraArgs) throws IOException {
        List<String> processCommand = new ArrayList<>();
        processCommand.addAll(Arrays.asList(
                "bash",
                CLIENT_SCRIPT,
                target.getAddress().getHostAddress(),
                String.valueOf(target.getTcpPort()),
                command,
                remotePath
        ));
        processCommand.addAll(Arrays.asList(extraArgs));

        ProcessBuilder builder = new ProcessBuilder(processCommand);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        String output;
        try (java.io.InputStream in = process.getInputStream()) {
            output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        try {
            int exitCode = process.waitFor();
                List<String> lines = output.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            if (exitCode != 0 && lines.isEmpty()) {
                throw new IOException("Script failed with exit code " + exitCode);
            }
            return lines;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for network script", e);
        }
    }
}