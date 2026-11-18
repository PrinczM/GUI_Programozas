package hu.jegynyilvantarto.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hu.jegynyilvantarto.model.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MentesService {

    private String hibaUzenet;

    /** F2 */
    public Adatbazis betolt(Path fajl) {
        hibaUzenet = null;
        if (!Files.exists(fajl)) {
            return new Adatbazis();
        }
        try {
            String json = Files.readString(fajl);
            Gson gson = new Gson();
            Adatbazis db = gson.fromJson(json, Adatbazis.class);
            if (db == null) db = new Adatbazis();
            nullCsere(db);
            ellenoriz(db);
            return db;
        } catch (Exception e) {
            hibaUzenet = e.getMessage();
            hibasraAtnevez(fajl);
            return new Adatbazis();
        }
    }

    public String hibaUzenet() {
        return hibaUzenet;
    }

    /** F25 */
    public void ment(Adatbazis db, Path fajl) throws IOException {
        if (fajl.getParent() != null) {
            Files.createDirectories(fajl.getParent());
        }
        Path tmp = fajl.resolveSibling(fajl.getFileName() + ".tmp");
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Files.writeString(tmp, gson.toJson(db));
        try {
            Files.move(tmp, fajl, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, fajl, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** 4.8 */
    public static void ellenoriz(Adatbazis db) {
        egyediId(db.osztalyok, "osztály");
        egyediId(db.tantargyak, "tantárgy");
        egyediId(db.tanarok, "tanár");
        egyediId(db.diakok, "diák");
        egyediId(db.jegyek, "jegy");

        Set<String> osztalyNevek = new HashSet<>();
        for (Osztaly o : db.osztalyok) {
            if (!o.nev().matches("\\d{1,2}\\.[A-Z]")) {
                throw new ValidaciosHiba("Érvénytelen osztálynév: " + o.nev());
            }
            if (!osztalyNevek.add(o.nev())) {
                throw new ValidaciosHiba("Duplikált osztálynév: " + o.nev());
            }
        }

        Set<String> tantargyNevek = new HashSet<>();
        for (Tantargy t : db.tantargyak) {
            if (!tantargyNevek.add(t.nev().toLowerCase())) {
                throw new ValidaciosHiba("Duplikált tantárgynév: " + t.nev());
            }
        }

        Set<String> tanarNevek = new HashSet<>();
        for (Tanar t : db.tanarok) {
            if (!tanarNevek.add(t.nev().toLowerCase())) {
                throw new ValidaciosHiba("Duplikált tanárnév: " + t.nev());
            }
        }

        Set<String> tanitasKulcsok = new HashSet<>();
        for (Tanitas t : db.tanitasok) {
            if (!tanitasKulcsok.add(t.osztalyId() + ":" + t.tantargyId())) {
                throw new ValidaciosHiba("Duplikált tanítás: osztály=" + t.osztalyId()
                        + ", tantárgy=" + t.tantargyId());
            }
        }

        Set<Integer> osztalyIds = new HashSet<>();
        db.osztalyok.forEach(o -> osztalyIds.add(o.id()));
        Set<Integer> tantargyIds = new HashSet<>();
        db.tantargyak.forEach(t -> tantargyIds.add(t.id()));
        Set<Integer> tanarIds = new HashSet<>();
        db.tanarok.forEach(t -> tanarIds.add(t.id()));
        Set<Integer> diakIds = new HashSet<>();
        db.diakok.forEach(d -> diakIds.add(d.id()));

        for (Tanitas t : db.tanitasok) {
            if (!osztalyIds.contains(t.osztalyId()))
                throw new ValidaciosHiba("Tanítás nem létező osztályra hivatkozik: " + t.osztalyId());
            if (!tantargyIds.contains(t.tantargyId()))
                throw new ValidaciosHiba("Tanítás nem létező tantárgyra hivatkozik: " + t.tantargyId());
            if (!tanarIds.contains(t.tanarId()))
                throw new ValidaciosHiba("Tanítás nem létező tanárra hivatkozik: " + t.tanarId());
        }

        for (Diak d : db.diakok) {
            if (!osztalyIds.contains(d.osztalyId()))
                throw new ValidaciosHiba("Diák nem létező osztályra hivatkozik: " + d.id());
        }

        for (Jegy j : db.jegyek) {
            if (!diakIds.contains(j.diakId()))
                throw new ValidaciosHiba("Jegy nem létező diákra hivatkozik: " + j.id());
            if (!tantargyIds.contains(j.tantargyId()))
                throw new ValidaciosHiba("Jegy nem létező tantárgyra hivatkozik: " + j.id());
            if (j.ertek() < 1 || j.ertek() > 5)
                throw new ValidaciosHiba("Érvénytelen jegyérték: " + j.ertek());
        }
    }

    public static Path alapertelmezettUtvonal() {
        return Path.of(System.getProperty("user.home"), ".jegynyilvantarto", "adatok.json");
    }

    private static <T extends Azonosithato> void egyediId(List<T> lista, String tabla) {
        Set<Integer> ids = new HashSet<>();
        for (T elem : lista) {
            if (!ids.add(elem.id())) {
                throw new ValidaciosHiba("Duplikált " + tabla + " azonosító: " + elem.id());
            }
        }
    }

    private static void nullCsere(Adatbazis db) {
        if (db.osztalyok == null) db.osztalyok = new ArrayList<>();
        if (db.tantargyak == null) db.tantargyak = new ArrayList<>();
        if (db.tanarok == null) db.tanarok = new ArrayList<>();
        if (db.tanitasok == null) db.tanitasok = new ArrayList<>();
        if (db.diakok == null) db.diakok = new ArrayList<>();
        if (db.jegyek == null) db.jegyek = new ArrayList<>();
    }

    private void hibasraAtnevez(Path fajl) {
        try {
            Path hibasFajl = fajl.resolveSibling(fajl.getFileName() + ".hibas");
            Files.move(fajl, hibasFajl, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }
}
