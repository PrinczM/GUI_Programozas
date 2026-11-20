package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.service.ValidaciosHiba;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class HibaKezelo {

    public static void mutat(Component szulo, Exception e) {
        switch (e) {
            case ValidaciosHiba v ->
                    JOptionPane.showMessageDialog(szulo, v.getMessage(),
                            "Érvényesítési hiba", JOptionPane.WARNING_MESSAGE);
            case IOException io ->
                    JOptionPane.showMessageDialog(szulo,
                            "Fájlhiba: " + io.getMessage(),
                            "Fájlhiba", JOptionPane.ERROR_MESSAGE);
            default -> {
                e.printStackTrace();
                JOptionPane.showMessageDialog(szulo,
                        "Váratlan hiba: " + e.getMessage(),
                        "Hiba", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
