package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.model.*;
import hu.jegynyilvantarto.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminPanel extends JPanel {

    private final Kontextus kontextus;

    private final DefaultListModel<Osztaly> osztalyListModel = new DefaultListModel<>();
    private final JList<Osztaly> osztalyLista = new JList<>(osztalyListModel);

    private final DefaultListModel<Tantargy> tantargyListModel = new DefaultListModel<>();
    private final JList<Tantargy> tantargyLista = new JList<>(tantargyListModel);

    private final DefaultListModel<Tanar> tanarListModel = new DefaultListModel<>();
    private final JList<Tanar> tanarLista = new JList<>(tanarListModel);

    private final DefaultTableModel hozzarendelesTableModel = new DefaultTableModel(
            new String[]{"Osztály", "Tantárgy", "Tanár"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable hozzarendelesTabla = new JTable(hozzarendelesTableModel);

    private final JComboBox<Osztaly> hrOsztalyCombo = new JComboBox<>();
    private final JComboBox<Tantargy> hrTantargyCombo = new JComboBox<>();
    private final JComboBox<Tanar> hrTanarCombo = new JComboBox<>();

    private List<Tanitas> tanitasLista = new ArrayList<>();

    public AdminPanel(Kontextus kontextus) {
        this.kontextus = kontextus;
        setLayout(new GridLayout(2, 2, 8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(letrehozOsztalyPanel());
        add(letrehozTantargyPanel());
        add(letrehozTanarPanel());
        add(letrehozHozzarendelesPanel());

        frissit();
    }

    private JPanel letrehozOsztalyPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Osztályok"));

        osztalyLista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Osztaly o) setText(o.nev());
                return this;
            }
        });

        JButton ujGomb = new JButton("Új...");
        JButton torlesGomb = new JButton("Törlés");

        ujGomb.addActionListener(e -> {
            String nev = JOptionPane.showInputDialog(this,
                    "Osztály neve:", "Új osztály", JOptionPane.PLAIN_MESSAGE);
            if (nev != null && !nev.isBlank()) {
                try {
                    kontextus.admin.osztalyFelvetel(nev);
                    kontextus.adatValtozott();
                } catch (Exception ex) {
                    HibaKezelo.mutat(this, ex);
                }
            }
        });

        torlesGomb.addActionListener(e -> {
            Osztaly kivalasztott = osztalyLista.getSelectedValue();
            if (kivalasztott != null) {
                try {
                    kontextus.admin.osztalyTorles(kivalasztott.id());
                    kontextus.adatValtozott();
                } catch (Exception ex) {
                    HibaKezelo.mutat(this, ex);
                }
            }
        });

        JPanel gombPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gombPanel.add(ujGomb);
        gombPanel.add(torlesGomb);

        panel.add(new JScrollPane(osztalyLista), BorderLayout.CENTER);
        panel.add(gombPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel letrehozTantargyPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Tantárgyak"));

        tantargyLista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Tantargy t) setText(t.nev());
                return this;
            }
        });

        JButton ujGomb = new JButton("Új...");
        JButton torlesGomb = new JButton("Törlés");

        ujGomb.addActionListener(e -> {
            String nev = JOptionPane.showInputDialog(this,
                    "Tantárgy neve:", "Új tantárgy", JOptionPane.PLAIN_MESSAGE);
            if (nev != null && !nev.isBlank()) {
                try {
                    kontextus.admin.tantargyFelvetel(nev);
                    kontextus.adatValtozott();
                } catch (Exception ex) {
                    HibaKezelo.mutat(this, ex);
                }
            }
        });

        torlesGomb.addActionListener(e -> {
            Tantargy kivalasztott = tantargyLista.getSelectedValue();
            if (kivalasztott != null) {
                try {
                    kontextus.admin.tantargyTorles(kivalasztott.id());
                    kontextus.adatValtozott();
                } catch (Exception ex) {
                    HibaKezelo.mutat(this, ex);
                }
            }
        });

        JPanel gombPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gombPanel.add(ujGomb);
        gombPanel.add(torlesGomb);

        panel.add(new JScrollPane(tantargyLista), BorderLayout.CENTER);
        panel.add(gombPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel letrehozTanarPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Tanárok"));

        tanarLista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Tanar t) setText(t.nev());
                return this;
            }
        });

        JButton ujGomb = new JButton("Új...");
        JButton atnevezesGomb = new JButton("Átnevezés");
        JButton torlesGomb = new JButton("Törlés");

        tanarLista.addListSelectionListener(e -> {
            Tanar kivalasztott = tanarLista.getSelectedValue();
            torlesGomb.setEnabled(kivalasztott != null
                    && kivalasztott.id() != kontextus.tanarId());
        });

        ujGomb.addActionListener(e -> {
            String nev = JOptionPane.showInputDialog(this,
                    "Tanár neve:", "Új tanár", JOptionPane.PLAIN_MESSAGE);
            if (nev != null && !nev.isBlank()) {
                try {
                    kontextus.admin.tanarFelvetel(nev);
                    kontextus.adatValtozott();
                } catch (Exception ex) {
                    HibaKezelo.mutat(this, ex);
                }
            }
        });

        atnevezesGomb.addActionListener(e -> {
            Tanar kivalasztott = tanarLista.getSelectedValue();
            if (kivalasztott != null) {
                String ujNev = JOptionPane.showInputDialog(this, "Új név:", kivalasztott.nev());
                if (ujNev != null && !ujNev.isBlank()) {
                    try {
                        kontextus.admin.tanarAtnevezes(kivalasztott.id(), ujNev);
                        kontextus.adatValtozott();
                    } catch (Exception ex) {
                        HibaKezelo.mutat(this, ex);
                    }
                }
            }
        });

        torlesGomb.addActionListener(e -> {
            Tanar kivalasztott = tanarLista.getSelectedValue();
            if (kivalasztott != null && kivalasztott.id() != kontextus.tanarId()) {
                try {
                    kontextus.admin.tanarTorles(kivalasztott.id());
                    kontextus.adatValtozott();
                } catch (Exception ex) {
                    HibaKezelo.mutat(this, ex);
                }
            }
        });

        JPanel gombPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gombPanel.add(ujGomb);
        gombPanel.add(atnevezesGomb);
        gombPanel.add(torlesGomb);

        panel.add(new JScrollPane(tanarLista), BorderLayout.CENTER);
        panel.add(gombPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel letrehozHozzarendelesPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Hozzárendelések"));

        DefaultListCellRenderer comboRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Osztaly o) setText(o.nev());
                else if (value instanceof Tantargy t) setText(t.nev());
                else if (value instanceof Tanar t) setText(t.nev());
                return this;
            }
        };
        hrOsztalyCombo.setRenderer(comboRenderer);
        hrTantargyCombo.setRenderer(comboRenderer);
        hrTanarCombo.setRenderer(comboRenderer);

        JButton hozzaadGomb = new JButton("Hozzáad");
        JButton eltavolitGomb = new JButton("Eltávolít");

        hozzaadGomb.addActionListener(e -> {
            Osztaly o = (Osztaly) hrOsztalyCombo.getSelectedItem();
            Tantargy t = (Tantargy) hrTantargyCombo.getSelectedItem();
            Tanar ta = (Tanar) hrTanarCombo.getSelectedItem();
            if (o != null && t != null && ta != null) {
                try {
                    kontextus.admin.hozzarendel(ta.id(), o.id(), t.id());
                    kontextus.adatValtozott();
                } catch (Exception ex) {
                    HibaKezelo.mutat(this, ex);
                }
            }
        });

        eltavolitGomb.addActionListener(e -> {
            int row = hozzarendelesTabla.getSelectedRow();
            if (row >= 0 && row < tanitasLista.size()) {
                Tanitas tanitas = tanitasLista.get(row);
                int jegyekSzama = kontextus.admin.parJegyeinekSzama(
                        tanitas.osztalyId(), tanitas.tantargyId());
                if (jegyekSzama > 0) {
                    int valasz = JOptionPane.showConfirmDialog(this,
                            "Ehhez a hozzárendeléshez " + jegyekSzama
                                    + " jegy tartozik. Biztosan törli?",
                            "Megerősítés", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (valasz != JOptionPane.YES_OPTION) return;
                }
                try {
                    kontextus.admin.hozzarendelesTorles(
                            tanitas.osztalyId(), tanitas.tantargyId());
                    kontextus.adatValtozott();
                } catch (Exception ex) {
                    HibaKezelo.mutat(this, ex);
                }
            }
        });

        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboPanel.add(new JLabel("Osztály:"));
        comboPanel.add(hrOsztalyCombo);
        comboPanel.add(new JLabel("Tantárgy:"));
        comboPanel.add(hrTantargyCombo);
        comboPanel.add(new JLabel("Tanár:"));
        comboPanel.add(hrTanarCombo);

        JPanel gombPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gombPanel.add(hozzaadGomb);
        gombPanel.add(eltavolitGomb);

        JPanel alsoPanel = new JPanel(new BorderLayout());
        alsoPanel.add(comboPanel, BorderLayout.NORTH);
        alsoPanel.add(gombPanel, BorderLayout.SOUTH);

        panel.add(new JScrollPane(hozzarendelesTabla), BorderLayout.CENTER);
        panel.add(alsoPanel, BorderLayout.SOUTH);
        return panel;
    }

    public void frissit() {
        osztalyListModel.clear();
        for (Osztaly o : kontextus.admin.osztalyok()) {
            osztalyListModel.addElement(o);
        }

        tantargyListModel.clear();
        for (Tantargy t : kontextus.admin.tantargyak()) {
            tantargyListModel.addElement(t);
        }

        tanarListModel.clear();
        for (Tanar t : kontextus.admin.tanarok()) {
            tanarListModel.addElement(t);
        }

        tanitasLista = kontextus.admin.tanitasok();
        hozzarendelesTableModel.setRowCount(0);
        for (Tanitas t : tanitasLista) {
            hozzarendelesTableModel.addRow(new Object[]{
                    kontextus.admin.osztalyNev(t.osztalyId()),
                    kontextus.admin.tantargyNev(t.tantargyId()),
                    kontextus.admin.tanarNev(t.tanarId())
            });
        }

        hrOsztalyCombo.removeAllItems();
        for (Osztaly o : kontextus.admin.osztalyok()) {
            hrOsztalyCombo.addItem(o);
        }
        hrTantargyCombo.removeAllItems();
        for (Tantargy t : kontextus.admin.tantargyak()) {
            hrTantargyCombo.addItem(t);
        }
        hrTanarCombo.removeAllItems();
        for (Tanar t : kontextus.admin.tanarok()) {
            hrTanarCombo.addItem(t);
        }
    }
}
