package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.model.Diak;
import hu.jegynyilvantarto.model.Jegy;
import hu.jegynyilvantarto.model.Tantargy;
import hu.jegynyilvantarto.service.ValidaciosHiba;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JegyDialog extends JDialog {

    private final Kontextus kontextus;
    private final JComboBox<Tantargy> tantargyCombo = new JComboBox<>();
    private final JComboBox<Integer> jegyCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
    private final JTextField datumMezo = new JTextField(10);
    private final JTextField megjegyzesMezo = new JTextField(20);
    private final JLabel hibaCimke = new JLabel(" ");
    private final Diak diak;
    private final Jegy szerkesztett;
    private boolean mentett = false;

    public JegyDialog(Frame owner, Kontextus kontextus, Diak diak,
                      List<Tantargy> tantargyak, Jegy szerkesztett) {
        super(owner, cim(kontextus, diak, szerkesztett), true);
        this.kontextus = kontextus;
        this.diak = diak;
        this.szerkesztett = szerkesztett;

        hibaCimke.setForeground(Color.RED);

        tantargyCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Tantargy t) setText(t.nev());
                return this;
            }
        });
        for (Tantargy t : tantargyak) {
            tantargyCombo.addItem(t);
        }

        datumMezo.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));

        if (szerkesztett != null) {
            tantargyCombo.setEnabled(false);
            for (int i = 0; i < tantargyCombo.getItemCount(); i++) {
                if (tantargyCombo.getItemAt(i).id() == szerkesztett.tantargyId()) {
                    tantargyCombo.setSelectedIndex(i);
                    break;
                }
            }
            jegyCombo.setSelectedItem(szerkesztett.ertek());
            datumMezo.setText(szerkesztett.datum());
            megjegyzesMezo.setText(szerkesztett.megjegyzes());
        }

        JPanel mezoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        mezoPanel.add(new JLabel("Tantárgy:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        mezoPanel.add(tantargyCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mezoPanel.add(new JLabel("Jegy:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        mezoPanel.add(jegyCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mezoPanel.add(new JLabel("Dátum:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        mezoPanel.add(datumMezo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mezoPanel.add(new JLabel("Megjegyzés:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        mezoPanel.add(megjegyzesMezo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mezoPanel.add(hibaCimke, gbc);

        JButton mentesGomb = new JButton("Mentés");
        JButton megseGomb = new JButton("Mégse");
        mentesGomb.addActionListener(e -> mentes());
        megseGomb.addActionListener(e -> dispose());

        JPanel gombPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        gombPanel.add(mentesGomb);
        gombPanel.add(megseGomb);

        setLayout(new BorderLayout(8, 8));
        add(mezoPanel, BorderLayout.CENTER);
        add(gombPanel, BorderLayout.SOUTH);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        getRootPane().setDefaultButton(mentesGomb);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setMinimumSize(new Dimension(400, getHeight()));
        setLocationRelativeTo(owner);
    }

    private static String cim(Kontextus kontextus, Diak diak, Jegy szerkesztett) {
        String diakInfo = diak.nev() + " (" + kontextus.admin.osztalyNev(diak.osztalyId()) + ")";
        return szerkesztett == null
                ? "Új jegy – " + diakInfo
                : "Jegy módosítása – " + diakInfo;
    }

    private void mentes() {
        try {
            Tantargy tantargy = (Tantargy) tantargyCombo.getSelectedItem();
            if (tantargy == null) {
                hibaCimke.setText("Válasszon tantárgyat!");
                return;
            }
            int ertek = (Integer) jegyCombo.getSelectedItem();
            String datum = datumMezo.getText().trim();
            String megjegyzes = megjegyzesMezo.getText().trim();

            if (szerkesztett == null) {
                kontextus.jegy.felvetel(kontextus.tanarId(), diak.id(), tantargy.id(),
                        ertek, datum, megjegyzes);
            } else {
                kontextus.jegy.modositas(szerkesztett.id(), ertek, datum, megjegyzes);
            }
            mentett = true;
            kontextus.adatValtozott();
            dispose();
        } catch (ValidaciosHiba e) {
            hibaCimke.setText(e.getMessage());
        } catch (Exception e) {
            HibaKezelo.mutat(this, e);
        }
    }

    public boolean isMentett() {
        return mentett;
    }
}
