package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.service.DiagramAdat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class OszlopDiagramPanel extends JPanel implements Scrollable {

    private static final int SOR_MAGASSAG = 30;
    private static final int BAR_MAGASSAG = 24;
    private static final int FEJLEC_TERKOZ = 10;
    private static final int ALSO_TERKOZ = 60;
    private static final int CIMKE_SZELESSEG = 150;
    private static final int JOBB_MARGO = 60;
    private static final Color NORMAL_SZIN = new Color(76, 140, 100);
    private static final Color KIEMELT_SZIN = new Color(220, 50, 50);

    private DiagramAdat adat;

    public OszlopDiagramPanel() {
        setToolTipText("");
    }

    public void setAdat(DiagramAdat adat) {
        this.adat = adat;
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (adat == null || adat.oszlopok().isEmpty()) {
            Container parent = getParent();
            return new Dimension(400, parent != null ? parent.getHeight() : 200);
        }
        int szuksegesMagassag = adat.oszlopok().size() * SOR_MAGASSAG + FEJLEC_TERKOZ + ALSO_TERKOZ;
        Container parent = getParent();
        int parentMagassag = parent != null ? parent.getHeight() : 200;
        return new Dimension(400, Math.max(szuksegesMagassag, parentMagassag));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (adat == null || adat.oszlopok().isEmpty()) {
            String szoveg = (adat != null && adat.uresSzoveg() != null)
                    ? adat.uresSzoveg() : "Nincs adat.";
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(szoveg)) / 2;
            int y = getHeight() / 2;
            g2.drawString(szoveg, x, y);
            g2.dispose();
            return;
        }

        var oszlopok = adat.oszlopok();
        double maxErtek = adat.maxErtek();
        int diagramSzelesseg = getWidth() - CIMKE_SZELESSEG - JOBB_MARGO;
        if (diagramSzelesseg < 10) diagramSzelesseg = 10;

        for (int i = 0; i < oszlopok.size(); i++) {
            var oszlop = oszlopok.get(i);
            int y = FEJLEC_TERKOZ + i * SOR_MAGASSAG;
            FontMetrics fm = g2.getFontMetrics();

            g2.setColor(getForeground());
            String cimke = oszlop.cimke();
            int cimkeSzel = fm.stringWidth(cimke);
            int cimkeX = CIMKE_SZELESSEG - cimkeSzel - 5;
            if (cimkeX < 2) cimkeX = 2;
            g2.drawString(cimke, cimkeX, y + BAR_MAGASSAG / 2 + fm.getAscent() / 2);

            double arany = maxErtek > 0 ? oszlop.ertek() / maxErtek : 0;
            int barSzelesseg = (int) (arany * diagramSzelesseg);
            g2.setColor(oszlop.kiemelt() ? KIEMELT_SZIN : NORMAL_SZIN);
            g2.fillRect(CIMKE_SZELESSEG, y, barSzelesseg, BAR_MAGASSAG);

            g2.setColor(getForeground());
            g2.drawString(oszlop.ertekSzoveg(),
                    CIMKE_SZELESSEG + barSzelesseg + 5,
                    y + BAR_MAGASSAG / 2 + fm.getAscent() / 2);
        }

        if (adat.referencia().isPresent()) {
            double refErtek = adat.referencia().getAsDouble();
            double refArany = maxErtek > 0 ? refErtek / maxErtek : 0;
            int refX = CIMKE_SZELESSEG + (int) (refArany * diagramSzelesseg);

            g2.setColor(Color.GRAY);
            float[] dash = {5, 5};
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10, dash, 0));
            g2.drawLine(refX, 0, refX, FEJLEC_TERKOZ + oszlopok.size() * SOR_MAGASSAG);
            g2.setStroke(new BasicStroke(1));
            if (adat.referenciaCimke() != null) {
                g2.drawString(adat.referenciaCimke(), refX + 3, FEJLEC_TERKOZ - 2);
            }
        }

        int alsoY = FEJLEC_TERKOZ + oszlopok.size() * SOR_MAGASSAG + 5;
        g2.setColor(Color.GRAY);
        int tickSzam = 5;
        for (int i = 0; i <= tickSzam; i++) {
            double ertek = maxErtek * i / tickSzam;
            int x = CIMKE_SZELESSEG + (int) ((double) i / tickSzam * diagramSzelesseg);
            g2.drawLine(x, alsoY, x, alsoY + 5);
            String ertekStr;
            if (ertek == Math.floor(ertek) && ertek < 1000) {
                ertekStr = String.valueOf((int) ertek);
            } else {
                ertekStr = String.format("%.1f", ertek);
            }
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(ertekStr, x - fm.stringWidth(ertekStr) / 2, alsoY + 20);
        }

        if (adat.alsoSzoveg() != null && !adat.alsoSzoveg().isEmpty()) {
            FontMetrics fm = g2.getFontMetrics();
            int kozepX = CIMKE_SZELESSEG + diagramSzelesseg / 2;
            g2.setColor(getForeground());
            g2.drawString(adat.alsoSzoveg(),
                    kozepX - fm.stringWidth(adat.alsoSzoveg()) / 2,
                    alsoY + 40);
        }

        g2.dispose();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (adat == null || adat.oszlopok().isEmpty()) return null;
        int y = event.getY() - FEJLEC_TERKOZ;
        int index = y / SOR_MAGASSAG;
        if (index >= 0 && index < adat.oszlopok().size()) {
            return adat.oszlopok().get(index).tooltip();
        }
        return null;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return SOR_MAGASSAG;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
