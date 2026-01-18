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
            // TODO: enregistrer ces données dans un fichier de configuration
            instance.setServerPort(18003);
            instance.setUploadDir(new File(System.getProperty("user.home")));
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
