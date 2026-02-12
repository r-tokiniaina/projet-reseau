package p2pshare;

import javax.swing.SwingUtilities;
import p2pshare.gui.MainWindow;

public class MainGUI {
    public static void main(String[] args) {
        App app = App.getInstance();

        try {
            // Démarrer l'application
            System.out.println("=== Démarrage P2P File Share ===");
            app.start();

            // Afficher les informations locales
            System.out.println("\n" + app.getInfos());

            SwingUtilities.invokeLater(() -> {
                new MainWindow(app).setVisible(true);
            });
        } catch (Exception e) {
            System.err.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Arrêter proprement
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                app.stop();
                System.out.println("Application terminée.");
            }));
        }
    }
}
