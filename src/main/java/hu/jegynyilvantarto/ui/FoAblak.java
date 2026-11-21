package hu.jegynyilvantarto.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class FoAblak extends JFrame {

    private final Kontextus kontextus;
    private final DiakokPanel diakokPanel;
    private final StatisztikaPanel statisztikaPanel;
    private final AdminPanel adminPanel;

    public FoAblak(Kontextus kontextus) {
        this.kontextus = kontextus;

        String tanarNev = kontextus.admin.tanarNev(kontextus.tanarId());
        setTitle("Jegynyilvántartó – Bejelentkezve: " + tanarNev);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                kilep();
            }
        });

        JMenuBar menuSav = new JMenuBar();
        JMenu fajlMenu = new JMenu("Fájl");

        JMenuItem mentesItem = new JMenuItem("Mentés");
        mentesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        mentesItem.addActionListener(e -> {
            try {
                kontextus.ment();
            } catch (IOException ex) {
                HibaKezelo.mutat(FoAblak.this, ex);
            }
        });

        JMenuItem tanarValtasItem = new JMenuItem("Tanárváltás");
        tanarValtasItem.addActionListener(e -> tanarValtas());

        JMenuItem kilepesItem = new JMenuItem("Kilépés");
        kilepesItem.addActionListener(e -> kilep());

        fajlMenu.add(mentesItem);
        fajlMenu.add(tanarValtasItem);
        fajlMenu.addSeparator();
        fajlMenu.add(kilepesItem);
        menuSav.add(fajlMenu);
        setJMenuBar(menuSav);

        diakokPanel = new DiakokPanel(kontextus);
        statisztikaPanel = new StatisztikaPanel(kontextus);
        adminPanel = new AdminPanel(kontextus);

        JTabbedPane tabPanel = new JTabbedPane();
        tabPanel.addTab("Diákok", diakokPanel);
        tabPanel.addTab("Statisztika", statisztikaPanel);
        tabPanel.addTab("Adminisztráció", adminPanel);

        JLabel statuszCimke = new JLabel("Mentve");
        statuszCimke.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        kontextus.setStatuszCimke(statuszCimke);

        setLayout(new BorderLayout());
        add(tabPanel, BorderLayout.CENTER);
        add(statuszCimke, BorderLayout.SOUTH);

        kontextus.regisztralFrissites(() -> {
            diakokPanel.frissit();
            statisztikaPanel.frissit();
            adminPanel.frissit();
        });

        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void tanarValtas() {
        TanarValasztoDialog dialog = new TanarValasztoDialog(this, kontextus.admin);
        dialog.setVisible(true);
        int ujTanarId = dialog.getValasztottTanarId();
        if (ujTanarId != -1) {
            kontextus.setTanarId(ujTanarId);
            String tanarNev = kontextus.admin.tanarNev(ujTanarId);
            setTitle("Jegynyilvántartó – Bejelentkezve: " + tanarNev);
            diakokPanel.frissit();
            statisztikaPanel.frissit();
            adminPanel.frissit();
        }
    }

    private void kilep() {
        try {
            kontextus.ment();
        } catch (IOException e) {
            int valasz = JOptionPane.showConfirmDialog(this,
                    "A mentés nem sikerült. Kilép mentés nélkül?",
                    "Mentési hiba", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (valasz != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
        System.exit(0);
    }
}
