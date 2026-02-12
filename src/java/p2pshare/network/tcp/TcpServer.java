package p2pshare.network.tcp;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import p2pshare.util.Logger;

public class TcpServer implements Runnable, Closeable {
    private static final String SERVER_SCRIPT = "scripts/network/p2p_tcp_server.sh";
    private volatile boolean running;

    public TcpServer() throws IOException {
        this.running = false;
        Logger.info("TcpServer wrapper ready (Bash mode)");
    }

    @Override
    public void run() {
        try {
            ScriptResult result = runScript("start");
            if (result.exitCode != 0) {
                throw new IOException(result.output);
            }
            running = true;
            Logger.info("TcpServer started via Bash script");

            while (running) {
                Thread.sleep(300);
            }
        } catch (IOException e) {
            Logger.error("Unable to start TcpServer script: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.warning("TcpServer thread interrupted");
        } finally {
            try {
                runScript("stop");
            } catch (IOException ignored) {
            }
            Logger.info("TcpServer stopped");
        }
    }

    @Override
    public void close() throws IOException {
        this.running = false;
        runScript("stop");
    }

    private ScriptResult runScript(String action) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("bash", SERVER_SCRIPT, action);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output;
        try (java.io.InputStream in = process.getInputStream()) {
            output = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
        try {
            int exitCode = process.waitFor();
            return new ScriptResult(exitCode, output);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting script", e);
        }
    }

    private static class ScriptResult {
        private final int exitCode;
        private final String output;

        private ScriptResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}