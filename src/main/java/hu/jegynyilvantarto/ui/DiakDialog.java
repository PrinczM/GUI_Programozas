package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.model.Diak;
import hu.jegynyilvantarto.model.Osztaly;
import hu.jegynyilvantarto.service.ValidaciosHiba;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DiakDialog extends JDialog {

    private final Kontextus kontextus;
    private final JTextField nevMezo = new JTextField(20);
    private final JComboBox<Osztaly> osztalyCombo = new JComboBox<>();
    private final JLabel hibaCimke = new JLabel(" ");
    private final Diak szerkesztett;
    private boolean mentett = false;

    public DiakDialog(Frame owner, Kontextus kontextus, Diak szerkesztett) {
        super(owner, szerkesztett == null ? "Új diák" : "Diák módosítása", true);
        this.kontextus = kontextus;
        this.szerkesztett = szerkesztett;

        hibaCimke.setForeground(Color.RED);

        osztalyCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Osztaly o) setText(o.nev());
                return this;
            }
        });

        List<Osztaly> osztalyok = kontextus.kereses.tanarOsztalyai(kontextus.tanarId());
        for (Osztaly o : osztalyok) {
            osztalyCombo.addItem(o);
        }

        if (szerkesztett != null) {
            nevMezo.setText(szerkesztett.nev());
            for (int i = 0; i < osztalyCombo.getItemCount(); i++) {
                if (osztalyCombo.getItemAt(i).id() == szerkesztett.osztalyId()) {
                    osztalyCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        JPanel mezoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        mezoPanel.add(new JLabel("Név:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        mezoPanel.add(nevMezo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mezoPanel.add(new JLabel("Osztály:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        mezoPanel.add(osztalyCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
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
        setMinimumSize(new Dimension(350, getHeight()));
        setLocationRelativeTo(owner);
    }

    private void mentes() {
        try {
            String nev = nevMezo.getText().trim();
            Osztaly osztaly = (Osztaly) osztalyCombo.getSelectedItem();
            if (osztaly == null) {
                hibaCimke.setText("Válasszon osztályt!");
                return;
            }

            if (szerkesztett == null) {
                if (kontextus.diak.vanAzonosNevu(nev, osztaly.id(), null)) {
                    int valasz = JOptionPane.showConfirmDialog(this,
                            "Már létezik ilyen nevű diák ebben az osztályban. Folytatja?",
                            "Megerősítés", JOptionPane.YES_NO_OPTION);
                    if (valasz != JOptionPane.YES_OPTION) return;
                }
                kontextus.diak.felvetel(nev, osztaly.id());
            } else {
                kontextus.diak.modositas(szerkesztett.id(), nev, osztaly.id());
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
