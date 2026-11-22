package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.model.Diak;
import hu.jegynyilvantarto.model.Osztaly;
import hu.jegynyilvantarto.model.Tantargy;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

public class DiakokPanel extends JPanel {

    private final Kontextus kontextus;
    private final DiakTableModel tableModel = new DiakTableModel();
    private final JTable tabla;
    private final JegyekPanel jegyekPanel;
    private final JTextField keresomMezo = new JTextField(15);
    private final JComboBox<Object> osztalySzuro = new JComboBox<>();
    private final JButton modositasGomb = new JButton("Módosítás");
    private final JButton torlesGomb = new JButton("Törlés");

    public DiakokPanel(Kontextus kontextus) {
        this.kontextus = kontextus;
        setLayout(new BorderLayout());

        tabla = new JTable(tableModel);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setDefaultRenderer(String.class, new BukasSorRenderer());

        tabla.getColumnModel().getColumn(0).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(50);

        jegyekPanel = new JegyekPanel(kontextus);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tabla.getSelectedRow();
                if (row >= 0) {
                    int modelRow = tabla.convertRowIndexToModel(row);
                    Diak diak = tableModel.getDiak(modelRow);
                    jegyekPanel.setDiak(diak);
                    modositasGomb.setEnabled(true);
                    torlesGomb.setEnabled(true);
                } else {
                    jegyekPanel.setDiak(null);
                    modositasGomb.setEnabled(false);
                    torlesGomb.setEnabled(false);
                }
            }
        });

        osztalySzuro.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Osztaly o) setText(o.nev());
                else if (value instanceof String s) setText(s);
                return this;
            }
        });

        DocumentListener keresListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { szures(); }
            @Override
            public void removeUpdate(DocumentEvent e) { szures(); }
            @Override
            public void changedUpdate(DocumentEvent e) { szures(); }
        };
        keresomMezo.getDocument().addDocumentListener(keresListener);
        osztalySzuro.addActionListener(e -> szures());

        modositasGomb.setEnabled(false);
        torlesGomb.setEnabled(false);

        JButton ujDiakGomb = new JButton("Új diák");
        ujDiakGomb.addActionListener(e -> ujDiak());
        modositasGomb.addActionListener(e -> modositas());
        torlesGomb.addActionListener(e -> torles());

        JPanel keresPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        keresPanel.add(new JLabel("Keresés:"));
        keresPanel.add(keresomMezo);
        keresPanel.add(new JLabel("Osztály:"));
        keresPanel.add(osztalySzuro);

        JPanel gombPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gombPanel.add(ujDiakGomb);
        gombPanel.add(modositasGomb);
        gombPanel.add(torlesGomb);

        JPanel balPanel = new JPanel(new BorderLayout());
        balPanel.add(keresPanel, BorderLayout.NORTH);
        balPanel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        balPanel.add(gombPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, balPanel, jegyekPanel);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);

        frissit();
    }

    public void frissit() {
        int kivalasztottDiakId = -1;
        int row = tabla.getSelectedRow();
        if (row >= 0) {
            int modelRow = tabla.convertRowIndexToModel(row);
            kivalasztottDiakId = tableModel.getDiak(modelRow).id();
        }

        Object korabbiSzuro = osztalySzuro.getSelectedItem();
        osztalySzuro.removeAllItems();
        osztalySzuro.addItem("Összes");
        List<Osztaly> osztalyok = kontextus.kereses.tanarOsztalyai(kontextus.tanarId());
        for (Osztaly o : osztalyok) {
            osztalySzuro.addItem(o);
        }
        if (korabbiSzuro instanceof Osztaly korabbi) {
            for (int i = 0; i < osztalySzuro.getItemCount(); i++) {
                if (osztalySzuro.getItemAt(i) instanceof Osztaly o && o.id() == korabbi.id()) {
                    osztalySzuro.setSelectedIndex(i);
                    break;
                }
            }
        }

        szures();

        if (kivalasztottDiakId != -1) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getDiak(i).id() == kivalasztottDiakId) {
                    int viewRow = tabla.convertRowIndexToView(i);
                    tabla.setRowSelectionInterval(viewRow, viewRow);
                    break;
                }
            }
        }

        jegyekPanel.frissit();
    }

    private void szures() {
        String nevReszlet = keresomMezo.getText().trim();
        Object szuroErtek = osztalySzuro.getSelectedItem();
        Integer osztalyId = (szuroErtek instanceof Osztaly o) ? o.id() : null;

        List<Diak> diakok = kontextus.kereses.szur(kontextus.tanarId(), nevReszlet, osztalyId);

        List<DiakTableModel.Sor> sorok = new ArrayList<>();
        for (Diak d : diakok) {
            String osztalyNev = kontextus.admin.osztalyNev(d.osztalyId());
            List<Tantargy> tantargyak = kontextus.kereses.tanitottTantargyak(kontextus.tanarId(), d.osztalyId());
            String tantargyakStr = String.join(", ", tantargyak.stream().map(Tantargy::nev).toList());
            OptionalDouble atlag = kontextus.jegy.osszesitettAtlag(kontextus.tanarId(), d.id());
            boolean bukas = kontextus.jegy.bukasraAllBarmelyikbol(kontextus.tanarId(), d.id());
            sorok.add(new DiakTableModel.Sor(d, osztalyNev, tantargyakStr, atlag, bukas));
        }
        tableModel.setSorok(sorok);
    }

    private void ujDiak() {
        DiakDialog dialog = new DiakDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), kontextus, null);
        dialog.setVisible(true);
    }

    private void modositas() {
        int row = tabla.getSelectedRow();
        if (row < 0) return;
        int modelRow = tabla.convertRowIndexToModel(row);
        Diak diak = tableModel.getDiak(modelRow);
        DiakDialog dialog = new DiakDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), kontextus, diak);
        dialog.setVisible(true);
    }

    private void torles() {
        int row = tabla.getSelectedRow();
        if (row < 0) return;
        int modelRow = tabla.convertRowIndexToModel(row);
        Diak diak = tableModel.getDiak(modelRow);
        int jegyekSzama = kontextus.diak.jegyekSzama(diak.id());
        String uzenet = "Biztosan törli " + diak.nev() + " diákot?";
        if (jegyekSzama > 0) {
            uzenet += "\n(" + jegyekSzama + " jegye is törlődni fog)";
        }
        int valasz = JOptionPane.showConfirmDialog(this, uzenet,
                "Diák törlése", JOptionPane.YES_NO_OPTION);
        if (valasz == JOptionPane.YES_OPTION) {
            try {
                kontextus.diak.torles(diak.id());
                kontextus.adatValtozott();
            } catch (Exception e) {
                HibaKezelo.mutat(this, e);
            }
        }
    }
}
