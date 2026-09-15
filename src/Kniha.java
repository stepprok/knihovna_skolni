import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Kniha {
    private String id;
    private String nazev;
    private String autor;
    private String rok;
    private String zanr;
    private boolean rezervovano;
    private String datumDo; // Datum ve formátu dd.MM.yyyy

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Kniha(String id, String nazev, String autor, String rok, String zanr, boolean rezervovano, String datumDo) {
        this.id = id;
        this.nazev = nazev;
        this.autor = autor;
        this.rok = rok;
        this.zanr = zanr;
        this.rezervovano = rezervovano;
        this.datumDo = (datumDo == null || datumDo.isEmpty()) ? "-" : datumDo;

        // Kontrola, zda rezervace již nevypršela
        zkontrolujExpiraci();
    }

    public void zkontrolujExpiraci() {
        if (rezervovano && !"-".equals(datumDo)) {
            try {
                LocalDate datumKonce = LocalDate.parse(datumDo, FORMATTER);
                if (LocalDate.now().isAfter(datumKonce)) {
                    this.rezervovano = false;
                    this.datumDo = "-";
                }
            } catch (Exception ignored) {}
        }
    }

    public String getId() { return id; }
    public String getNazev() { return nazev; }
    public String getAutor() { return autor; }
    public String getRok() { return rok; }
    public String getZanr() { return zanr; }
    public boolean isRezervovano() { return rezervovano; }
    public String getDatumDo() { return datumDo; }

    public void rezervovat(String datumDo) {
        this.rezervovano = true;
        this.datumDo = datumDo;
    }

    public String toTxtLine() {
        return id + ";" + nazev + ";" + autor + ";" + rok + ";" + zanr + ";" + rezervovano + ";" + datumDo;
    }

    @Override
    public String toString() {
        return nazev + " (" + autor + ")" + (rezervovano ? " [REZERVOVÁNO do: " + datumDo + "]" : "");
    }
}