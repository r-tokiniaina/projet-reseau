package p2pshare;

import p2pshare.config.AppConfig;
import p2pshare.network.udp.UdpDiscoveryService;
import p2pshare.model.Peer;
import networking.AppServer;
import networking.AppClient;
import model.File;

import java.util.List;
import java.util.ArrayList;

public class App {
    private UdpDiscoveryService udpService;
    private AppServer tcpServer;
    private Thread serverThread;
    private boolean isRunning;
    private static App instance;

    public static synchronized App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    private App() {
        this.isRunning = false;
    }

    public void start() throws Exception {
        if (isRunning) return;

        System.out.println("=== Starting P2P File Share ===");

        // Démarrer UDP Discovery
        udpService = new UdpDiscoveryService();
        udpService.start();
        System.out.println("✓ UDP Discovery started (port: " + p2pshare.config.AppConfig.UDP_PORT + ")");

        // Démarrer TCP Server
        tcpServer = new AppServer();
        serverThread = new Thread(tcpServer);
        serverThread.start();
        System.out.println("✓ TCP Server started (port: " + p2pshare.config.AppConfig.TCP_PORT + ")");

        // Attendre un peu pour que le serveur démarre
        Thread.sleep(1000);

        isRunning = true;
        System.out.println("✓ Application ready! Waiting for peers...");
        System.out.println("   (Scanning network silently...)");
    }

    public void stop() {
        if (!isRunning) return;

        System.out.println("\n=== Stopping application ===");

        // Arrêter UDP
        if (udpService != null) {
            udpService.stop();
            System.out.println("✓ UDP Discovery stopped");
        }

        // Arrêter TCP
        if (tcpServer != null) {
            try {
                tcpServer.close();
                System.out.println("✓ TCP Server stopped");
            } catch (Exception e) {
                System.err.println("Error stopping TCP server: " + e.getMessage());
            }
        }

        // Attendre le thread
        if (serverThread != null && serverThread.isAlive()) {
            try {
                serverThread.join(2000);
            } catch (InterruptedException e) {
                System.err.println("Error joining server thread: " + e.getMessage());
            }
        }

        isRunning = false;
        System.out.println("✓ Application stopped");
    }

    // Méthode utilitaire pour convertir entre les deux types de Peer
    private model.Peer convertPeer(p2pshare.model.Peer udpPeer) {
        return new model.Peer(udpPeer.getName(), udpPeer.getAddress(), udpPeer.getTcpPort());
    }

    public String listerPairs() {
        List<Peer> peers = udpService.getPeers();

        if (peers.isEmpty()) {
            return "No peers available. Make sure both PCs are on the same network.\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== AVAILABLE PEERS (").append(peers.size()).append(") ===\n");

        for (int i = 0; i < peers.size(); i++) {
            Peer peer = peers.get(i);
            sb.append(i + 1).append(". ").append(peer.getName())
              .append(" - IP: ").append(peer.getAddress().getHostAddress())
              .append(":").append(peer.getTcpPort()).append("\n");
        }

        return sb.toString();
    }

    public List<Peer> getPeers() {
        return udpService != null ? udpService.getPeers() : new ArrayList<>();
    }

    public Peer getPeerByIP(String ip) {
        for (Peer peer : getPeers()) {
            if (peer.getAddress().getHostAddress().equals(ip)) {
                return peer;
            }
        }
        return null;
    }

    public String listerFichiers(String ip, String chemin) {
        Peer peer = getPeerByIP(ip);
        if (peer == null) {
            return "Error: Peer not found with IP: " + ip + "\n";
        }

        // Normaliser le chemin
        if (!chemin.startsWith("/")) {
            chemin = "/" + chemin;
        }
        if (!chemin.endsWith("/") && !chemin.isEmpty()) {
            chemin = chemin + "/";
        }

        try (AppClient client = new AppClient(convertPeer(peer))) {
            File dossier = new File(chemin, File.Type.DIRECTORY);
            List<File> fichiers = client.sendListRequest(dossier);

            if (fichiers.isEmpty()) {
                return "Empty directory: " + chemin + "\n";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== FILES ON ").append(ip).append(":").append(chemin).append(" ===\n");

            for (File fichier : fichiers) {
                String type = fichier.getType() == File.Type.FILE ? "[FILE]" : "[DIR]";
                sb.append(type).append(" ").append(fichier.getName()).append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\n";
        }
    }

    public List<File> listFichiers(Peer peer, String chemin) {
        try (AppClient client = new AppClient(convertPeer(peer))) {
            File dossier = new File(chemin, File.Type.DIRECTORY);
            List<File> fichiers = client.sendListRequest(dossier);
            return fichiers;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public String supprimerFichier(Peer peer, String chemin) {
        try (AppClient client = new AppClient(convertPeer(peer))) {
            File fichier = new File(chemin, File.Type.FILE);
            client.sendDeleteRequest(fichier);
            return "Success: File " + peer.getAddress() + " -> " + fichier + " deleted\n";
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\n";
        }
    }

    public String envoyerFichier(String ip, String cheminSource, String cheminDestination) {
        Peer peer = getPeerByIP(ip);
        if (peer == null) {
            return "Error: Peer not found with IP: " + ip + "\n";
        }

        java.io.File fichierSource = new java.io.File(cheminSource);
        if (!fichierSource.exists()) {
            return "Error: Source file not found: " + cheminSource + "\n";
        }

        if (!cheminDestination.startsWith("/")) {
            cheminDestination = "/" + cheminDestination;
        }

        try (AppClient client = new AppClient(convertPeer(peer))) {
            File dest = new File(cheminDestination, File.Type.FILE);
            client.sendUploadRequest(fichierSource, dest);
            return "Success: File sent to " + ip + " -> " + cheminDestination + "\n";
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\n";
        }
    }

    public String envoyerFichier(Peer peer, String cheminSource, String cheminDestination) {
        java.io.File fichierSource = new java.io.File(cheminSource);
        if (!fichierSource.exists()) {
            return "Error: Source file not found: " + cheminSource + "\n";
        }

        if (!cheminDestination.startsWith("/")) {
            cheminDestination = "/" + cheminDestination;
        }

        try (AppClient client = new AppClient(convertPeer(peer))) {
            File dest = new File(cheminDestination, File.Type.FILE);
            client.sendUploadRequest(fichierSource, dest);
            return "Success: File sent to " + peer.getAddress() + " -> " + cheminDestination + "\n";
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\n";
        }
    }

    public String telechargerFichier(String ip, String cheminFichier) {
        Peer peer = getPeerByIP(ip);
        if (peer == null) {
            return "Error: Peer not found with IP: " + ip + "\n";
        }

        if (!cheminFichier.startsWith("/")) {
            cheminFichier = "/" + cheminFichier;
        }

        try (AppClient client = new AppClient(convertPeer(peer))) {
            int lastSlash = cheminFichier.lastIndexOf("/");
            String chemin = cheminFichier.substring(0, lastSlash + 1);
            String nomFichier = cheminFichier.substring(lastSlash + 1);

            File fichierDistant = new File(chemin + nomFichier, File.Type.FILE);
            java.io.File fichierLocal = client.sendDownloadRequest(fichierDistant);

            if (fichierLocal != null && fichierLocal.exists()) {
                return "Success: Downloaded to: " + fichierLocal.getAbsolutePath() + "\n";
            } else {
                return "Error: Download failed\n";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\n";
        }
    }

    public String telechargerFichier(Peer peer, String cheminFichier) {
        try (AppClient client = new AppClient(convertPeer(peer))) {
            int lastSlash = cheminFichier.lastIndexOf("/");
            String chemin = cheminFichier.substring(0, lastSlash + 1);
            String nomFichier = cheminFichier.substring(lastSlash + 1);

            File fichierDistant = new File(chemin + nomFichier, File.Type.FILE);
            java.io.File fichierLocal = client.sendDownloadRequest(fichierDistant);

            if (fichierLocal != null && fichierLocal.exists()) {
                return "Success: Downloaded to: " + fichierLocal.getAbsolutePath() + "\n";
            } else {
                return "Error: Download failed\n";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\n";
        }
    }

    public String getInfos() {
        return "=== LOCAL INFO ===\n" +
               "Name: " + AppConfig.PC_NAME + "\n" +
               "TCP Port: " + AppConfig.TCP_PORT + "\n" +
               "UDP Port: " + AppConfig.UDP_PORT + "\n" +
               "Upload Dir: " + AppConfig.UPLOAD_DIR.getAbsolutePath() + "\n" +
               "Download Dir: " + AppConfig.DOWNLOAD_DIR.getAbsolutePath() + "\n";
    }

    public boolean isRunning() {
        return isRunning;
    }
}
