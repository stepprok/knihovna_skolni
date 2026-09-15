import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Hlavni {
    JPanel panel;
    private JLabel nazev;

    public static void main(String[] args) throws IOException {
        zobrazPrihlasovaciOkno();
    }

    private static void zobrazPrihlasovaciOkno() {
        String[] moznosti = {"Student", "Pracovník"};
        JComboBox<String> dropdown = new JComboBox<>(moznosti);

        JLabel hesloLabel = new JLabel("Heslo:");
        JPasswordField hesloField = new JPasswordField();

        // Výchozí stav: pro Studenta jsou prvky hesla skryté
        hesloLabel.setVisible(false);
        hesloField.setVisible(false);

        // Dynamické skrývání/zobrazování podle výběru v dropdownu
        dropdown.addActionListener(e -> {
            boolean jePracovnik = "Pracovník".equals(dropdown.getSelectedItem());
            hesloLabel.setVisible(jePracovnik);
            hesloField.setVisible(jePracovnik);

            // Revalidace dialogu, aby se správně přizpůsobil velikosti
            Window window = SwingUtilities.getWindowAncestor(dropdown);
            if (window != null) {
                window.pack();
            }
        });

        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        dialogPanel.add(new JLabel("Vyberte roli:"));
        dialogPanel.add(dropdown);
        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(hesloLabel);
        dialogPanel.add(hesloField);

        int vysledek = JOptionPane.showConfirmDialog(
                null,
                dialogPanel,
                "Přihlášení do systému",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (vysledek == JOptionPane.OK_OPTION) {
            String vybranaRole = (String) dropdown.getSelectedItem();

            if ("Student".equals(vybranaRole)) {
                student();
            } else if ("Pracovník".equals(vybranaRole)) {
                String zadaneHeslo = new String(hesloField.getPassword());
                if ("admin".equals(zadaneHeslo)) {
                    pracovnik();
                } else {
                    JOptionPane.showMessageDialog(null, "Nesprávné heslo!", "Chyba", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            }
        } else {
            System.exit(0);
        }
    }

    // Student
    public static void student() {
        JOptionPane.showMessageDialog(null, "Vítejte v režimu Student.");
        SwingUtilities.invokeLater(() -> {
            Student.main(new String[]{});
        });
    }

    // Pracovník
    public static void pracovnik() {
        JOptionPane.showMessageDialog(null, "Vítejte v režimu Pracovník.");
        SwingUtilities.invokeLater(() -> {
            Pracovnik.main(new String[]{});
        });
    }
}