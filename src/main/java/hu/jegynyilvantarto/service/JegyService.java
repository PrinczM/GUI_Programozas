package hu.jegynyilvantarto.service;

import hu.jegynyilvantarto.model.*;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

public class JegyService {

    private final Adatbazis db;
    private final KeresesService kereses;

    public JegyService(Adatbazis db, KeresesService kereses) {
        this.db = db;
        this.kereses = kereses;
    }

    /** F11 */
    public Jegy felvetel(int tanarId, int diakId, int tantargyId, int ertek,
                         String datum, String megjegyzes) {
        Validator.jegy(ertek);
        Validator.datum(datum);
        megjegyzes = Validator.megjegyzes(megjegyzes);

        int osztalyId = db.diakok.stream()
                .filter(d -> d.id() == diakId)
                .findFirst()
                .orElseThrow(() -> new ValidaciosHiba("Nem létező diák."))
                .osztalyId();

        if (!kereses.tanitja(tanarId, osztalyId, tantargyId)) {
            throw new ValidaciosHiba("A tanár nem tanítja ezt a tantárgyat ebben az osztályban.");
        }

        Jegy jegy = new Jegy(Adatbazis.kovetkezoId(db.jegyek), diakId, tantargyId, ertek, datum, megjegyzes);
        db.jegyek.add(jegy);
        return jegy;
    }

    /** F12 */
    public void modositas(int jegyId, int ertek, String datum, String megjegyzes) {
        Validator.jegy(ertek);
        Validator.datum(datum);
        megjegyzes = Validator.megjegyzes(megjegyzes);

        for (int i = 0; i < db.jegyek.size(); i++) {
            if (db.jegyek.get(i).id() == jegyId) {
                Jegy regi = db.jegyek.get(i);
                db.jegyek.set(i, new Jegy(jegyId, regi.diakId(), regi.tantargyId(), ertek, datum, megjegyzes));
                return;
            }
        }
    }

    /** F13 */
    public void torles(int jegyId) {
        db.jegyek.removeIf(j -> j.id() == jegyId);
    }

    /** F14 */
    public List<Jegy> jegyek(int diakId, int tantargyId) {
        return db.jegyek.stream()
                .filter(j -> j.diakId() == diakId && j.tantargyId() == tantargyId)
                .sorted(Comparator.comparing(Jegy::datum))
                .toList();
    }

    /** F14 */
    public OptionalDouble tantargyiAtlag(int diakId, int tantargyId) {
        return db.jegyek.stream()
                .filter(j -> j.diakId() == diakId && j.tantargyId() == tantargyId)
                .mapToInt(Jegy::ertek)
                .average();
    }

    /** F10, F14 */
    public OptionalDouble osszesitettAtlag(int tanarId, int diakId) {
        int osztalyId = db.diakok.stream()
                .filter(d -> d.id() == diakId)
                .findFirst()
                .map(Diak::osztalyId)
                .orElse(-1);

        Set<Integer> tantargyIds = db.tanitasok.stream()
                .filter(t -> t.tanarId() == tanarId && t.osztalyId() == osztalyId)
                .map(Tanitas::tantargyId)
                .collect(Collectors.toSet());

        return db.jegyek.stream()
                .filter(j -> j.diakId() == diakId && tantargyIds.contains(j.tantargyId()))
                .mapToInt(Jegy::ertek)
                .average();
    }

    /** F15 */
    public boolean bukasraAll(int diakId, int tantargyId) {
        List<Jegy> diakJegyei = db.jegyek.stream()
                .filter(j -> j.diakId() == diakId && j.tantargyId() == tantargyId)
                .toList();
        if (diakJegyei.isEmpty()) return false;
        double atlag = diakJegyei.stream().mapToInt(Jegy::ertek).average().orElse(0);
        return atlag < 2.0;
    }

    /** F15 */
    public boolean bukasraAllBarmelyikbol(int tanarId, int diakId) {
        int osztalyId = db.diakok.stream()
                .filter(d -> d.id() == diakId)
                .findFirst()
                .map(Diak::osztalyId)
                .orElse(-1);

        return db.tanitasok.stream()
                .filter(t -> t.tanarId() == tanarId && t.osztalyId() == osztalyId)
                .anyMatch(t -> bukasraAll(diakId, t.tantargyId()));
    }
}
