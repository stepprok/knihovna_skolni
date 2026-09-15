import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Student {
    private JPanel panel;
    private JLabel nazev;
    private JScrollPane knihy;
    private JButton vyhledatBtn;
    private JButton konecBtn;

    private JList<Kniha> seznamKnihGUI;
    private DefaultListModel<Kniha> listModel;
    private List<Kniha> seznamKnih = new ArrayList<>();

    private static final String SOUBOR_KNIHY = "knihy.txt";

    public Student() {
        panel = new JPanel(new BorderLayout(10, 10));
        nazev = new JLabel("Školní knihovna - Režim Student", SwingConstants.CENTER);
        nazev.setFont(new Font("Arial", Font.BOLD, 16));

        listModel = new DefaultListModel<>();
        seznamKnihGUI = new JList<>(listModel);
        knihy = new JScrollPane(seznamKnihGUI);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        vyhledatBtn = new JButton("Vyhledat knihu");
        konecBtn = new JButton("Konec a Uložit");
        bottomPanel.add(vyhledatBtn);
        bottomPanel.add(konecBtn);

        panel.add(nazev, BorderLayout.NORTH);
        panel.add(knihy, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        nacistiKnihyZeSouboru();

        seznamKnihGUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Kniha vybranaKniha = seznamKnihGUI.getSelectedValue();
                if (vybranaKniha != null) {
                    zobrazDetailKnihy(vybranaKniha);
                }
            }
        });

        vyhledatBtn.addActionListener(e -> otvriVyhledavaciOkno());
        konecBtn.addActionListener(e -> {
            ulozKnihyDoSouboru();
            JOptionPane.showMessageDialog(null, "Data uložena. Aplikace se ukončuje.");
            System.exit(0);
        });
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
                    listModel.addElement(kniha);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Chyba při čtení: " + e.getMessage());
        }
    }

    private void ulozKnihyDoSouboru() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SOUBOR_KNIHY), StandardCharsets.UTF_8))) {
            for (Kniha kniha : seznamKnih) {
                bw.write(kniha.toTxtLine());
                bw.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Chyba při zápisu: " + e.getMessage());
        }
    }

    private void otvriVyhledavaciOkno() {
        JDialog searchDialog = new JDialog((Frame) null, "Vyhledat knihu", true);
        searchDialog.setSize(400, 300);
        searchDialog.setLocationRelativeTo(panel);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        JTextField searchField = new JTextField();
        DefaultListModel<Kniha> searchListModel = new DefaultListModel<>();
        JList<Kniha> searchList = new JList<>(searchListModel);

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

        searchPanel.add(new JLabel(" Napište název nebo autora:"), BorderLayout.NORTH);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(new JScrollPane(searchList), BorderLayout.SOUTH);

        searchDialog.add(searchPanel);
        searchDialog.setVisible(true);
    }

    private void zobrazDetailKnihy(Kniha kniha) {
        kniha.zkontrolujExpiraci(); // Aktualizace stavu podle data

        JDialog detailDialog = new JDialog((Frame) null, "Detail knihy", true);
        detailDialog.setSize(380, 260);
        detailDialog.setLocationRelativeTo(panel);
        detailDialog.setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoPanel.add(new JLabel("ID: " + kniha.getId()));
        infoPanel.add(new JLabel("Název: " + kniha.getNazev()));
        infoPanel.add(new JLabel("Autor: " + kniha.getAutor()));
        infoPanel.add(new JLabel("Rok vydání: " + kniha.getRok()));
        infoPanel.add(new JLabel("Žánr: " + kniha.getZanr()));

        JLabel stavLabel = new JLabel("Stav: " + (kniha.isRezervovano() ? "REZERVOVÁNO do " + kniha.getDatumDo() : "Dostupná"));
        stavLabel.setFont(new Font("Arial", Font.BOLD, 12));
        infoPanel.add(stavLabel);

        detailDialog.add(infoPanel, BorderLayout.CENTER);

        if (!kniha.isRezervovano()) {
            JButton rezervovatBtn = new JButton("Rezervovat knihu");
            rezervovatBtn.addActionListener(e -> {
                String vychoziDatum = LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                String zadanedatum = JOptionPane.showInputDialog(
                        detailDialog,
                        "Zadajte datum konce rezervace (dd.MM.yyyy):",
                        vychoziDatum
                );

                if (zadanedatum != null && !zadanedatum.trim().isEmpty()) {
                    kniha.rezervovat(zadanedatum.trim());
                    ulozKnihyDoSouboru();
                    JOptionPane.showMessageDialog(detailDialog, "Kniha byla rezervována do " + zadanedatum);
                    seznamKnihGUI.repaint();
                    detailDialog.dispose();
                }
            });
            detailDialog.add(rezervovatBtn, BorderLayout.SOUTH);
        }

        detailDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Student - Knihovna Školní knihovny");
            Student app = new Student();
            frame.setContentPane(app.panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}