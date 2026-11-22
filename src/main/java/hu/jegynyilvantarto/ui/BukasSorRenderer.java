package hu.jegynyilvantarto.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class BukasSorRenderer extends DefaultTableCellRenderer {

    private static final Color BUKAS_SZIN = new Color(255, 200, 200);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            int modelRow = table.convertRowIndexToModel(row);
            DiakTableModel model = (DiakTableModel) table.getModel();
            if (model.isBukas(modelRow)) {
                c.setBackground(BUKAS_SZIN);
            } else {
                c.setBackground(table.getBackground());
            }
        }
        return c;
    }
}
