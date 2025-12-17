package ru.spb.ipo.generator.base.ui;

import javax.swing.*;
import java.awt.*;

public class ValidatedTextField extends JTextField {

    private int max;

    public ValidatedTextField(int max) {
        this.max = max;
        setInputVerifier(new InputVerifier() {
            public boolean verify(JComponent input) {
                boolean isValid = true;
                try {
                    int value = Integer.valueOf(getText());
                    if (0 > value || ValidatedTextField.this.max < value) {
                        isValid = false;
                    }
                } catch (Exception e) {
                    isValid = false;
                }
                if (!isValid) {
                    Component par = ValidatedTextField.this;
                    while (par.getParent() != null) {
                        par = par.getParent();
                    }

                    final JFrame frame = (JFrame) par;

                    JOptionPane optionPane = new JOptionPane("Значение поля должно быть числом от 0 до " + ValidatedTextField.this.max,
                            JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
                    optionPane.createDialog(frame, "Неверное значение").setVisible(true);
                }
                return isValid;
            }
        });
    }
}