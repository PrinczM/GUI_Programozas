package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.model.Adatbazis;
import hu.jegynyilvantarto.service.*;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Kontextus {

    public final AdminService admin;
    public final DiakService diak;
    public final JegyService jegy;
    public final KeresesService kereses;
    public final StatisztikaService statisztika;

    private int tanarId;
    private final MentesService mentes;
    private final Adatbazis adatbazis;
    private final Path mentesUtvonal;
    private final List<Runnable> frissitok = new ArrayList<>();
    private JLabel statuszCimke;

    public Kontextus(Adatbazis adatbazis, MentesService mentes, Path mentesUtvonal) {
        this.adatbazis = adatbazis;
        this.mentes = mentes;
        this.mentesUtvonal = mentesUtvonal;
        this.admin = new AdminService(adatbazis);
        this.kereses = new KeresesService(adatbazis);
        this.diak = new DiakService(adatbazis);
        this.jegy = new JegyService(adatbazis, kereses);
        this.statisztika = new StatisztikaService(adatbazis, kereses, jegy);
    }

    public int tanarId() {
        return tanarId;
    }

    public void setTanarId(int tanarId) {
        this.tanarId = tanarId;
    }

    public void setStatuszCimke(JLabel statuszCimke) {
        this.statuszCimke = statuszCimke;
    }

    public void regisztralFrissites(Runnable r) {
        frissitok.add(r);
    }

    public void ment() throws IOException {
        mentes.ment(adatbazis, mentesUtvonal);
        if (statuszCimke != null) {
            String ido = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            statuszCimke.setText("Mentve " + ido);
        }
    }

    public void adatValtozott() {
        try {
            ment();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Mentési hiba: " + e.getMessage(),
                    "Hiba", JOptionPane.ERROR_MESSAGE);
        }
        for (Runnable r : frissitok) {
            r.run();
        }
    }
}
