package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import p2pshare.App;
import p2pshare.model.Peer;

public class Fenetre extends JFrame {

    private App app;
    private JTextField searchField;
    private JPanel listPanel;
    private JProgressBar loadingBar;
    private Timer stopLoadingTimer;

    public Fenetre(App app) {
        this.app = app;

        setTitle("Réseaux");
        setSize(350, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(33, 37, 41));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. CRÉER LE HEADER D'ABORD
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        searchField.setBackground(new Color(45, 49, 54));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(false);
        loadingBar.setPreferredSize(new Dimension(0, 3));
        loadingBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        loadingBar.setBorderPainted(false);
        loadingBar.setBackground(new Color(33, 37, 41));
        loadingBar.setForeground(new Color(13, 110, 253));

        // 2. AJOUTER AU HEADER
        headerPanel.add(searchField);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        headerPanel.add(loadingBar);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 3. ÉCOUTEUR UNIQUE ET PROPRE
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                startSearchAnimation();
                updateList(searchField.getText());
            }
        });

        // --- Liste des Réseaux ---
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(33, 37, 41));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(33, 37, 41));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Rafraîchir la liste des réseaux toutes les 3 secondes
        new Timer(3000, e -> updateList(searchField.getText())).start();
        add(mainPanel);
    }

    // MÉTHODE POUR L'ANIMATION DE CHARGEMENT
    private void startSearchAnimation() {
        loadingBar.setIndeterminate(true);
        if (stopLoadingTimer != null && stopLoadingTimer.isRunning()) {
            stopLoadingTimer.stop();
        }
        stopLoadingTimer = new Timer(500, e -> loadingBar.setIndeterminate(false));
        stopLoadingTimer.setRepeats(false);
        stopLoadingTimer.start();
    }

    private void updateList(String query) {
        listPanel.removeAll();
        for (Peer peer : app.getPeers()) {
            if (peer.getName().toLowerCase().contains(query.toLowerCase())) {
                listPanel.add(createNetworkItem(peer));
                listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createNetworkItem(Peer peer) {
        JPanel item = new JPanel(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        item.setBackground(new Color(45, 49, 54));
        item.setBorder(new EmptyBorder(10, 15, 10, 15));

        // --- Nom du réseau ---
        JLabel nameLabel = new JLabel(peer.getName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        item.add(nameLabel, BorderLayout.WEST);

        // --- Panel d'actions ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        // Bouton Paramètres
        JButton settingsBtn = new JButton("⚙");
        settingsBtn.setForeground(new Color(200, 200, 200));
        settingsBtn.setContentAreaFilled(false);
        settingsBtn.setBorderPainted(false);
        settingsBtn.setFocusPainted(false);
        settingsBtn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        settingsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- CRÉATION DU MENU POPUP ---
        JPopupMenu settingsMenu = new JPopupMenu();
        // Style sombre pour le menu (optionnel)
        settingsMenu.setBackground(new Color(55, 59, 64));
        settingsMenu.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));

        JMenuItem itemPseudo = new JMenuItem("Pseudo");
        JMenuItem itemConnect = new JMenuItem("Se Connecter");
        JMenuItem itemDisconnect = new JMenuItem("Déconnecter");

        // Style des items
        itemConnect.setForeground(Color.BLACK); // Note: Swing gère parfois mal le dark sur les menus par défaut
        itemDisconnect.setForeground(Color.BLACK);

        // Actions des boutons du menu
        itemConnect.addActionListener(e -> {
            System.out.println("Connexion à : " + peer.getName());
            new Page(app, peer).setVisible(true);
        });

        itemDisconnect.addActionListener(e -> {
            System.out.println("Déconnexion de : " + peer.getName());
        });

        settingsMenu.add(itemConnect);

        // Afficher le menu au clic sur l'engrenage
        settingsBtn.addActionListener(e -> {
            settingsMenu.show(settingsBtn, 0, settingsBtn.getHeight());
        });

        actionPanel.add(settingsBtn);
        item.add(actionPanel, BorderLayout.EAST);

        return item;
    }
}
