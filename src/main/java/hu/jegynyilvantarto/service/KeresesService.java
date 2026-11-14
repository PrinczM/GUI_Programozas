package hu.jegynyilvantarto.service;

import hu.jegynyilvantarto.model.*;

import java.text.Collator;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

public class KeresesService {

    private final Adatbazis db;
    private static final Collator HU = Collator.getInstance(Locale.of("hu"));

    /** F18 */
    public static final Comparator<OptionalDouble> ATLAG_RENDEZO = (a, b) -> {
        if (a.isEmpty() && b.isEmpty()) return 0;
        if (a.isEmpty()) return 1;
        if (b.isEmpty()) return -1;
        return Double.compare(a.getAsDouble(), b.getAsDouble());
    };

    public KeresesService(Adatbazis db) {
        this.db = db;
    }

    /** F7, F17, F20 */
    public List<Osztaly> tanarOsztalyai(int tanarId) {
        Set<Integer> osztalyIds = db.tanitasok.stream()
                .filter(t -> t.tanarId() == tanarId)
                .map(Tanitas::osztalyId)
                .collect(Collectors.toSet());
        return db.osztalyok.stream()
                .filter(o -> osztalyIds.contains(o.id()))
                .sorted((a, b) -> HU.compare(a.nev(), b.nev()))
                .toList();
    }

    /** F11, F19, F20 */
    public List<Tantargy> tanitottTantargyak(int tanarId, int osztalyId) {
        Set<Integer> tantargyIds = db.tanitasok.stream()
                .filter(t -> t.tanarId() == tanarId && t.osztalyId() == osztalyId)
                .map(Tanitas::tantargyId)
                .collect(Collectors.toSet());
        return db.tantargyak.stream()
                .filter(t -> tantargyIds.contains(t.id()))
                .sorted((a, b) -> HU.compare(a.nev(), b.nev()))
                .toList();
    }

    /** F27 */
    public List<Tantargy> tanarTantargyai(int tanarId) {
        Set<Integer> tantargyIds = db.tanitasok.stream()
                .filter(t -> t.tanarId() == tanarId)
                .map(Tanitas::tantargyId)
                .collect(Collectors.toSet());
        return db.tantargyak.stream()
                .filter(t -> tantargyIds.contains(t.id()))
                .sorted((a, b) -> HU.compare(a.nev(), b.nev()))
                .toList();
    }

    /** F11 */
    public boolean tanitja(int tanarId, int osztalyId, int tantargyId) {
        return db.tanitasok.stream().anyMatch(
                t -> t.tanarId() == tanarId && t.osztalyId() == osztalyId && t.tantargyId() == tantargyId);
    }

    /** F10 */
    public List<Diak> tanarDiakjai(int tanarId) {
        Set<Integer> osztalyIds = db.tanitasok.stream()
                .filter(t -> t.tanarId() == tanarId)
                .map(Tanitas::osztalyId)
                .collect(Collectors.toSet());
        return db.diakok.stream()
                .filter(d -> osztalyIds.contains(d.osztalyId()))
                .sorted((a, b) -> HU.compare(a.nev(), b.nev()))
                .toList();
    }

    /** F16, F17 */
    public List<Diak> szur(int tanarId, String nevReszlet, Integer osztalyIdVagyNull) {
        List<Diak> lista = new ArrayList<>(tanarDiakjai(tanarId));
        if (nevReszlet != null && !nevReszlet.trim().isEmpty()) {
            String keresett = normalizal(nevReszlet.trim());
            lista.removeIf(d -> !normalizal(d.nev()).contains(keresett));
        }
        if (osztalyIdVagyNull != null) {
            lista.removeIf(d -> d.osztalyId() != osztalyIdVagyNull);
        }
        return lista;
    }

    /** F16 */
    public static String normalizal(String s) {
        String lower = s.toLowerCase();
        String nfd = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}", "");
    }
}
