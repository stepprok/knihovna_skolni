import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Student {
    private JPanel panel;
    private JLabel nazev;
    private JScrollPane knihy;
    private JButton vyhledatBtn;
    private JButton konecBtn;
    private JComboBox<String> raditCombo;

    private JList<Kniha> seznamKnihGUI;
    private DefaultListModel<Kniha> listModel;
    private List<Kniha> seznamKnih = new ArrayList<>();

    private static final String SOUBOR_KNIHY = "knihy.txt";

    // Barevná paleta
    private static final Color BARVA_POZADI = new Color(245, 247, 250);
    private static final Color BARVA_HLAVICKA = new Color(41, 128, 185);
    private static final Color BARVA_TLACITKO = new Color(52, 152, 219);
    private static final Color BARVA_TEXT_SVETLY = Color.WHITE;

    public Student() {
        nastavLookAndFeel();

        panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BARVA_POZADI);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Horní panel s názvem
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BARVA_HLAVICKA);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        nazev = new JLabel("Školní knihovna — Režim Student", SwingConstants.LEFT);
        nazev.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nazev.setForeground(BARVA_TEXT_SVETLY);
        nazev.setIcon(vytvorIkonuKnihy(28, 28, BARVA_TEXT_SVETLY));
        nazev.setIconTextGap(12);
        headerPanel.add(nazev, BorderLayout.CENTER);

        // Panel s možností řazení
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setOpaque(false);

        JLabel raditLabel = new JLabel("Seřadit podle:");
        raditLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        String[] moznostiRazeni = {"Názvu (A-Z)", "Autora (A-Z)", "Roku vydání (nejnovější)", "Roku vydání (nejstarší)"};
        raditCombo = new JComboBox<>(moznostiRazeni);
        raditCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        raditCombo.addActionListener(e -> seradKnihy());

        controlPanel.add(raditLabel);
        controlPanel.add(raditCombo);

        // Seznam knih s vlastním rendererem karet
        listModel = new DefaultListModel<>();
        seznamKnihGUI = new JList<>(listModel);
        seznamKnihGUI.setCellRenderer(new KnihaKartaRenderer());
        seznamKnihGUI.setBackground(BARVA_POZADI);
        seznamKnihGUI.setSelectionBackground(new Color(220, 235, 252));
        seznamKnihGUI.setFixedCellHeight(65);

        knihy = new JScrollPane(seznamKnihGUI);
        knihy.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220), 1));

        // Středový panel sdružující řazení a seznam
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setOpaque(false);
        centerPanel.add(controlPanel, BorderLayout.NORTH);
        centerPanel.add(knihy, BorderLayout.CENTER);

        // Spodní tlačítkový panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        vyhledatBtn = vytvorStyloveTlacitko("Vyhledat knihu", new Color(46, 204, 113));
        konecBtn = vytvorStyloveTlacitko("Konec a Uložit", new Color(231, 76, 60));

        bottomPanel.add(vyhledatBtn);
        bottomPanel.add(konecBtn);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        nacistiKnihyZeSouboru();

        seznamKnihGUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 || e.getClickCount() == 1) {
                    Kniha vybranaKniha = seznamKnihGUI.getSelectedValue();
                    if (vybranaKniha != null) {
                        zobrazDetailKnihy(vybranaKniha);
                    }
                }
            }
        });

        vyhledatBtn.addActionListener(e -> otvriVyhledavaciOkno());
        konecBtn.addActionListener(e -> {
            ulozKnihyDoSouboru();
            JOptionPane.showMessageDialog(null, "Data byla úspěšně uložena.", "Uloženo", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        });
    }

    private void seradKnihy() {
        int vybranyIndex = raditCombo.getSelectedIndex();
        switch (vybranyIndex) {
            case 0: // Názvu (A-Z)
                seznamKnih.sort(Comparator.comparing(Kniha::getNazev, String.CASE_INSENSITIVE_ORDER));
                break;
            case 1: // Autora (A-Z)
                seznamKnih.sort(Comparator.comparing(Kniha::getAutor, String.CASE_INSENSITIVE_ORDER));
                break;
            case 2: // Roku vydání (nejnovější)
                seznamKnih.sort((k1, k2) -> Integer.compare(parseRok(k2.getRok()), parseRok(k1.getRok())));
                break;
            case 3: // Roku vydání (nejstarší)
                seznamKnih.sort((k1, k2) -> Integer.compare(parseRok(k1.getRok()), parseRok(k2.getRok())));
                break;
        }

        listModel.clear();
        for (Kniha k : seznamKnih) {
            listModel.addElement(k);
        }
    }

    private int parseRok(String rokStr) {
        try {
            return Integer.parseInt(rokStr.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void nastavLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private JButton vytvorStyloveTlacitko(String text, Color barva) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(barva);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private void nacistiKnihyZeSouboru() {
        seznamKnih.clear();
        listModel.clear();
        File file = new File(SOUBOR_KNIHY);

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String radek;
            while ((radek = br.readLine()) != null) {
                if (radek.trim().isEmpty()) continue;
                String[] casti = radek.split(";");
                if (casti.length >= 6) {
                    String datumDo = (casti.length >= 7) ? casti[6].trim() : "-";
                    Kniha kniha = new Kniha(
                            casti[0].trim(),
                            casti[1].trim(),
                            casti[2].trim(),
                            casti[3].trim(),
                            casti[4].trim(),
                            Boolean.parseBoolean(casti[5].trim()),
                            datumDo
                    );
                    seznamKnih.add(kniha);
                }
            }
            seradKnihy(); // Po načtení se automaticky seřadí podle výchozího nastavení
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Chyba při čtení: " + e.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ulozKnihyDoSouboru() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SOUBOR_KNIHY), StandardCharsets.UTF_8))) {
            for (Kniha kniha : seznamKnih) {
                bw.write(kniha.toTxtLine());
                bw.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Chyba při zápisu: " + e.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void otvriVyhledavaciOkno() {
        JDialog searchDialog = new JDialog((Frame) null, "Vyhledávání v katalogu", true);
        searchDialog.setSize(480, 400);
        searchDialog.setLocationRelativeTo(panel);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        searchPanel.setBackground(BARVA_POZADI);

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        DefaultListModel<Kniha> searchListModel = new DefaultListModel<>();
        JList<Kniha> searchList = new JList<>(searchListModel);
        searchList.setCellRenderer(new KnihaKartaRenderer());
        searchList.setFixedCellHeight(65);

        for (Kniha k : seznamKnih) searchListModel.addElement(k);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String text = searchField.getText().toLowerCase();
                searchListModel.clear();
                for (Kniha k : seznamKnih) {
                    if (k.getNazev().toLowerCase().contains(text) || k.getAutor().toLowerCase().contains(text)) {
                        searchListModel.addElement(k);
                    }
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });

        searchList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Kniha vybrana = searchList.getSelectedValue();
                if (vybrana != null) {
                    zobrazDetailKnihy(vybrana);
                    searchList.repaint();
                    seznamKnihGUI.repaint();
                }
            }
        });

        JPanel topSearch = new JPanel(new BorderLayout(5, 5));
        topSearch.setOpaque(false);
        JLabel label = new JLabel("Hledat (název nebo autor):");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        topSearch.add(label, BorderLayout.NORTH);
        topSearch.add(searchField, BorderLayout.CENTER);

        searchPanel.add(topSearch, BorderLayout.NORTH);
        searchPanel.add(new JScrollPane(searchList), BorderLayout.CENTER);

        searchDialog.add(searchPanel);
        searchDialog.setVisible(true);
    }

    private void zobrazDetailKnihy(Kniha kniha) {
        kniha.zkontrolujExpiraci();

        JDialog detailDialog = new JDialog((Frame) null, "Detail knihy", true);
        detailDialog.setSize(450, 320);
        detailDialog.setLocationRelativeTo(panel);
        detailDialog.setLayout(new BorderLayout());

        JPanel cardPanel = new JPanel(new BorderLayout(15, 15));
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        cardPanel.setBackground(Color.WHITE);

        JLabel iconLabel = new JLabel(vytvorIkonuKnihy(64, 64, BARVA_HLAVICKA));
        cardPanel.add(iconLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("<html><b>" + kniha.getNazev() + "</b></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel autorLabel = new JLabel("Autor: " + kniha.getAutor());
        autorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel zanrLabel = new JLabel("Žánr: " + kniha.getZanr() + " (" + kniha.getRok() + ")");
        zanrLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        zanrLabel.setForeground(Color.GRAY);

        JLabel idLabel = new JLabel("Katalogové ID: " + kniha.getId());
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        idLabel.setForeground(Color.LIGHT_GRAY);

        boolean jeRezervovano = kniha.isRezervovano();
        JLabel stavLabel = new JLabel(jeRezervovano ? "REZERVOVÁNO do " + kniha.getDatumDo() : "Dostupná k vypůjčení");
        stavLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        stavLabel.setForeground(jeRezervovano ? new Color(231, 76, 60) : new Color(39, 174, 96));

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(autorLabel);
        infoPanel.add(zanrLabel);
        infoPanel.add(idLabel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(stavLabel);

        cardPanel.add(infoPanel, BorderLayout.CENTER);

        if (!jeRezervovano) {
            JButton rezervovatBtn = vytvorStyloveTlacitko("Rezervovat knihu", BARVA_TLACITKO);
            rezervovatBtn.addActionListener(e -> {
                String vychoziDatum = LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                String zadanedatum = JOptionPane.showInputDialog(
                        detailDialog,
                        "Zadejte datum konce rezervace (dd.MM.yyyy):",
                        vychoziDatum
                );

                if (zadanedatum != null && !zadanedatum.trim().isEmpty()) {
                    kniha.rezervovat(zadanedatum.trim());
                    ulozKnihyDoSouboru();
                    JOptionPane.showMessageDialog(detailDialog, "Kniha byla rezervována do " + zadanedatum, "Úspěch", JOptionPane.INFORMATION_MESSAGE);
                    seznamKnihGUI.repaint();
                    detailDialog.dispose();
                }
            });

            JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnWrapper.setBackground(Color.WHITE);
            btnWrapper.add(rezervovatBtn);
            cardPanel.add(btnWrapper, BorderLayout.SOUTH);
        }

        detailDialog.add(cardPanel);
        detailDialog.setVisible(true);
    }

    private ImageIcon vytvorIkonuKnihy(int width, int height, Color color) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        g2.fillRoundRect(2, 2, width - 4, height - 4, 8, 8);
        g2.setColor(Color.WHITE);
        g2.fillRect(6, 6, width - 12, 3);
        g2.fillRect(6, 12, width - 18, 2);
        g2.fillRect(6, 17, width - 14, 2);

        g2.dispose();
        return new ImageIcon(img);
    }

    private class KnihaKartaRenderer extends JPanel implements ListCellRenderer<Kniha> {
        private JLabel iconLabel = new JLabel();
        private JLabel nazevLabel = new JLabel();
        private JLabel autorLabel = new JLabel();
        private JLabel stavLabel = new JLabel();

        public KnihaKartaRenderer() {
            setLayout(new BorderLayout(10, 0));
            setBorder(new EmptyBorder(5, 10, 5, 10));

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);

            nazevLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            autorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            autorLabel.setForeground(Color.GRAY);

            textPanel.add(nazevLabel);
            textPanel.add(autorLabel);

            stavLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));

            add(iconLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
            add(stavLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Kniha> list, Kniha kniha, int index, boolean isSelected, boolean cellHasFocus) {
            nazevLabel.setText(kniha.getNazev());
            autorLabel.setText(kniha.getAutor() + " (" + kniha.getRok() + ") • " + kniha.getZanr());

            if (kniha.isRezervovano()) {
                stavLabel.setText("Obsazeno");
                stavLabel.setForeground(new Color(231, 76, 60));
                iconLabel.setIcon(vytvorIkonuKnihy(24, 24, new Color(231, 76, 60)));
            } else {
                stavLabel.setText("Dostupná");
                stavLabel.setForeground(new Color(39, 174, 96));
                iconLabel.setIcon(vytvorIkonuKnihy(24, 24, new Color(39, 174, 96)));
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(index % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
            }

            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Student — Knihovna");
            Student app = new Student();
            frame.setContentPane(app.panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(650, 550);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}