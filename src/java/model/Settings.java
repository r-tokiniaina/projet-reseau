package model;

import java.io.File;

public class Settings {
    private static Settings instance = null;
    private int serverPort;
    private File uploadDir;

    private Settings() {}

    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
            // MODIFICATION ICI : Utiliser AppConfig.TCP_PORT au lieu de 18003 en dur
            instance.setServerPort(p2pshare.config.AppConfig.TCP_PORT);
            File uploadDir = p2pshare.config.AppConfig.UPLOAD_DIR;
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            instance.setUploadDir(uploadDir);
        }
        return instance;
    }

    public synchronized int getServerPort() {
        return serverPort;
    }

    public synchronized void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public synchronized File getUploadDir() {
        return uploadDir;
    }

    public synchronized void setUploadDir(File dir) {
        this.uploadDir = dir;
    }
}
