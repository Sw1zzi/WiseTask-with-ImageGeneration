package ru.spb.ipo.generator.chess;

import ru.spb.ipo.generator.base.ui.BaseGeneratorUI;
import ru.spb.ipo.generator.base.ui.ConstraintPanel;
import ru.spb.ipo.generator.chess.ChessSetPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ChessFuncPanel extends ConstraintPanel {

    public static int ChooseY = 0;
    private static final long serialVersionUID = 1L;
    private JPanel jPanel = null;
    private JPanel jPanel1 = null;
    private JTextField xNumber = null;
    private JButton addRegion = null;
    private JLabel jLabel3 = null;
    private JComboBox jComboBox = null;
    private JComboBox jComboBox1 = null;
    private JButton addConstraint = null;
    private JPanel jPanel2 = null;
    private JPanel jPanel3 = null;
    private JPanel jPanelCheck = null;
    private JPanel jPanelkill = null;
    private JComboBox constraint = null;
    private JComboBox comdConstraint = null;
    private JComboBox constrNumber = null;
    public JCheckBox isSingle = null;
    public JCheckBox pob = null;
    public JCheckBox isKiller = null;
    public static boolean isDiag = false;
    public static boolean isPob = false;
    public static boolean isKill = false;

    /**
     * This is the default constructor
     */
    public ChessFuncPanel(BaseGeneratorUI gen) {
        super(gen);
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setSize(450, 138);
        this.setMinimumSize(new Dimension(440, 138));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createTitledBorder(null, "Выбор условий задачи:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
        this.add(getIsKiller(), null);
        this.add(getJPanel1(), null);
        this.add(getIsDiag(), null);
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            FlowLayout flowLayout1 = new FlowLayout();
            flowLayout1.setAlignment(FlowLayout.RIGHT);
            jLabel3 = new JLabel();
            jLabel3.setText("Ограничения  ");
            jPanel = new JPanel();
            jPanel.setLayout(flowLayout1);
            jPanel.add(jLabel3, null);
           
            jPanel.add(getJPanel2(), null);

            jPanel.add(getJPanel3(), null);
            jPanel.add(getAddConstraint(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.CENTER);
            jPanel1 = new JPanel();
            jPanel1.setLayout(flowLayout);
            
            jPanel1.add(getComdConstraint(), null);
            jPanel1.add(getConstrNumber(), null);
            jPanel1.add(getAddRegion(), null);
        }
        return jPanel1;
    }

    /**
     * This method initializes addRegion
     *
     * @return javax.swing.JButton
     */
    public static int numberFigur = -1;

    private JButton getAddRegion() {
        if (addRegion == null) {
            addRegion = new JButton();
            addRegion.setText("Добавить");
            addRegion.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Object cond = comdConstraint.getSelectedItem();
                    int index = constrNumber.getSelectedIndex();

                    if (index > 0) {
                        numberFigur = index;
                    } else {
                        try {
                            numberFigur = Integer.valueOf((String) constrNumber.getSelectedItem());
                        } catch (Exception ew) {
                        }
                        if (numberFigur < 0 || numberFigur > 50) {
                            JOptionPane optionPane = new JOptionPane("Количество фигур должно быть должно быть числом от 0 до 50",
                                    JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
                            optionPane.createDialog(ChessFuncPanel.this, "Неверное значение").setVisible(true);
                            return;
                        }
                    }
                    
                    isDiag = isSingle().isSelected();
                    isKill = isKiller().isSelected();
                    isPob = pob().isSelected();
                    if (!(ChessSetPanel.Hight.getText().equals(ChessSetPanel.Weight.getText())) && (isDiag||isPob)) {
                        JOptionPane optionPane = new JOptionPane("При выборе доски без диагонали должна быть только квадратная доска!",
                                JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
                        optionPane.createDialog(ChessFuncPanel.this, "Предупреждение").setVisible(true);
                    } else {
                        if (cond.equals("Слон")) {
                            addCondition(new ChessBox("Cлон", numberFigur, isDiag, isPob));
                            ChooseY = 1;

                        } else if (cond.equals("Конь")) {
                            addCondition(new ChessBox("Конь", numberFigur, isDiag, isPob));
                            ChooseY = 2;

                        } else if (cond.equals("Ладья")) {
                            addCondition(new ChessBox("Ладья", numberFigur, isDiag, isPob));
                            ChooseY = 3;

                        } else if (cond.equals("Ферзь")) {
                            addCondition(new ChessBox("Ферзь", numberFigur, isDiag, isPob));
                            ChooseY = 5;
                        }else if (cond.equals("Пешка")) {
                            JOptionPane optionPane = new JOptionPane("Пешка в нашей программе абсолютно нейтральна - то есть не имеет цвета, и может ходить и вверх и вниз по доске",
                                JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
                            optionPane.createDialog(ChessFuncPanel.this, "Кстати..").setVisible(true);
                            addCondition(new ChessBox("Пешка", numberFigur, isDiag, isPob));
                            ChooseY = 6;
                        }else if (cond.equals("Король")) {
                            addCondition(new ChessBox("Король", numberFigur, isDiag, isPob));
                            ChooseY = 7;
                        }
                    
                    }
                }
                    
            });
        }
        return addRegion;
    }

    private JPanel getIsDiag() {

        if (jPanelCheck == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            jPanelCheck = new JPanel();
            jPanelCheck.setLayout(flowLayout);
            jPanelCheck.add(isSingle(), null);
            jPanelCheck.add(pob(),null);
            jPanelkill.add(isKiller(), null);
        }
        return jPanelCheck;
    }
    
    private JPanel getIsKiller() {

        if (jPanelkill == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            jPanelkill = new JPanel();
            jPanelkill.setLayout(flowLayout);
            jPanelkill.add(isKiller(), null);
            
        }
        return jPanelkill;
    }
    
     public JCheckBox isKiller() {
        if (isKiller == null) {
            isKiller = new JCheckBox();
            isKiller.setText("Условие с бьющими фигурами");

            isKiller.setSelected(false);

        }
        return isKiller;
    }

    public JCheckBox isSingle() {
        if (isSingle == null) {
            isSingle = new JCheckBox();
            isSingle.setText("Без Главное диагонали");

            isSingle.setSelected(false);

        }
        return isSingle;
    }
      public JCheckBox pob() {
        if (pob == null) {
            pob = new JCheckBox();
            pob.setText("Без побочной диагонали");

            pob.setSelected(false);

        }
        return pob;
      }
    

    /**
     * This method initializes addConstraint
     *
     * @return javax.swing.JButton
     */
    private JButton getAddConstraint() {
        if (addConstraint == null) {
            addConstraint = new JButton();
            addConstraint.setText("Добавить");
        }
        return addConstraint;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            jPanel2 = new JPanel();
            jPanel2.setLayout(new FlowLayout());
        }
        return jPanel2;
    }

    /**
     * This method initializes jPanel3
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel3() {
        if (jPanel3 == null) {
            jPanel3 = new JPanel();
            jPanel3.setLayout(new FlowLayout());
        }
        return jPanel3;
    }

    /**
     * This method initializes constraint
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getConstraint() {
        if (constraint == null) {
            constraint = new JComboBox(new DefaultComboBoxModel(new String[]{""}));
            constraint.setEditable(true);
            constraint.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    Object item = e.getItem();
                    if (0 >= constraint.getSelectedIndex()) {

                        constraint.setEditable(true);
                    } else {

                        constraint.setEditable(false);
                    }
                    System.out.println("itemStateChanged()"); // TODO Auto-generated Event stub itemStateChanged()
                }
            });

        }
        return constraint;
    }

    /**
     * This method initializes comdConstraint
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getComdConstraint() {
        if (comdConstraint == null) {
            comdConstraint = new JComboBox(new DefaultComboBoxModel(new String[]{"Ладья", "Слон", "Конь", "Ферзь", "Пешка", "Король"}));
            comdConstraint.setToolTipText("Выберите фигуру");
        }
        return comdConstraint;
    }

    /**
     * This method initializes constrNumber
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getConstrNumber() {
        if (constrNumber == null) {
            constrNumber = new JComboBox(new DefaultComboBoxModel(new String[]{"", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"}));
            constrNumber.setEditable(true);
            constrNumber.setSelectedItem("2");
            constrNumber.setToolTipText("Выберите количество фигур");
           /* constrNumber.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    Object value = e.getItem();
                    if (constrNumber.getSelectedIndex() <= 0) {
                        constrNumber.setEditable(true);
                    } else {
                        constrNumber.setEditable(false);
                        if (comdConstraint.getSelectedIndex() == 3) {
                            constrNumber.setSelectedIndex(0);
                        }
                    }
                }
            });*/
        }
        return constrNumber;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
