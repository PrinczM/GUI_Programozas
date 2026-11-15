package hu.jegynyilvantarto.service;

import hu.jegynyilvantarto.model.*;

public class DiakService {

    private final Adatbazis db;

    public DiakService(Adatbazis db) {
        this.db = db;
    }

    /** F7 */
    public Diak felvetel(String nev, int osztalyId) {
        nev = Validator.nev(nev);
        if (db.osztalyok.stream().noneMatch(o -> o.id() == osztalyId)) {
            throw new ValidaciosHiba("Nem létező osztály.");
        }
        Diak diak = new Diak(Adatbazis.kovetkezoId(db.diakok), nev, osztalyId);
        db.diakok.add(diak);
        return diak;
    }

    /** F7 */
    public boolean vanAzonosNevu(String nev, int osztalyId, Integer kiveveDiakId) {
        String keresett = nev.trim().toLowerCase();
        return db.diakok.stream().anyMatch(d ->
                d.osztalyId() == osztalyId
                        && d.nev().trim().toLowerCase().equals(keresett)
                        && (kiveveDiakId == null || d.id() != kiveveDiakId));
    }

    /** F8 */
    public void modositas(int diakId, String ujNev, int ujOsztalyId) {
        ujNev = Validator.nev(ujNev);
        if (db.osztalyok.stream().noneMatch(o -> o.id() == ujOsztalyId)) {
            throw new ValidaciosHiba("Nem létező osztály.");
        }
        for (int i = 0; i < db.diakok.size(); i++) {
            if (db.diakok.get(i).id() == diakId) {
                db.diakok.set(i, new Diak(diakId, ujNev, ujOsztalyId));
                return;
            }
        }
    }

    /** F9 */
    public void torles(int diakId) {
        db.jegyek.removeIf(j -> j.diakId() == diakId);
        db.diakok.removeIf(d -> d.id() == diakId);
    }

    /** F14 */
    public Diak diak(int diakId) {
        return db.diakok.stream()
                .filter(d -> d.id() == diakId)
                .findFirst()
                .orElseThrow(() -> new ValidaciosHiba("Nem létező diák."));
    }

    /** F9 */
    public int jegyekSzama(int diakId) {
        return (int) db.jegyek.stream().filter(j -> j.diakId() == diakId).count();
    }
}
