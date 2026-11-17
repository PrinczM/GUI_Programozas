package hu.jegynyilvantarto.service;

import hu.jegynyilvantarto.model.*;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

public class StatisztikaService {

    private final Adatbazis db;
    private final KeresesService kereses;
    private final JegyService jegy;
    private static final Collator HU = Collator.getInstance(Locale.of("hu"));

    public StatisztikaService(Adatbazis db, KeresesService kereses, JegyService jegy) {
        this.db = db;
        this.kereses = kereses;
        this.jegy = jegy;
    }

    /** F20-F24 */
    public Statisztika szamol(int tanarId, int osztalyId, Integer tantargyIdVagyNull) {
        List<Jegy> jegyekLista = relevansJegyek(tanarId, osztalyId, tantargyIdVagyNull);

        int jegyekSzama = jegyekLista.size();
        OptionalDouble atlag = jegyekLista.stream().mapToInt(Jegy::ertek).average();

        int[] szamok = new int[5];
        for (Jegy j : jegyekLista) szamok[j.ertek() - 1]++;
        List<Integer> eloszlas = List.of(szamok[0], szamok[1], szamok[2], szamok[3], szamok[4]);

        List<DiakAtlag> diakAtlagLista = diakAtlagok(tanarId, osztalyId, tantargyIdVagyNull);
        Optional<DiakAtlag> legjobb = diakAtlagLista.isEmpty()
                ? Optional.empty()
                : Optional.of(diakAtlagLista.getFirst());

        List<Diak> bukasraAllok;
        if (tantargyIdVagyNull != null) {
            bukasraAllok = this.db.diakok.stream()
                    .filter(d -> d.osztalyId() == osztalyId && jegy.bukasraAll(d.id(), tantargyIdVagyNull))
                    .sorted((a, b) -> HU.compare(a.nev(), b.nev()))
                    .toList();
        } else {
            bukasraAllok = this.db.diakok.stream()
                    .filter(d -> d.osztalyId() == osztalyId && jegy.bukasraAllBarmelyikbol(tanarId, d.id()))
                    .sorted((a, b) -> HU.compare(a.nev(), b.nev()))
                    .toList();
        }

        return new Statisztika(jegyekSzama, atlag, legjobb, eloszlas, bukasraAllok);
    }

    /** F26 */
    public List<DiakAtlag> diakAtlagok(int tanarId, int osztalyId, Integer tantargyIdVagyNull) {
        Set<Integer> tantargyIds = tantargyIdHalmaz(tanarId, osztalyId, tantargyIdVagyNull);

        List<DiakAtlag> eredmeny = new ArrayList<>();
        for (Diak diak : db.diakok.stream().filter(d -> d.osztalyId() == osztalyId).toList()) {
            List<Jegy> diakJegyei = db.jegyek.stream()
                    .filter(j -> j.diakId() == diak.id() && tantargyIds.contains(j.tantargyId()))
                    .toList();
            if (!diakJegyei.isEmpty()) {
                double atlag = diakJegyei.stream().mapToInt(Jegy::ertek).average().orElse(0);
                eredmeny.add(new DiakAtlag(diak, atlag, diakJegyei.size()));
            }
        }

        eredmeny.sort(Comparator
                .comparingDouble(DiakAtlag::atlag).reversed()
                .thenComparing(da -> da.diak().nev(), HU)
                .thenComparingInt(da -> da.diak().id()));

        return eredmeny;
    }

    /** F27 */
    public List<OsztalyAtlag> osztalyAtlagok(int tanarId, Integer tantargyIdVagyNull) {
        List<Osztaly> osztalyok;
        if (tantargyIdVagyNull != null) {
            Set<Integer> osztalyIds = db.tanitasok.stream()
                    .filter(t -> t.tanarId() == tanarId && t.tantargyId() == tantargyIdVagyNull)
                    .map(Tanitas::osztalyId)
                    .collect(Collectors.toSet());
            osztalyok = db.osztalyok.stream().filter(o -> osztalyIds.contains(o.id())).toList();
        } else {
            Set<Integer> osztalyIds = db.tanitasok.stream()
                    .filter(t -> t.tanarId() == tanarId)
                    .map(Tanitas::osztalyId)
                    .collect(Collectors.toSet());
            osztalyok = db.osztalyok.stream().filter(o -> osztalyIds.contains(o.id())).toList();
        }

        List<OsztalyAtlag> eredmeny = new ArrayList<>();
        for (Osztaly osztaly : osztalyok) {
            List<Jegy> jegyekLista = relevansJegyek(tanarId, osztaly.id(), tantargyIdVagyNull);
            OptionalDouble atlag = jegyekLista.stream().mapToInt(Jegy::ertek).average();
            eredmeny.add(new OsztalyAtlag(osztaly, atlag, jegyekLista.size()));
        }

        eredmeny.sort((a, b) -> HU.compare(a.osztaly().nev(), b.osztaly().nev()));
        return eredmeny;
    }

    /** F27 */
    public OptionalDouble tanarOsszesitettAtlaga(int tanarId, Integer tantargyIdVagyNull) {
        Set<Integer> tantargyIds;
        if (tantargyIdVagyNull != null) {
            tantargyIds = Set.of(tantargyIdVagyNull);
        } else {
            tantargyIds = db.tanitasok.stream()
                    .filter(t -> t.tanarId() == tanarId)
                    .map(Tanitas::tantargyId)
                    .collect(Collectors.toSet());
        }

        Set<Integer> osztalyIds = db.tanitasok.stream()
                .filter(t -> t.tanarId() == tanarId
                        && (tantargyIdVagyNull == null || t.tantargyId() == tantargyIdVagyNull))
                .map(Tanitas::osztalyId)
                .collect(Collectors.toSet());

        Set<Integer> diakIds = db.diakok.stream()
                .filter(d -> osztalyIds.contains(d.osztalyId()))
                .map(Diak::id)
                .collect(Collectors.toSet());

        return db.jegyek.stream()
                .filter(j -> diakIds.contains(j.diakId()) && tantargyIds.contains(j.tantargyId()))
                .mapToInt(Jegy::ertek)
                .average();
    }

    /** F23, F26, F27 */
    public DiagramAdat diagram(Nezet nezet, int tanarId, Integer osztalyIdVagyNull,
                               Integer tantargyIdVagyNull) {
        return switch (nezet) {
            case JEGYELOSZLAS -> jegyeloszlasDiagram(tanarId, osztalyIdVagyNull, tantargyIdVagyNull);
            case DIAKOK_ATLAGA -> diakokAtlagaDiagram(tanarId, osztalyIdVagyNull, tantargyIdVagyNull);
            case OSZTALYOK_OSSZEVETESE -> osztalyokOsszeveteseDiagram(tanarId, tantargyIdVagyNull);
        };
    }

    private DiagramAdat jegyeloszlasDiagram(int tanarId, int osztalyId, Integer tantargyIdVagyNull) {
        List<Jegy> jegyekLista = relevansJegyek(tanarId, osztalyId, tantargyIdVagyNull);

        int[] eloszlas = new int[5];
        for (Jegy j : jegyekLista) eloszlas[j.ertek() - 1]++;

        int maxSzam = 0;
        for (int sz : eloszlas) if (sz > maxSzam) maxSzam = sz;

        List<Oszlop> oszlopok = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String cimke = String.valueOf(i + 1);
            String szoveg = eloszlas[i] + " db";
            oszlopok.add(new Oszlop(cimke, eloszlas[i], szoveg, szoveg, i == 0));
        }

        return new DiagramAdat(oszlopok, maxSzam, OptionalDouble.empty(), "", "", "Nincs jegy");
    }

    private DiagramAdat diakokAtlagaDiagram(int tanarId, int osztalyId, Integer tantargyIdVagyNull) {
        List<DiakAtlag> atlagok = diakAtlagok(tanarId, osztalyId, tantargyIdVagyNull);
        List<Jegy> jegyekLista = relevansJegyek(tanarId, osztalyId, tantargyIdVagyNull);
        OptionalDouble osztalyAtlag = jegyekLista.stream().mapToInt(Jegy::ertek).average();

        long osszDiak = db.diakok.stream().filter(d -> d.osztalyId() == osztalyId).count();
        long jegyNelkuli = osszDiak - atlagok.size();

        List<Oszlop> oszlopok = new ArrayList<>();
        for (DiakAtlag da : atlagok) {
            String ertekSzoveg = Formazo.atlag(da.atlag());
            String tooltip = da.diak().nev() + ": " + ertekSzoveg + " (" + da.jegyekSzama() + " jegy)";
            oszlopok.add(new Oszlop(da.diak().nev(), da.atlag(), ertekSzoveg, tooltip, da.atlag() < 2.0));
        }

        String refCimke = osztalyAtlag.isPresent()
                ? "Osztályátlag: " + Formazo.atlag(osztalyAtlag.getAsDouble()) : "";
        String alsoSzoveg = jegyNelkuli > 0 ? "Jegy nélkül: " + jegyNelkuli + " fő" : "";

        return new DiagramAdat(oszlopok, 5, osztalyAtlag, refCimke, alsoSzoveg, "Nincs jegy");
    }

    private DiagramAdat osztalyokOsszeveteseDiagram(int tanarId, Integer tantargyIdVagyNull) {
        List<OsztalyAtlag> atlagok = osztalyAtlagok(tanarId, tantargyIdVagyNull);
        OptionalDouble osszesitett = tanarOsszesitettAtlaga(tanarId, tantargyIdVagyNull);

        List<Oszlop> oszlopok = new ArrayList<>();
        for (OsztalyAtlag oa : atlagok) {
            String cimke = oa.osztaly().nev();
            double ertek = oa.atlag().orElse(0);
            String ertekSzoveg;
            String tooltip;
            if (oa.atlag().isPresent()) {
                ertekSzoveg = Formazo.atlag(oa.atlag().getAsDouble()) + " (" + oa.jegyekSzama() + " jegy)";
                tooltip = cimke + ": " + ertekSzoveg;
            } else {
                ertekSzoveg = "–";
                tooltip = cimke + ": –";
            }
            boolean kiemelt = oa.atlag().isPresent() && oa.atlag().getAsDouble() < 2.0;
            oszlopok.add(new Oszlop(cimke, ertek, ertekSzoveg, tooltip, kiemelt));
        }

        String refCimke = osszesitett.isPresent()
                ? "Összesen: " + Formazo.atlag(osszesitett.getAsDouble()) : "";

        return new DiagramAdat(oszlopok, 5, osszesitett, refCimke, "", "Nincs jegy");
    }

    private List<Jegy> relevansJegyek(int tanarId, int osztalyId, Integer tantargyIdVagyNull) {
        Set<Integer> tantargyIds = tantargyIdHalmaz(tanarId, osztalyId, tantargyIdVagyNull);
        Set<Integer> diakIds = db.diakok.stream()
                .filter(d -> d.osztalyId() == osztalyId)
                .map(Diak::id)
                .collect(Collectors.toSet());
        return db.jegyek.stream()
                .filter(j -> diakIds.contains(j.diakId()) && tantargyIds.contains(j.tantargyId()))
                .toList();
    }

    private Set<Integer> tantargyIdHalmaz(int tanarId, int osztalyId, Integer tantargyIdVagyNull) {
        if (tantargyIdVagyNull != null) {
            return Set.of(tantargyIdVagyNull);
        }
        return db.tanitasok.stream()
                .filter(t -> t.tanarId() == tanarId && t.osztalyId() == osztalyId)
                .map(Tanitas::tantargyId)
                .collect(Collectors.toSet());
    }
}
