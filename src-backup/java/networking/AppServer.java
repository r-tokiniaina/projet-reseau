package networking;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import logging.Log;

public class AppServer implements Runnable, Closeable {
    private static final String SERVER_SCRIPT = "scripts/network/p2p_tcp_server.sh";
    private volatile boolean running;

    public AppServer() throws IOException {
        this.running = false;
        Log.info("AppServer wrapper ready (Bash mode)");
    }

    @Override
    public void run() {
        try {
            ListResult result = runScript("start");
            if (result.exitCode != 0) {
                throw new IOException(result.output);
            }
            running = true;
            Log.info("AppServer started via Bash script");

            while (running) {
                Thread.sleep(300);
            }
        } catch (IOException e) {
            Log.error("Unable to start AppServer script: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.warning("AppServer thread interrupted");
        } finally {
            try {
                runScript("stop");
            } catch (IOException ignored) {
            }
            Log.info("AppServer stopped");
        }
    }

    @Override
    public void close() throws IOException {
        this.running = false;
        runScript("stop");
    }

    private ListResult runScript(String action) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("bash", SERVER_SCRIPT, action);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output;
        try (java.io.InputStream in = process.getInputStream()) {
            output = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
        try {
            int exitCode = process.waitFor();
            return new ListResult(exitCode, output);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting script", e);
        }
    }

    private static class ListResult {
        private final int exitCode;
        private final String output;

        private ListResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
