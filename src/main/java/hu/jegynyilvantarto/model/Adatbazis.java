package hu.jegynyilvantarto.model;

import java.util.ArrayList;
import java.util.List;

public class Adatbazis {
    public List<Osztaly> osztalyok = new ArrayList<>();
    public List<Tantargy> tantargyak = new ArrayList<>();
    public List<Tanar> tanarok = new ArrayList<>();
    public List<Tanitas> tanitasok = new ArrayList<>();
    public List<Diak> diakok = new ArrayList<>();
    public List<Jegy> jegyek = new ArrayList<>();

    public static int kovetkezoId(List<? extends Azonosithato> lista) {
        return lista.stream().mapToInt(Azonosithato::id).max().orElse(0) + 1;
    }
}
