package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.model.Diak;
import hu.jegynyilvantarto.model.Jegy;
import hu.jegynyilvantarto.model.Tantargy;
import hu.jegynyilvantarto.service.Formazo;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;

public class JegyekPanel extends JPanel {

    private final Kontextus kontextus;
    private Diak diak;
    private final JLabel fejlecCimke = new JLabel("Válasszon diákot a listából.");
    private final JegyekTableModel tableModel = new JegyekTableModel();
    private final JTable tabla = new JTable(tableModel);
    private final JButton ujJegyGomb = new JButton("Új jegy");
    private final JButton modositasGomb = new JButton("Módosítás");
    private final JButton torlesGomb = new JButton("Törlés");

    public JegyekPanel(Kontextus kontextus) {
        this.kontextus = kontextus;
        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        fejlecCimke.setFont(fejlecCimke.getFont().deriveFont(Font.BOLD, 14f));

        tabla.setDefaultRenderer(Object.class, new JegyekRenderer());
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tabla.getSelectedRow();
                boolean jegyKivalasztva = row >= 0 && !tableModel.isFejlec(row);
                modositasGomb.setEnabled(jegyKivalasztva);
                torlesGomb.setEnabled(jegyKivalasztva);
            }
        });

        tabla.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(200);

        ujJegyGomb.setEnabled(false);
        modositasGomb.setEnabled(false);
        torlesGomb.setEnabled(false);

        ujJegyGomb.addActionListener(e -> ujJegy());
        modositasGomb.addActionListener(e -> modositas());
        torlesGomb.addActionListener(e -> torles());

        JPanel gombPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gombPanel.add(ujJegyGomb);
        gombPanel.add(modositasGomb);
        gombPanel.add(torlesGomb);

        add(fejlecCimke, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(gombPanel, BorderLayout.SOUTH);
    }

    public void setDiak(Diak diak) {
        this.diak = diak;
        frissit();
    }

    public void frissit() {
        if (diak == null) {
            fejlecCimke.setText("Válasszon diákot a listából.");
            tableModel.clear();
            ujJegyGomb.setEnabled(false);
            modositasGomb.setEnabled(false);
            torlesGomb.setEnabled(false);
            return;
        }

        try {
            diak = kontextus.diak.diak(diak.id());
        } catch (Exception e) {
            diak = null;
            fejlecCimke.setText("Válasszon diákot a listából.");
            tableModel.clear();
            ujJegyGomb.setEnabled(false);
            return;
        }

        String osztalyNev = kontextus.admin.osztalyNev(diak.osztalyId());
        OptionalDouble osszAtlag = kontextus.jegy.osszesitettAtlag(kontextus.tanarId(), diak.id());
        fejlecCimke.setText(diak.nev() + " – " + osztalyNev + "    Átlag: " + Formazo.atlag(osszAtlag));

        List<Tantargy> tantargyak = kontextus.kereses.tanitottTantargyak(kontextus.tanarId(), diak.osztalyId());
        List<JegyekTableModel.Sor> sorok = new ArrayList<>();

        for (Tantargy t : tantargyak) {
            OptionalDouble atlag = kontextus.jegy.tantargyiAtlag(diak.id(), t.id());
            String fejlecSzoveg = t.nev() + "  –  Átlag: " + Formazo.atlag(atlag);
            sorok.add(new JegyekTableModel.Sor(true, fejlecSzoveg, atlag, null));

            List<Jegy> jegyek = kontextus.jegy.jegyek(diak.id(), t.id());
            jegyek.stream()
                    .sorted(Comparator.comparing(Jegy::datum))
                    .forEach(j -> sorok.add(new JegyekTableModel.Sor(false, null, OptionalDouble.empty(), j)));
        }

        tableModel.setSorok(sorok);
        ujJegyGomb.setEnabled(!tantargyak.isEmpty());
        modositasGomb.setEnabled(false);
        torlesGomb.setEnabled(false);
    }

    private void ujJegy() {
        if (diak == null) return;
        List<Tantargy> tantargyak = kontextus.kereses.tanitottTantargyak(kontextus.tanarId(), diak.osztalyId());
        JegyDialog dialog = new JegyDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                kontextus, diak, tantargyak, null);
        dialog.setVisible(true);
    }

    private void modositas() {
        int row = tabla.getSelectedRow();
        if (row < 0 || tableModel.isFejlec(row)) return;
        Jegy jegy = tableModel.getJegy(row);
        List<Tantargy> tantargyak = kontextus.kereses.tanitottTantargyak(kontextus.tanarId(), diak.osztalyId());
        JegyDialog dialog = new JegyDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                kontextus, diak, tantargyak, jegy);
        dialog.setVisible(true);
    }

    private void torles() {
        int row = tabla.getSelectedRow();
        if (row < 0 || tableModel.isFejlec(row)) return;
        Jegy jegy = tableModel.getJegy(row);
        int valasz = JOptionPane.showConfirmDialog(this,
                "Biztosan törli a jegyet?", "Törlés", JOptionPane.YES_NO_OPTION);
        if (valasz == JOptionPane.YES_OPTION) {
            try {
                kontextus.jegy.torles(jegy.id());
                kontextus.adatValtozott();
            } catch (Exception e) {
                HibaKezelo.mutat(this, e);
            }
        }
    }

    static class JegyekTableModel extends AbstractTableModel {

        record Sor(boolean fejlec, String fejlecSzoveg, OptionalDouble fejlecAtlag, Jegy jegy) {}

        private final List<Sor> sorok = new ArrayList<>();

        void setSorok(List<Sor> ujSorok) {
            sorok.clear();
            sorok.addAll(ujSorok);
            fireTableDataChanged();
        }

        void clear() {
            sorok.clear();
            fireTableDataChanged();
        }

        boolean isFejlec(int row) {
            return row >= 0 && row < sorok.size() && sorok.get(row).fejlec();
        }

        OptionalDouble getFejlecAtlag(int row) {
            return sorok.get(row).fejlecAtlag();
        }

        Jegy getJegy(int row) {
            return sorok.get(row).jegy();
        }

        @Override
        public int getRowCount() {
            return sorok.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Dátum";
                case 1 -> "Jegy";
                case 2 -> "Megjegyzés";
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int row, int column) {
            Sor sor = sorok.get(row);
            if (sor.fejlec()) {
                return column == 0 ? sor.fejlecSzoveg() : "";
            }
            Jegy j = sor.jegy();
            return switch (column) {
                case 0 -> j.datum();
                case 1 -> String.valueOf(j.ertek());
                case 2 -> j.megjegyzes();
                default -> "";
            };
        }
    }

    class JegyekRenderer extends DefaultTableCellRenderer {

        private static final Color FEJLEC_HATTER = new Color(230, 230, 240);
        private static final Color BUKAS_SZIN = new Color(180, 40, 40);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (tableModel.isFejlec(row)) {
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                if (!isSelected) {
                    c.setBackground(FEJLEC_HATTER);
                }
                OptionalDouble atlag = tableModel.getFejlecAtlag(row);
                if (atlag.isPresent() && atlag.getAsDouble() < 2.0) {
                    c.setForeground(BUKAS_SZIN);
                } else {
                    c.setForeground(table.getForeground());
                }
            } else {
                c.setFont(c.getFont().deriveFont(Font.PLAIN));
                if (!isSelected) {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
            }
            return c;
        }
    }
}
