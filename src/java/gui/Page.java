package gui;

import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import model.File;
import p2pshare.App;
import p2pshare.model.Peer;

public class Page extends JFrame {

    private App app;
    private Peer peer;
    private JTextField pathField;
    private JList<String> sideBar;
    private JTable fileTable;

    public Page(App app, Peer peer) {
        this.app = app;
        this.peer = peer;

        setTitle("Gestionnaire de Fichiers - " + peer.getName());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Ne ferme pas l'appli entière
        setLocationRelativeTo(null);

        initUI();

        update("/");
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(33, 37, 41));

        // --- 1. Barre d'outils (Haut) ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(45, 49, 54));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(70, 70, 70)));

        JButton btnBack = new JButton("⬅");
        btnBack.addActionListener(e -> {
            String path = pathField.getText();
            String parent = path.substring(0, path.lastIndexOf("/"));
            if (parent.equals("")) {
                parent = "/";
            }
            update(parent);
        });
        JButton btnHome = new JButton("🏠");
        btnHome.addActionListener(e -> {
            update("/");
        });
        pathField = new JTextField("/home/jerri/documents/");
        pathField.setPreferredSize(new Dimension(500, 25));
        pathField.setBackground(new Color(33, 37, 41));
        pathField.setForeground(Color.WHITE);
        pathField.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

        toolbar.add(btnBack);
        toolbar.add(btnHome);
        toolbar.add(pathField);

        // --- 2. Arborescence (Gauche) ---
        String[] dossiers = { "Recents", "Dossier Personnel", "Documents", "Images", "Musique", "Téléchargements", "Vidéos" };
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String d : dossiers)
            listModel.addElement("📁 " + d);

        sideBar = new JList<>(listModel);
        sideBar.setBackground(new Color(45, 49, 54));
        sideBar.setForeground(Color.WHITE);
        sideBar.setFixedCellHeight(40);
        sideBar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane sideScroll = new JScrollPane(sideBar);
        sideScroll.setPreferredSize(new Dimension(200, 0));
        sideScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(70, 70, 70)));

        // --- 3. Table des fichiers (Centre) ---
        String[] columns = { "Nom", "Taille", "Type", "Modifié le" };
        Object[][] data = {
                { "Projet_Java.zip", "12 Mo", "Archive", "Hier" },
                { "Rapport.pdf", "1.5 Mo", "PDF", "Aujourd'hui" },
                { "vacances.jpg", "4 Mo", "Image", "12/01/2026" },
                { "script.py", "2 Ko", "Python", "Il y a 1h" }
        };

        DefaultTableModel model = new DefaultTableModel(data, columns);
        fileTable = new JTable(model);
        fileTable.setBackground(new Color(33, 37, 41));
        fileTable.setForeground(Color.WHITE);
        fileTable.setGridColor(new Color(60, 60, 60));
        fileTable.setRowHeight(30);
        fileTable.getTableHeader().setBackground(new Color(45, 49, 54));
        fileTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane tableScroll = new JScrollPane(fileTable);
        tableScroll.getViewport().setBackground(new Color(33, 37, 41));
        tableScroll.setBorder(null);

        // --- Assemblage ---
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(sideScroll, BorderLayout.WEST);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        add(mainPanel);

        // --- 4. Pied de page (Footer) ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(45, 49, 54));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(70, 70, 70)));
        footer.setPreferredSize(new Dimension(0, 50));

        // Bouton Déconnexion (Gauche)
        JButton btnLogout = new JButton("⏻ Déconnexion");
        btnLogout.setForeground(new Color(255, 100, 100));
        btnLogout.setContentAreaFilled(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(new EmptyBorder(0, 20, 0, 0));

        // Bouton Supprimer
        JButton btnDelete = new JButton("Supprimer -");
        btnDelete.setBackground(new Color(253, 10, 10)); // Rouge
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> {
            int selected = fileTable.getSelectedRow();
            if (selected != -1) {
                String filename = (String) ((DefaultTableModel) fileTable.getModel()).getValueAt(selected, 0);
                String parent = pathField.getText();
                if (! parent.endsWith("/")) {
                    parent += "/";
                }
                delete(parent + filename);
            }
        });

        // Bouton Ajouter
        JButton btnAdd = new JButton("Ajouter +");
        btnAdd.setBackground(new Color(13, 110, 253)); // Bleu
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(false); // only one file
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                upload(selectedFile.getAbsolutePath());
            }
        });

        // Bouton Télécharger
        JButton btnSend = new JButton("Télécharger ➤");
        btnSend.setBackground(new Color(25, 135, 84)); // Vert
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.addActionListener(e -> {
            int selected = fileTable.getSelectedRow();
            if (selected != -1) {
                String filename = (String) ((DefaultTableModel) fileTable.getModel()).getValueAt(selected, 0);
                String parent = pathField.getText();
                if (! parent.endsWith("/")) {
                    parent += "/";
                }
                download(parent + filename);
            }
        });

        // Panel de DROITE pour regrouper Ajouter et Télécharger
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(btnDelete);
        rightPanel.add(btnAdd);
        rightPanel.add(btnSend);

        // On assemble le footer
        // footer.add(btnLogout, BorderLayout.WEST);
        footer.add(rightPanel, BorderLayout.EAST);

        // On ajoute UN SEUL footer au SUD
        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void update(String path) {
        List<File> filesAndDirs = app.listFichiers(peer, path);
        List<File> files = filesAndDirs.stream().filter(f -> f.getType().equals(File.Type.FILE)).toList();
        List<File> dirs = filesAndDirs.stream().filter(f -> f.getType().equals(File.Type.DIRECTORY)).toList();

        pathField.setText(path);

        DefaultListModel<String> dirModel = (DefaultListModel<String>) sideBar.getModel();
        dirModel.clear();
        for (File d : dirs)
            dirModel.addElement("📁 " + d.getName());

        sideBar.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = sideBar.getSelectedValue();
                if (selected != null) {
                    String parent = path;
                    if (! parent.endsWith("/")) {
                        parent += "/";
                    }
                    update(parent + selected.substring("📁 ".length()));
                }
            }
        });

        DefaultTableModel fileModel = (DefaultTableModel) fileTable.getModel();
        fileModel.setRowCount(0);
        for (File f : files)
            fileModel.addRow( new Object[] { f.getName(), "-", "-", "-" });
    }

    private void download(String path) {
        String output = app.telechargerFichier(peer, path);
        System.out.println(output);
    }

    private void upload(String source) {
        String filename = source.substring(source.lastIndexOf("/") + 1);
        String parent = pathField.getText();
        if (! parent.endsWith("/")) {
            parent += "/";
        }

        String destination = parent + filename;
        String output = app.envoyerFichier(peer, source, destination);
        System.out.println(output);

        update(pathField.getText());
    }

    private void delete(String path) {
        String output = app.supprimerFichier(peer, path);
        System.out.println(output);

        update(pathField.getText());
    }
}
