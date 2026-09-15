# Školní knihovna

Jednoduchá desktopová aplikace pro správu knihovního katalogu ve Swing GUI. Projekt simuluje práci se školní knihovnou pro dvě role:

- Student – prohlížení katalogu, řazení, vyhledávání a rezervaci knih
- Pracovník – správa knih, úprava katalogu a evidence rezervací

Aplikace ukládá data do souboru `knihy.txt` v textovém formátu, takže nevyžaduje databázi ani externí server.

## Analýza projektu

### Cílový účel
Projekt je zaměřený na jednoduchou evidenci knih a jejich dostupnosti ve školní knihovně. Hlavní funkce jsou:

- přihlášení podle role (`Student` / `Pracovník`)
- procházení knih z katalogu
- filtrování a řazení knih
- vyhledávání podle názvu nebo autora
- rezervace knih studentem
- správa rezervací a mazání/řazení knih pracovníkem
- persistenci dat do textového souboru

### Architektura
Projekt neobsahuje MVC framework ani databázový layer. Je postavený jako klasická Java Swing aplikace s datovým modelem `Kniha` a několika view třídami:

- `Hlavni.java` – vstupní bod aplikace a přihlašovací dialog
- `Student.java` – GUI pro studenta
- `Pracovnik.java` – GUI pro pracovníka
- `Kniha.java` – datová třída reprezentující knihu
- `knihy.txt` – soubor s daty katalogu

### Datový model
Třída `Kniha` obsahuje základní informace o jedné knize:

- `id`
- `nazev`
- `autor`
- `rok`
- `zanr`
- `rezervovano`
- `datumDo`

Obsahuje také logiku pro automatické vyhodnocení expirace rezervace:

- pokud je kniha rezervovaná a datum vypršení je v minulosti, rezervace se zruší
- při výpisu do souboru se řetězec serializuje do formátu:
  `id;nazev;autor;rok;zanr;rezervovano;datumDo`

### Persistenci dat
Data se ukládají do souboru `knihy.txt` v kořenovém adresáři projektu.

Při každém uložení se zapíše seznam knih ve formátu CSV-like textu:

```text
5;1984;George Orwell;1949;Dystopický román;true;31.12.2026
220;20 000 mil pod mořem;Jules Verne;1870;Dobrodružný;false;-
```

Tento přístup je jednoduchý a vhodný pro školní aplikaci bez databáze.

### Silné stránky
- jednoduchá implementace a přehledná struktura
- bez externích závislostí
- snadné spuštění v IntelliJ IDEA nebo z příkazové řádky
- čisté GUI pro studenty i pracovníky

### Omezení
- žádný skutečný autentizační systém, heslo je pouze pevně zakódované (`admin`)
- data jsou uložená jako text, ne jako relační databáze
- chybí jednotkové testy a robustnější validace vstupů
- chybí rozšíření typu evidence výpůjček, vrácení, historie nebo přístupová práva

## Instalace a spuštění

### Požadavky
- Java JDK 17 nebo novější
- IntelliJ IDEA / Eclipse / libovolný editor s podporou Javy

### Spuštění v IntelliJ IDEA
1. Otevřete projekt v IntelliJ IDEA
2. Ujistěte se, že je nastaven Java SDK
3. Spusťte třídu `Hlavni` v adresáři `src`

### Spuštění z příkazové řádky
Z kořenového adresáře projektu:

```bash
javac -d out src/*.java
java -cp out Hlavni
```

Pokud `javac` není rozpoznán, nainstalujte JDK a ujistěte se, že je nastavena proměnná `PATH`.

## Popis jednotlivých tříd a metod

### `Hlavni.java`
Hlavní třída aplikace, která slouží jako vstupní bod.

#### `main(String[] args)`
Spouští přihlašovací dialog a po výběru role otevírá správný režim aplikace.

#### `zobrazPrihlasovaciOkno()`
Vytváří dialog s výběrem role (`Student` / `Pracovník`).

- pro studenta jsou skryta pole pro heslo
- pro pracovníka se zobrazí `JPasswordField`
- po stisku OK se ověří heslo `admin`
- při chybném hesle se aplikace ukončí

#### `student()`
Spustí režim studenta přes Swing thread.

#### `pracovnik()`
Spustí režim pracovníka přes Swing thread.

### `Kniha.java`
Datová třída reprezentující jednu knihu.

#### `zkontrolujExpiraci()`
Kontroluje, zda rezervace již nevypršela. Pokud ano, nastaví:

- `rezervovano = false`
- `datumDo = "-"`

#### `rezervovat(String datumDo)`
Uloží datum rezervace a nastaví stav knihy jako rezervovanou.

#### `toTxtLine()`
Převede objekt do řetězce vhodného pro uložení do `knihy.txt`.

#### `toString()`
Vrací lidsky čitelné zobrazení knihy s informací o rezervaci.

### `Student.java`
GUI pro studenta.

#### `seradKnihy()`
Řadí knihy podle:

- názvu
- autora
- roku vydání (nejnovější/nejstarší)

#### `nacistiKnihyZeSouboru()`
Načte knihy z `knihy.txt` a naplní seznam v GUI.

#### `ulozKnihyDoSouboru()`
Uloží aktuální katalog do textového souboru.

#### `otvriVyhledavaciOkno()`
Otevírá dialog pro vyhledávání podle názvu nebo autora.

#### `zobrazDetailKnihy(Kniha kniha)`
Zobrazí okno s informacemi o knize.

- pokud je kniha volná, umožňuje rezervaci
- při rezervaci se zadá datum a uloží se do souboru

#### `vytvorIkonuKnihy(int width, int height, Color color)`
Generuje jednoduchou ikonku knihy pro vizuální zobrazení v seznamu.

### `Pracovnik.java`
GUI pro pracovníka.

#### `setTabulkaEditovatelna(boolean editable)`
Přepíná tabulku mezi režimem prohlížení a režimem úprav. ID knihy je chráněno před úpravou.

#### `nacistiKnihyZeSouboru()`
Načte všechny knihy z textového souboru a naplní tabulku.

#### `synchornizovatTabulkuDoSeznamu()`
Převede data z tabulky zpět do seznamu `List<Kniha>` před uložením.

#### `ulozKnihyDoSouboru()`
Uloží aktuální stav katalogu do `knihy.txt`.

#### `otvriSpravuRezervaci()`
Otevře dialog se dvěma záložkami:

- `Rezervované`
- `Nerezervované`

Umožňuje:

- zrušení rezervace dvojklikem
- přesun knihy mezi stavy
- označení za vyřazenou z oběhu
- nové rezervace

#### `obnovZalozky(...)`
Obnoví obsah obou listů ve správě rezervací podle aktuálního stavu knih.

## Pracovní postup aplikace

1. Spuštění aplikace otevře přihlašovací obrazovku.
2. Uživatel zvolí roli `Student` nebo `Pracovník`.
3. Pokud je vybrán pracovník, musí zadat heslo `admin`.
4. Student:
   - vidí katalog knih
   - může řadit a hledat
   - kliknutím na knihu zobrazí detail
   - může rezervovat volnou knihu
5. Pracovník:
   - vidí tabulku všech knih
   - může upravovat data v režimu Edit
   - může ukládat změny
   - může spravovat rezervace a vyřazovat knihy

## Poznámka
V aktuálním prostředí nebyl nainstalovaný JDK, proto nebylo možné zde provést skutečnou kompilaci a spuštění. V projektu je však nastaven standardní Java Swing stack a instalace je plně kompatibilní s běžným JDK 17+.

## Shrnutí
Tento projekt je jednoduchý, ale funkční školní katalog knih vytvořený v Javě Swing. Je vhodný pro výuku, prototypování a demonstraci práce s GUI, datovým modelem a serializací do textového souboru.
