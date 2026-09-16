# Školní knihovna — kompletní dokumentace

Tato dokumentace popisuje projekt "Školní knihovna" od přípravy a analýzy, přes návrh a implementaci, až po provoz, údržbu a možné rozšíření. Je psaná tak, aby vývojář i uživatel rychle pochopili účel projektu, jeho strukturu a postupy práce.

Obsah:
- Přehled projektu
- Příprava a analýza
- Funkční a nefunkční požadavky
- Architektura a datový model
- Popis tříd a metod
- Instalace, build a spuštění
- Pracovní postup aplikace (uživatelský i vývojářský)
- Testování a validace
- Nasazení a údržba
- Možná vylepšení
- Poznámky a autor

## Přehled projektu
"Školní knihovna" je desktopová Java aplikace s GUI postaveným na Swingu, určená pro jednoduchou evidenci knih, jejich rezervací a správu katalogu ve školním prostředí. Cílem je demonstrovat práci s GUI, jednoduchým perzistentním úložištěm (textový soubor) a základními rolemi uživatelů: Student a Pracovník.

## Příprava a analýza

1. Požadavky a cíle
   - Základní evidence knih (název, autor, rok, žánr, id)
   - Možnost rezervace knih studenty
   - Administrativní rozhraní pro pracovníka ke správě katalogu
   - Jednoduché perzistentní uložení bez nutnosti DB
   - Snadné spuštění v IDE i z příkazové řádky

2. Uživatelské scénáře (use cases)
   - Student prohlíží katalog, vyhledává knihy, rezervuje dostupné tituly
   - Pracovník přidává/maže/úpravuje záznamy, spravuje rezervace
   - Data jsou trvale uložená v textovém souboru

3. Omezení a předpoklady
   - Jednoduché autentizační řešení (pevné heslo `admin` pro pracovníka)
   - Perzistence v plochém textovém souboru (CSV-like)
   - Single-user scénář (soubor není vyřešen pro současné víceuživatelské přístupy)

4. Návrh řešení
   - Swing GUI pro obě role
   - Datová třída `Kniha` s metodami pro serializaci a kontrolu expirace
   - Jednoduché CRUD operace nad kolekcí `List<Kniha>`
   - Ukládání/načítání do `knihy.txt`

## Funkční a nefunkční požadavky

Funkční:
- Přihlášení podle role
- Prohlížení a filtrování katalogu
- Rezervace a zrušení rezervace
- Správa katalogu pracovníkem (přidání, editace, smazání)
- Uložení a načtení katalogu do souboru

Nefunkční:
- Kompatibilita s JDK 17+
- Přívětivé GUI
- Snadná údržba kódu a rozšiřitelnost

## Architektura a datový model
Aplikace je organizovaná do jednoduchých tříd bez zvláštního frameworku.

Hlavní komponenty:
- Hlavni.java — vstupní bod, přihlašovací rozhraní
- Student.java — GUI a logika pro uživatele-studenta
- Pracovnik.java — GUI a logika pro pracovníka (administrátora)
- Kniha.java — model knihy a utilitní metody
- knihy.txt — perzistentní úložiště (CSV-like)

Formát řádku v `knihy.txt`:
id;nazev;autor;rok;zanr;rezervovano;datumDo

Příklad:
5;1984;George Orwell;1949;Dystopický román;true;31.12.2026

## Popis tříd a metod (detailně)

Níže jsou uvedeny hlavní třídy a jejich významné metody. Uvedené signatury jsou orientační podle reálné implementace v projektu.

- Hlavni.java
  - public static void main(String[] args)
    - Inicializuje aplikaci, volá zobrazPrihlasovaciOkno.
  - private void zobrazPrihlasovaciOkno()
    - Zobrazí dialog pro výběr role; na základě volby spustí Student nebo Pracovnik.
  - private void student()
    - Spouští GUI pro studenta v EDT (SwingUtilities.invokeLater).
  - private void pracovnik()
    - Spouští GUI pro pracovníka v EDT.

- Kniha.java
  - private String id, nazev, autor, rok, zanr;
  - private boolean rezervovano;
  - private String datumDo; // formát dd.MM.yyyy nebo "-"

  - public Kniha(String id, String nazev, String autor, String rok, String zanr, boolean rezervovano, String datumDo)
    - Konstruktor, validace základních polí.
  - public void zkontrolujExpiraci()
    - Pokud je rezervovano==true a datumDo před dnešním dnem, nastaví rezervovano=false a datumDo="-".
  - public void rezervovat(String datumDo)
    - Nastaví rezervovano=true a uloží datumDo (platné ověření formátu).
  - public String toTxtLine()
    - Serializuje do formátu pro `knihy.txt`.
  - public static Kniha fromTxtLine(String line)
    - Parsuje řádek ze souboru a vrací instanci Kniha (robustní proti chybám formátu).
  - public String toString()
    - Čitelný popis pro zobrazení v GUI.

- Student.java
  - private List<Kniha> knihy;
  - private JList nebo JTable pro zobrazení katalogu.

  - public void nacistiKnihyZeSouboru()
    - Načte `knihy.txt`, vytvoří objekty Kniha, zavolá zkontrolujExpiraci pro každou.
  - public void ulozKnihyDoSouboru()
    - Zapíše seznam knih do souboru (atomicky: dočasný soubor -> přejmenovat).
  - public void seradKnihy(String kriterium, boolean asc)
    - Řadí kolekci podle názvu/ autora/ roku.
  - public void otvriVyhledavaciOkno()
    - Zobrazí dialog pro vyhledávání a filtruje zobrazení.
  - public void zobrazDetailKnihy(Kniha k)
    - Detailní okno s možností rezervace.

- Pracovnik.java
  - private List<Kniha> knihy;
  - private JTable pro editaci dat.

  - public void nacistiKnihyZeSouboru()
    - Stejné jako u studenta.
  - public void ulozKnihyDoSouboru()
    - Serializace zpět do `knihy.txt`.
  - public void setTabulkaEditovatelna(boolean editable)
    - Umožní/zakáže úpravy buněk kromě ID (ID readonly).
  - public void synchornizovatTabulkuDoSeznamu()
    - Převede hodnoty z JTable do List<Kniha> s validacemi.
  - public void otvriSpravuRezervaci()
    - Dialog pro přehled rezervací a manipulaci s nimi.

## Instalace, build a spuštění (podrobně)

1. Nainstalovat JDK 17+ (OpenJDK nebo Oracle JDK).
2. Otevřít projekt v IntelliJ IDEA nebo jiném editoru.
3. Nastavit JDK pro projekt.
4. Spustit třídu Hlavni.

Příkazově (Windows, příklad z kořenového adresáře projektu):

javac -d out src\\*.java
java -cp out Hlavni

Poznámka: doporučeno vytvořit build skript nebo použít Maven/Gradle pro lepší správu závislostí a sestavení.

## Bezpečnost a validace vstupů
- Heslo pracovnika je aktuálně pevné (`admin`). Pro produkční použití nasadit bezpečnou autentizaci.
- Při parsování souboru `knihy.txt` dělat defensivní programování: ignorovat řádky ve špatném formátu a logovat chyby.
- Při zápisu do souboru použít atomický zápis: nejdřív dočasný soubor a poté přejmenovat.

## Testování a kontrola kvality
- Doporučeno přidat jednotkové testy pro:
  - Parsování a serializaci `Kniha.fromTxtLine` / `toTxtLine`
  - Logiku expirace rezervace
  - Funkce pro řazení a filtrování
- Manuální testování GUI provést v IntelliJ: testovat běžné scénáře (rezervace, vyřazení, editace).

## Nasazení a údržba
- Projekt je jednosouborový desktop — dodání formou JAR souboru je vhodné (přidat manifest s hlavní třídou Hlavni).
- Zálohování `knihy.txt` pravidelně
- Logování chyb do souboru pro jednodušší debugování

## Možná vylepšení (roadmap)
1. Přidat SQLite nebo jinou DB pro víceuživatelské použití a transakční integritu
2. Vylepšit autentizaci (uživatelské účty, role, hesla)
3. Přidat historii výpůjček a vrácení knih
4. Implementovat REST API a přesun UI do webového rozhraní (pro vzdálený přístup)
5. Přidat unit/integration tests a CI pipeline
6. Export/import katalogu (CSV/JSON)

## Příklad pracovního postupu vývojáře
1. Vytvořit větev feature/<kratky-popis> v Git
2. Implementovat změnu + lokálně otestovat
3. Přidat nebo upravit unit testy
4. Commit s popisem změny a push do vzdáleného repozitáře
5. Vytvořit pull request a požádat o code review

## Poznámky
- Soubor `knihy.txt` je citlivý na formát — při ručních úpravách používejte správné oddělovače a formáty dat.
- Pokud je potřeba převod na DB, použít migrační skript, který přečte `knihy.txt` a vytvoří odpovídající tabulky.

## Autor
- Jméno: steprooauh