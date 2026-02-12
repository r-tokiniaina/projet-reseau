package p2pshare.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class AppConfig {
    public static final int UDP_PORT; // = 8888;
    public static final int TCP_PORT; // = 18003;
    public static final String PC_NAME; // = System.getProperty("user.name") + "-PC";
    public static final File UPLOAD_DIR; // = new File(System.getProperty("user.home"), "p2p_shared");
    public static final File DOWNLOAD_DIR; // = new File(System.getProperty("user.home"), "Téléchargements");



    static {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("app.conf")) {
            props.load(fis);

            UDP_PORT = Integer.parseInt(props.getProperty("udp_port"));
            TCP_PORT = Integer.parseInt(props.getProperty("tcp_port"));
            PC_NAME = props.getProperty("pc_name");
            UPLOAD_DIR = new File(props.getProperty("upload_dir"));
            DOWNLOAD_DIR = new File(props.getProperty("download_dir"));
        } catch (Exception e) {
            throw new RuntimeException("Cannot load app.conf", e);
        }
    }
}
