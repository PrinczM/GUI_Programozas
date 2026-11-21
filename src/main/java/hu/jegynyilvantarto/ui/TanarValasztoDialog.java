package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.model.Tanar;
import hu.jegynyilvantarto.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class TanarValasztoDialog extends JDialog {

    private final AdminService adminService;
    private final DefaultListModel<Tanar> listModel = new DefaultListModel<>();
    private final JList<Tanar> tanarLista = new JList<>(listModel);
    private int valasztottTanarId = -1;

    public TanarValasztoDialog(Frame owner, AdminService adminService) {
        super(owner, "Tanár kiválasztása", true);
        this.adminService = adminService;

        tanarLista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tanarLista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Tanar t) {
                    setText(t.nev());
                }
                return this;
            }
        });

        JButton belepesGomb = new JButton("Belépés");
        belepesGomb.setEnabled(false);
        JButton ujTanarGomb = new JButton("Új tanár...");
        JButton megseGomb = new JButton("Mégse");

        tanarLista.addListSelectionListener(e ->
                belepesGomb.setEnabled(tanarLista.getSelectedValue() != null));

        tanarLista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tanarLista.getSelectedValue() != null) {
                    belepes();
                }
            }
        });

        belepesGomb.addActionListener(e -> belepes());
        ujTanarGomb.addActionListener(e -> ujTanar());
        megseGomb.addActionListener(e -> dispose());

        JPanel gombokPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        gombokPanel.add(belepesGomb);
        gombokPanel.add(ujTanarGomb);
        gombokPanel.add(megseGomb);

        setLayout(new BorderLayout(8, 8));
        add(new JScrollPane(tanarLista), BorderLayout.CENTER);
        add(gombokPanel, BorderLayout.SOUTH);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        listaFrissites();

        setSize(350, 300);
        setLocationRelativeTo(owner);

        if (listModel.isEmpty()) {
            SwingUtilities.invokeLater(this::ujTanar);
        }
    }

    private void listaFrissites() {
        listModel.clear();
        List<Tanar> tanarok = adminService.tanarok();
        tanarok.stream()
                .sorted((a, b) -> a.nev().compareToIgnoreCase(b.nev()))
                .forEach(listModel::addElement);
    }

    private void belepes() {
        Tanar selected = tanarLista.getSelectedValue();
        if (selected != null) {
            valasztottTanarId = selected.id();
            dispose();
        }
    }

    private void ujTanar() {
        String nev = JOptionPane.showInputDialog(this, "Tanár neve:", "Új tanár", JOptionPane.PLAIN_MESSAGE);
        if (nev != null && !nev.isBlank()) {
            try {
                adminService.tanarFelvetel(nev);
                listaFrissites();
            } catch (Exception e) {
                HibaKezelo.mutat(this, e);
            }
        }
    }

    public int getValasztottTanarId() {
        return valasztottTanarId;
    }
}
