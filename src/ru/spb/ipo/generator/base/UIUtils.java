package ru.spb.ipo.generator.base;

import ru.spb.ipo.generator.basket.BasketXmlGenerator;

import javax.swing.*;
import java.awt.*;

public class UIUtils {

    public static void enableAll(JComponent comp, boolean enable) {
        comp.setEnabled(enable);
        Component[] cs = comp.getComponents();
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] instanceof JComponent) {
                ((JComponent) cs[i]).setEnabled(enable);
                enableAll(((JComponent) cs[i]), enable);
            }
        }
    }

    public static Color getColor(int color) {
        return BasketXmlGenerator.colors[color];
    }
}
