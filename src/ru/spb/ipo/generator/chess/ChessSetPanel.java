package ru.spb.ipo.generator.chess;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import ru.spb.ipo.generator.base.FuncUtil;
import ru.spb.ipo.generator.base.ui.BaseGeneratorUI;
import ru.spb.ipo.generator.base.ui.ConstraintPanel;
import ru.spb.ipo.generator.base.ui.ValidatedTextField;
import ru.spb.ipo.generator.chess.ChessFuncPanel;

public class ChessSetPanel extends ConstraintPanel {

    private static final long serialVersionUID = 1L;
    private JPanel jPanel = null;
    private JPanel jPanel1 = null;
    private JPanel jPanel2 = null;
    public static JTextField Hight = null;
    private JLabel jLabel = null;
    private JLabel jLabel1 = null;
    public static JTextField Weight = null;
    


    /**
     * This is the default constructor
     */
    public ChessSetPanel(BaseGeneratorUI generator) {
        super(generator);
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setSize(475, 74);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createTitledBorder(null, "Параметры доски:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
        this.add(getJPanel(), null);
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BoxLayout(getJPanel(), BoxLayout.X_AXIS));
            jPanel.add(getJPanel1(), null);
            jPanel.add(getJPanel2(), null);
        }
        return jPanel;
    }

    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            FlowLayout flowLayout1 = new FlowLayout();
            flowLayout1.setAlignment(FlowLayout.LEFT);
            jLabel = new JLabel();
            jLabel.setText("Высота доски");
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints.weightx = 1.0;
            jPanel1 = new JPanel();
            jPanel1.setLayout(flowLayout1);
            jPanel1.add(jLabel, null);
            jPanel1.add(getHight(), null);
        }
        return jPanel1;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.RIGHT);
            jLabel1 = new JLabel();
            jLabel1.setText("Длина доски");

            jPanel2 = new JPanel();
            jPanel2.setLayout(flowLayout);
            jPanel2.add(jLabel1, null);
            jPanel2.add(getDlina(), null);
        }
        return jPanel2;
    }

    /**
     * This method initializes xNumber
     *
     * @return javax.swing.JComboBox
     */
    public JTextField getHight() {
        if (Hight == null) {
            Hight = new ValidatedTextField(50);
            Hight.setColumns(3);
            Hight.setText("6");
            Hight.setToolTipText("Введите число от 0 до 50");
        }
        return Hight;
    }

    /**
     * This method initializes resultNumber
     *
     * @return javax.swing.JTextField
     */
    public JTextField getDlina() {
        if (Weight == null) {
            Weight = new ValidatedTextField(50);
            Weight.setColumns(3);
            Weight.setText("5");
            Weight.setToolTipText("Введите число от 0 до 50");
            
        }
        return Weight;
    }

    public void fillMaps(Map source, Map func, Map task) {
        source.put("resultX", Weight.getText());
        source.put("nabor", Hight.getText());
        String function = (String) func.get("function");
        if (function == null) {
            function = "";
        }


    }

}
