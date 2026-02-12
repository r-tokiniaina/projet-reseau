package p2pshare.network.udp;

import p2pshare.model.Peer;

import p2pshare.config.AppConfig;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class UdpDiscoveryService {
    private static final String DISCOVERY_SCRIPT = "scripts/network/p2p_udp_discovery.sh";
    private final List<Peer> peers = new CopyOnWriteArrayList<>();
    private volatile boolean running = false;
    private Thread refreshThread;

    public UdpDiscoveryService() {
    }

    public void start() {
        if (running) return;

        try {
            runScript("start");
            running = true;
            refreshThread = new Thread(this::refreshPeersLoop, "UDP-Peers-Refresh");
            refreshThread.setDaemon(true);
            refreshThread.start();
        } catch (IOException e) {
            throw new RuntimeException("Unable to start UDP discovery script", e);
        }
    }

    public void stop() {
        running = false;
        try {
            runScript("stop");
        } catch (IOException ignored) {
        }
        if (refreshThread != null) {
            refreshThread.interrupt();
        }
    }

    private void refreshPeersLoop() {
        while (running) {
            try {
                List<String> lines = runScript("peers");
                updatePeers(lines);
                Thread.sleep(2500);
            } catch (Exception ignored) {
            }
        }
    }

    private void updatePeers(List<String> lines) {
        List<Peer> updated = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("\\|", 3);
            if (parts.length < 3) {
                continue;
            }
            String name = parts[0];
            String ip = parts[1];
            int tcpPort;
            try {
                tcpPort = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                continue;
            }
            if (name.equals(AppConfig.PC_NAME)) {
                continue;
            }
            try {
                updated.add(new Peer(name, InetAddress.getByName(ip), tcpPort));
            } catch (Exception ignored) {
            }
        }

        peers.clear();
        peers.addAll(updated);
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public boolean isRunning() {
        return running;
    }

    private List<String> runScript(String action) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("bash", DISCOVERY_SCRIPT, action);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output;
        try (java.io.InputStream in = process.getInputStream()) {
            output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        int exit;
        try {
            exit = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for discovery script", e);
        }
        if (exit != 0) {
            throw new IOException(output.isBlank() ? "Discovery script failed" : output.trim());
        }
        return output.lines().collect(Collectors.toList());
    }
}
