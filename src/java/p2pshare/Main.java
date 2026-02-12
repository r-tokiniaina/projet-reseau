package p2pshare;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        App app = App.getInstance();
        
        try {
            // Démarrer l'application
            System.out.println("=== Démarrage P2P File Share ===");
            app.start();
            
            // Afficher les informations locales
            System.out.println("\n" + app.getInfos());
            System.out.println("Attente de la découverte d'autres pairs...\n");
            
            boolean running = true;
            
            while (running) {
                System.out.println("=== MENU PRINCIPAL ===");
                System.out.println("1. Lister les pairs disponibles");
                System.out.println("2. Lister fichiers d'un pair");
                System.out.println("3. Envoyer fichier à un pair");
                System.out.println("4. Télécharger fichier d'un pair");
                System.out.println("5. Infos locales");
                System.out.println("6. Quitter");
                System.out.print("Choix: ");
                
                String choix = scanner.nextLine().trim();
                
                switch (choix) {
                    case "1":
                        System.out.println("\n" + app.listerPairs());
                        break;
                        
                    case "2":
                        System.out.println("\n" + app.listerPairs());
                        if (!app.getPeers().isEmpty()) {
                            System.out.print("IP du pair: ");
                            String ipListe = scanner.nextLine().trim();
                            String cheminListe = "/";
                            System.out.println("\n" + app.listerFichiers(ipListe, cheminListe));
                        } else {
                            System.out.println("Aucun pair disponible.");
                        }
                        break;
                        
                    case "3":
                        System.out.println("\n" + app.listerPairs());
                        if (!app.getPeers().isEmpty()) {
                            System.out.print("IP du destinataire: ");
                            String ipDest = scanner.nextLine().trim();
                            System.out.print("Chemin du fichier local: ");
                            String source = scanner.nextLine().trim();
                            System.out.print("Chemin de destination (ex: /fichier.txt): ");
                            String dest = scanner.nextLine().trim();
                            System.out.println("\n" + app.envoyerFichier(ipDest, source, dest));
                        } else {
                            System.out.println("Aucun pair disponible.");
                        }
                        break;
                        
                    case "4":
                        System.out.println("\n" + app.listerPairs());
                        if (!app.getPeers().isEmpty()) {
                            System.out.print("IP du pair source: ");
                            String ipSource = scanner.nextLine().trim();
                            System.out.print("Chemin du fichier distant (ex: /fichier.txt): ");
                            String fichierDistant = scanner.nextLine().trim();
                            System.out.println("\n" + app.telechargerFichier(ipSource, fichierDistant));
                        } else {
                            System.out.println("Aucun pair disponible.");
                        }
                        break;
                        
                    case "5":
                        System.out.println("\n" + app.getInfos());
                        break;
                        
                    case "6":
                        running = false;
                        System.out.println("Fermeture en cours...");
                        break;
                        
                    default:
                        System.out.println("Choix invalide. Veuillez choisir 1-6.");
                }
                
                if (running && !choix.equals("5")) {
                    System.out.print("\nAppuyez sur Entrée pour continuer...");
                    scanner.nextLine();
                }
            }
            
        } catch (Exception e) {
            System.err.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Arrêter proprement
            app.stop();
            scanner.close();
            System.out.println("Application terminée.");
        }
    }
}