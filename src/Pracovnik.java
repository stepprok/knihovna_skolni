import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Pracovnik {
    private JPanel panel;
    private JLabel nazev;
    private JScrollPane knihy;
    private JButton rezervace;
    private JButton edit;
    private JButton ulozit;
    private JButton konec;

    private JTable tabulkaKnih;
    private DefaultTableModel tableModel;
    private List<Kniha> seznamKnih = new ArrayList<>();
    private static final String SOUBOR_KNIHY = "knihy.txt";

    public Pracovnik() {
        panel = new JPanel(new BorderLayout(10, 10));
        nazev = new JLabel("Školní knihovna - Režim Pracovník", SwingConstants.CENTER);
        nazev.setFont(new Font("Arial", Font.BOLD, 16));

        // Nastavení tabulky pro zobrazení a editaci
        String[] sloupcoveNazvy = {"ID", "Název", "Autor", "Rok", "Žánr", "Rezervováno", "Datum do"};
        tableModel = new DefaultTableModel(sloupcoveNazvy, 0) {
            private boolean isEditable = false;

            public void setEditable(boolean editable) {
                this.isEditable = editable;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // ID (sloupec 0) nelze upravovat ani v režimu editace
                return isEditable && column != 0;
            }
        };

        tabulkaKnih = new JTable(tableModel);
        knihy = new JScrollPane(tabulkaKnih);

        // Tlačítka
        rezervace = new JButton("Správa rezervací");
        edit = new JButton("Upravit (Edit)");
        ulozit = new JButton("Uložit");
        konec = new JButton("Konec");

        ulozit.setEnabled(false); // Tlačítko Uložit je výchozí zablokované

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(rezervace);
        buttonPanel.add(edit);
        buttonPanel.add(ulozit);
        buttonPanel.add(konec);

        panel.add(nazev, BorderLayout.NORTH);
        panel.add(knihy, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Načtení dat
        nacistiKnihyZeSouboru();

        // Posluchače událostí
        edit.addActionListener(e -> {
            ((DefaultTableModel) tabulkaKnih.getModel()).isCellEditable(0, 0); // Activate edit mode
            setTabulkaEditovatelna(true);
            ulozit.setEnabled(true);
            JOptionPane.showMessageDialog(panel, "Režim úprav aktivován. Můžete upravovat buňky v tabulce.");
        });

        ulozit.addActionListener(e -> {
            synchornizovatTabulkuDoSeznamu();
            ulozKnihyDoSouboru();
            setTabulkaEditovatelna(false);
            ulozit.setEnabled(false);
            JOptionPane.showMessageDialog(panel, "Změny byly úspěšně uloženy.");
        });

        rezervace.addActionListener(e -> otvriSpravuRezervaci());

        konec.addActionListener(e -> {
            ulozKnihyDoSouboru();
            System.exit(0);
        });
    }

    private void setTabulkaEditovatelna(boolean editable) {
        // Pomocná metoda pro povolení úprav v tabulce
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Název", "Autor", "Rok", "Žánr", "Rezervováno", "Datum do"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return editable && column != 0;
            }
        };

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object[] row = new Object[7];
            for (int j = 0; j < 7; j++) {
                row[j] = tableModel.getValueAt(i, j);
            }
            model.addRow(row);
        }
        tableModel = model;
        tabulkaKnih.setModel(tableModel);
    }

    private void nacistiKnihyZeSouboru() {
        seznamKnih.clear();
        tableModel.setRowCount(0);
        File file = new File(SOUBOR_KNIHY);

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String radek;
            while ((radek = br.readLine()) != null) {
                if (radek.trim().isEmpty()) continue;
                String[] c = radek.split(";");
                if (c.length >= 6) {
                    String datumDo = (c.length >= 7) ? c[6].trim() : "-";
                    Kniha kniha = new Kniha(c[0].trim(), c[1].trim(), c[2].trim(), c[3].trim(), c[4].trim(), Boolean.parseBoolean(c[5].trim()), datumDo);
                    seznamKnih.add(kniha);
                    tableModel.addRow(new Object[]{kniha.getId(), kniha.getNazev(), kniha.getAutor(), kniha.getRok(), kniha.getZanr(), kniha.isRezervovano(), kniha.getDatumDo()});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(panel, "Chyba při čtení: " + e.getMessage());
        }
    }

    private void synchornizovatTabulkuDoSeznamu() {
        seznamKnih.clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String id = tableModel.getValueAt(i, 0).toString();
            String nazev = tableModel.getValueAt(i, 1).toString();
            String autor = tableModel.getValueAt(i, 2).toString();
            String rok = tableModel.getValueAt(i, 3).toString();
            String zanr = tableModel.getValueAt(i, 4).toString();
            boolean rez = Boolean.parseBoolean(tableModel.getValueAt(i, 5).toString());
            String datum = tableModel.getValueAt(i, 6).toString();

            seznamKnih.add(new Kniha(id, nazev, autor, rok, zanr, rez, datum));
        }
    }

    private void ulozKnihyDoSouboru() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SOUBOR_KNIHY), StandardCharsets.UTF_8))) {
            for (Kniha kniha : seznamKnih) {
                bw.write(kniha.toTxtLine());
                bw.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(panel, "Chyba při zápisu: " + e.getMessage());
        }
    }

    // Okno pro správu rezervací se 2 záložkami
    private void otvriSpravuRezervaci() {
        JDialog dialog = new JDialog((Frame) null, "Správa rezervací a oběhu", true);
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(panel);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Záložka 1: Rezervované
        DefaultListModel<Kniha> modelRezervovane = new DefaultListModel<>();
        JList<Kniha> listRezervovane = new JList<>(modelRezervovane);

        // Záložka 2: Nerezervované
        DefaultListModel<Kniha> modelNerezervovane = new DefaultListModel<>();
        JList<Kniha> listNerezervovane = new JList<>(modelNerezervovane);

        obnovZalozky(modelRezervovane, modelNerezervovane);

        // Akce dvojklik na Rezervované
        listRezervovane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Kniha vybrana = listRezervovane.getSelectedValue();
                    if (vybrana != null) {
                        String[] duvody = {"Vraceno čtenářem", "Vypršela lhůta", "Zrušeno administrativně", "Kniha poškozena"};
                        String duvod = (String) JOptionPane.showInputDialog(
                                dialog,
                                "Vyberte důvod zrušení rezervace pro: " + vybrana.getNazev(),
                                "Zrušit rezervaci",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                duvody,
                                duvody[0]
                        );

                        if (duvod != null) {
                            vybrana.rezervovat("-");
                            vybrana.zkontrolujExpiraci(); // resetuje rezervováno na false
                            ulozKnihyDoSouboru();
                            nacistiKnihyZeSouboru();
                            obnovZalozky(modelRezervovane, modelNerezervovane);
                            JOptionPane.showMessageDialog(dialog, "Rezervace zrušena. Důvod: " + duvod);
                        }
                    }
                }
            }
        });

        // Akce dvojklik na Nerezervované
        listNerezervovane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Kniha vybrana = listNerezervovane.getSelectedValue();
                    if (vybrana != null) {
                        String[] moznosti = {"Zarezervovat", "Vyřadit z oběhu (Smazat)", "Zrušit"};
                        int volba = JOptionPane.showOptionDialog(
                                dialog,
                                "Co chcete udělat s knihou: " + vybrana.getNazev() + "?",
                                "Možnosti knihy",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                moznosti,
                                moznosti[0]
                        );

                        if (volba == 0) { // Zarezervovat
                            String datum = JOptionPane.showInputDialog(dialog, "Zadejte datum do kdy (dd.MM.yyyy):", "14 dní");
                            if (datum != null && !datum.isEmpty()) {
                                vybrana.rezervovat(datum);
                                ulozKnihyDoSouboru();
                                nacistiKnihyZeSouboru();
                                obnovZalozky(modelRezervovane, modelNerezervovane);
                            }
                        } else if (volba == 1) { // Vyřadit z oběhu
                            seznamKnih.remove(vybrana);
                            ulozKnihyDoSouboru();
                            nacistiKnihyZeSouboru();
                            obnovZalozky(modelRezervovane, modelNerezervovane);
                            JOptionPane.showMessageDialog(dialog, "Kniha byla vyřazena z oběhu.");
                        }
                    }
                }
            }
        });

        tabbedPane.addTab("Rezervované (Dvojklik pro zrušení)", new JScrollPane(listRezervovane));
        tabbedPane.addTab("Nerezervované (Dvojklik pro akce)", new JScrollPane(listNerezervovane));

        dialog.add(tabbedPane);
        dialog.setVisible(true);
    }

    private void obnovZalozky(DefaultListModel<Kniha> resModel, DefaultListModel<Kniha> noResModel) {
        resModel.clear();
        noResModel.clear();
        for (Kniha k : seznamKnih) {
            k.zkontrolujExpiraci();
            if (k.isRezervovano()) {
                resModel.addElement(k);
            } else {
                noResModel.addElement(k);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pracovník - Knihovna");
            Pracovnik app = new Pracovnik();
            frame.setContentPane(app.panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 450);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}