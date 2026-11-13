package hu.jegynyilvantarto.service;

import hu.jegynyilvantarto.model.*;

import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminService {

    private final Adatbazis db;
    private static final Collator HU = Collator.getInstance(Locale.of("hu"));

    public AdminService(Adatbazis db) {
        this.db = db;
    }

    /** F3 */
    public Osztaly osztalyFelvetel(String nev) {
        nev = Validator.osztalyNev(nev);
        String n = nev;
        if (db.osztalyok.stream().anyMatch(o -> o.nev().equals(n))) {
            throw new ValidaciosHiba("Ilyen osztály már létezik.");
        }
        Osztaly osztaly = new Osztaly(Adatbazis.kovetkezoId(db.osztalyok), nev);
        db.osztalyok.add(osztaly);
        return osztaly;
    }

    /** F3 */
    public void osztalyTorles(int osztalyId) {
        long diakSzam = db.diakok.stream().filter(d -> d.osztalyId() == osztalyId).count();
        if (diakSzam > 0) {
            throw new ValidaciosHiba(String.format("Az osztályban %d diák van.", diakSzam));
        }
        long tanitasSzam = db.tanitasok.stream().filter(t -> t.osztalyId() == osztalyId).count();
        if (tanitasSzam > 0) {
            throw new ValidaciosHiba(String.format("Az osztályhoz %d tanítás tartozik.", tanitasSzam));
        }
        db.osztalyok.removeIf(o -> o.id() == osztalyId);
    }

    /** F4 */
    public Tantargy tantargyFelvetel(String nev) {
        nev = Validator.tantargyNev(nev);
        String nevLower = nev.toLowerCase();
        if (db.tantargyak.stream().anyMatch(t -> t.nev().toLowerCase().equals(nevLower))) {
            throw new ValidaciosHiba("Ilyen tantárgy már létezik.");
        }
        Tantargy tantargy = new Tantargy(Adatbazis.kovetkezoId(db.tantargyak), nev);
        db.tantargyak.add(tantargy);
        return tantargy;
    }

    /** F4 */
    public void tantargyTorles(int tantargyId) {
        if (db.jegyek.stream().anyMatch(j -> j.tantargyId() == tantargyId)) {
            throw new ValidaciosHiba("A tantárgyhoz jegy tartozik, nem törölhető.");
        }
        if (db.tanitasok.stream().anyMatch(t -> t.tantargyId() == tantargyId)) {
            throw new ValidaciosHiba("A tantárgyhoz tanítás tartozik, nem törölhető.");
        }
        db.tantargyak.removeIf(t -> t.id() == tantargyId);
    }

    /** F5 */
    public Tanar tanarFelvetel(String nev) {
        nev = Validator.nev(nev);
        String nevLower = nev.toLowerCase();
        if (db.tanarok.stream().anyMatch(t -> t.nev().toLowerCase().equals(nevLower))) {
            throw new ValidaciosHiba("Ilyen nevű tanár már létezik.");
        }
        Tanar tanar = new Tanar(Adatbazis.kovetkezoId(db.tanarok), nev);
        db.tanarok.add(tanar);
        return tanar;
    }

    /** F5 */
    public void tanarAtnevezes(int tanarId, String ujNev) {
        ujNev = Validator.nev(ujNev);
        String ujNevLower = ujNev.toLowerCase();
        if (db.tanarok.stream().anyMatch(t -> t.id() != tanarId && t.nev().toLowerCase().equals(ujNevLower))) {
            throw new ValidaciosHiba("Ilyen nevű tanár már létezik.");
        }
        for (int i = 0; i < db.tanarok.size(); i++) {
            if (db.tanarok.get(i).id() == tanarId) {
                db.tanarok.set(i, new Tanar(tanarId, ujNev));
                return;
            }
        }
    }

    /** F5 */
    public void tanarTorles(int tanarId) {
        if (db.tanitasok.stream().anyMatch(t -> t.tanarId() == tanarId)) {
            throw new ValidaciosHiba("A tanárhoz tanítás tartozik, nem törölhető.");
        }
        db.tanarok.removeIf(t -> t.id() == tanarId);
    }

    /** F6 */
    public void hozzarendel(int tanarId, int osztalyId, int tantargyId) {
        db.tanitasok.stream()
                .filter(t -> t.osztalyId() == osztalyId && t.tantargyId() == tantargyId)
                .findFirst()
                .ifPresent(t -> {
                    throw new ValidaciosHiba(String.format("A %s %s már %s tanítja.",
                            osztalyNev(osztalyId), tantargyNev(tantargyId), tanarNev(t.tanarId())));
                });
        db.tanitasok.add(new Tanitas(osztalyId, tantargyId, tanarId));
    }

    /** F6 */
    public void hozzarendelesTorles(int osztalyId, int tantargyId) {
        db.tanitasok.removeIf(t -> t.osztalyId() == osztalyId && t.tantargyId() == tantargyId);
    }

    /** F6 */
    public int parJegyeinekSzama(int osztalyId, int tantargyId) {
        Set<Integer> diakIds = db.diakok.stream()
                .filter(d -> d.osztalyId() == osztalyId)
                .map(Diak::id)
                .collect(Collectors.toSet());
        return (int) db.jegyek.stream()
                .filter(j -> j.tantargyId() == tantargyId && diakIds.contains(j.diakId()))
                .count();
    }

    /** F3 */
    public List<Osztaly> osztalyok() {
        return db.osztalyok.stream()
                .sorted((a, b) -> HU.compare(a.nev(), b.nev()))
                .toList();
    }

    /** F4 */
    public List<Tantargy> tantargyak() {
        return db.tantargyak.stream()
                .sorted((a, b) -> HU.compare(a.nev(), b.nev()))
                .toList();
    }

    /** F1 */
    public List<Tanar> tanarok() {
        return db.tanarok.stream()
                .sorted((a, b) -> HU.compare(a.nev(), b.nev()))
                .toList();
    }

    /** F6 */
    public List<Tanitas> tanitasok() {
        return List.copyOf(db.tanitasok);
    }

    public String osztalyNev(int id) {
        return db.osztalyok.stream().filter(o -> o.id() == id).findFirst().orElseThrow().nev();
    }

    public String tantargyNev(int id) {
        return db.tantargyak.stream().filter(t -> t.id() == id).findFirst().orElseThrow().nev();
    }

    public String tanarNev(int id) {
        return db.tanarok.stream().filter(t -> t.id() == id).findFirst().orElseThrow().nev();
    }
}
