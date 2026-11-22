package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.model.Diak;
import hu.jegynyilvantarto.service.Formazo;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

public class DiakTableModel extends AbstractTableModel {

    private static final String[] OSZLOPOK = {"Név", "Osztály", "Tantárgyak", "Átlag", "Bukás"};

    record Sor(Diak diak, String osztalyNev, String tantargyak, OptionalDouble atlag, boolean bukas) {}

    private final List<Sor> sorok = new ArrayList<>();

    public void setSorok(List<Sor> ujSorok) {
        sorok.clear();
        sorok.addAll(ujSorok);
        fireTableDataChanged();
    }

    public Diak getDiak(int modelSor) {
        return sorok.get(modelSor).diak();
    }

    public boolean isBukas(int modelSor) {
        return sorok.get(modelSor).bukas();
    }

    @Override
    public int getRowCount() {
        return sorok.size();
    }

    @Override
    public int getColumnCount() {
        return OSZLOPOK.length;
    }

    @Override
    public String getColumnName(int column) {
        return OSZLOPOK[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return String.class;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Sor sor = sorok.get(row);
        return switch (column) {
            case 0 -> sor.diak().nev();
            case 1 -> sor.osztalyNev();
            case 2 -> sor.tantargyak();
            case 3 -> Formazo.atlag(sor.atlag());
            case 4 -> sor.bukas() ? "Igen" : "";
            default -> null;
        };
    }
}
