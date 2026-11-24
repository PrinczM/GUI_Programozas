package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.model.Diak;
import hu.jegynyilvantarto.model.Osztaly;
import hu.jegynyilvantarto.model.Tantargy;
import hu.jegynyilvantarto.service.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StatisztikaPanel extends JPanel {

    private final Kontextus kontextus;
    private final JComboBox<String> nezetCombo = new JComboBox<>();
    private final JComboBox<Object> osztalyCombo = new JComboBox<>();
    private final JComboBox<Object> tantargyCombo = new JComboBox<>();
    private final JLabel atlagCimke = new JLabel(" ");
    private final JLabel legjobbCimke = new JLabel(" ");
    private final JLabel bukasCimke = new JLabel(" ");
    private final DefaultListModel<String> bukasListModel = new DefaultListModel<>();
    private final JList<String> bukasLista = new JList<>(bukasListModel);
    private final OszlopDiagramPanel diagramPanel = new OszlopDiagramPanel();
    private final JLabel uresCimke = new JLabel("Nincs hozzárendelt osztály.");
    private final JPanel tartalomPanel = new JPanel(new BorderLayout());
    private boolean frissitesFolyamatban = false;

    public StatisztikaPanel(Kontextus kontextus) {
        this.kontextus = kontextus;
        setLayout(new BorderLayout());

        nezetCombo.addItem("Jegyeloszlás");
        nezetCombo.addItem("Diákok átlaga");
        nezetCombo.addItem("Osztályok összevetése");

        osztalyCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Osztaly o) setText(o.nev());
                return this;
            }
        });
        tantargyCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Tantargy t) setText(t.nev());
                else if (value instanceof String s) setText(s);
                return this;
            }
        });

        nezetCombo.addActionListener(e -> {
            if (!frissitesFolyamatban) frissit();
        });
        osztalyCombo.addActionListener(e -> {
            if (!frissitesFolyamatban) frissit();
        });
        tantargyCombo.addActionListener(e -> {
            if (!frissitesFolyamatban) frissitStatisztika();
        });

        JPanel szuroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        szuroPanel.add(new JLabel("Nézet:"));
        szuroPanel.add(nezetCombo);
        szuroPanel.add(new JLabel("Osztály:"));
        szuroPanel.add(osztalyCombo);
        szuroPanel.add(new JLabel("Tantárgy:"));
        szuroPanel.add(tantargyCombo);

        JPanel statPanel = new JPanel();
        statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.Y_AXIS));
        statPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        statPanel.add(atlagCimke);
        statPanel.add(Box.createVerticalStrut(8));
        statPanel.add(legjobbCimke);
        statPanel.add(Box.createVerticalStrut(8));
        statPanel.add(bukasCimke);
        statPanel.add(Box.createVerticalStrut(4));
        statPanel.add(new JScrollPane(bukasLista));
        statPanel.setPreferredSize(new Dimension(250, 0));

        JScrollPane diagramScroll = new JScrollPane(diagramPanel);

        tartalomPanel.add(statPanel, BorderLayout.WEST);
        tartalomPanel.add(diagramScroll, BorderLayout.CENTER);

        uresCimke.setHorizontalAlignment(SwingConstants.CENTER);
        uresCimke.setFont(uresCimke.getFont().deriveFont(16f));

        add(szuroPanel, BorderLayout.NORTH);
        add(tartalomPanel, BorderLayout.CENTER);

        frissit();
    }

    public void frissit() {
        frissitesFolyamatban = true;

        List<Osztaly> osztalyok = kontextus.kereses.tanarOsztalyai(kontextus.tanarId());

        if (osztalyok.isEmpty()) {
            remove(tartalomPanel);
            add(uresCimke, BorderLayout.CENTER);
            revalidate();
            repaint();
            frissitesFolyamatban = false;
            return;
        }
        remove(uresCimke);
        add(tartalomPanel, BorderLayout.CENTER);

        int nezetIndex = nezetCombo.getSelectedIndex();
        boolean osztalyokOsszevetese = nezetIndex == 2;

        Object korabbiOsztaly = osztalyCombo.getSelectedItem();
        osztalyCombo.removeAllItems();
        for (Osztaly o : osztalyok) {
            osztalyCombo.addItem(o);
        }
        osztalyCombo.setEnabled(!osztalyokOsszevetese);
        if (korabbiOsztaly instanceof Osztaly ko) {
            for (int i = 0; i < osztalyCombo.getItemCount(); i++) {
                if (osztalyCombo.getItemAt(i) instanceof Osztaly o && o.id() == ko.id()) {
                    osztalyCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        Object korabbiTantargy = tantargyCombo.getSelectedItem();
        tantargyCombo.removeAllItems();
        tantargyCombo.addItem("Összes");
        List<Tantargy> tantargyak;
        if (osztalyokOsszevetese) {
            tantargyak = kontextus.kereses.tanarTantargyai(kontextus.tanarId());
        } else {
            Osztaly kivalasztottOsztaly = getKivalasztottOsztaly();
            if (kivalasztottOsztaly != null) {
                tantargyak = kontextus.kereses.tanitottTantargyak(
                        kontextus.tanarId(), kivalasztottOsztaly.id());
            } else {
                tantargyak = List.of();
            }
        }
        for (Tantargy t : tantargyak) {
            tantargyCombo.addItem(t);
        }
        if (korabbiTantargy instanceof Tantargy kt) {
            for (int i = 0; i < tantargyCombo.getItemCount(); i++) {
                if (tantargyCombo.getItemAt(i) instanceof Tantargy t && t.id() == kt.id()) {
                    tantargyCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        frissitesFolyamatban = false;
        frissitStatisztika();
    }

    private void frissitStatisztika() {
        if (frissitesFolyamatban) return;

        int nezetIndex = nezetCombo.getSelectedIndex();
        Nezet nezet = switch (nezetIndex) {
            case 0 -> Nezet.JEGYELOSZLAS;
            case 1 -> Nezet.DIAKOK_ATLAGA;
            default -> Nezet.OSZTALYOK_OSSZEVETESE;
        };

        Osztaly osztaly = getKivalasztottOsztaly();
        Integer osztalyId = (osztaly != null && nezetIndex != 2) ? osztaly.id() : null;
        Integer tantargyId = getKivalasztottTantargyId();

        if (osztalyId != null) {
            var stat = kontextus.statisztika.szamol(kontextus.tanarId(), osztalyId, tantargyId);
            atlagCimke.setText("Osztályátlag: " + Formazo.atlag(stat.atlag())
                    + " (" + stat.jegyekSzama() + " jegy)");
            if (stat.legjobb().isPresent()) {
                var dAtlag = stat.legjobb().get();
                legjobbCimke.setText("Legjobb diák: " + dAtlag.diak().nev()
                        + " (" + Formazo.atlag(dAtlag.atlag()) + ")");
            } else {
                legjobbCimke.setText("Legjobb diák: –");
            }
            bukasCimke.setText("Bukásra áll: " + stat.bukasraAllok().size() + " fő");
            bukasListModel.clear();
            for (Diak d : stat.bukasraAllok()) {
                bukasListModel.addElement(d.nev());
            }
        } else {
            atlagCimke.setText("Osztályátlag: –");
            legjobbCimke.setText("Legjobb diák: –");
            bukasCimke.setText("Bukásra áll: –");
            bukasListModel.clear();
        }

        var diagramAdat = kontextus.statisztika.diagram(
                nezet, kontextus.tanarId(), osztalyId, tantargyId);
        diagramPanel.setAdat(diagramAdat);

        revalidate();
        repaint();
    }

    private Osztaly getKivalasztottOsztaly() {
        Object item = osztalyCombo.getSelectedItem();
        return (item instanceof Osztaly o) ? o : null;
    }

    private Integer getKivalasztottTantargyId() {
        Object item = tantargyCombo.getSelectedItem();
        return (item instanceof Tantargy t) ? t.id() : null;
    }
}
