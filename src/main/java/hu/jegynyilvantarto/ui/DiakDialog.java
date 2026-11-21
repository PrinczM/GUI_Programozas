package hu.jegynyilvantarto.ui;

import hu.jegynyilvantarto.model.Diak;
import javax.swing.*;
import java.awt.*;

public class DiakDialog extends JDialog {
    public DiakDialog(Frame owner, Kontextus kontextus, Diak szerkesztett) {
        super(owner, true);
    }
    public boolean isMentett() { return false; }
}
