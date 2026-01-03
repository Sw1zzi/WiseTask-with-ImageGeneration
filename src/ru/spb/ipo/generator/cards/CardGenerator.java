package ru.spb.ipo.generator.cards;

import ru.spb.ipo.generator.base.BaseGenerator;
import ru.spb.ipo.generator.base.ListElement;
import ru.spb.ipo.generator.base.ui.BaseGeneratorUI;
import ru.spb.ipo.generator.base.ui.ConstraintPanel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.SchemaOutputResolver;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ru.spb.ipo.generator.chess.ChessFuncPanel;

public class CardGenerator extends BaseGeneratorUI {

    private JPanel collectionSizePanel = null;

    protected JLabel packSizeLabel = null;

    private JPanel packSizePanel = null;
    protected ArrayList imagesList = new ArrayList();
    private JComboBox coloda = null;

    protected JLabel collectionSizeLabel = null;

    private JButton addImageButton = null;

    private JComboBox nabor = null;

    private JPanel cardQuantityPanel = null;

    private JComboBox value = null;
    public Map setPicture = new HashMap<String, String>();

    private JButton addQuantityCardButton = null;
    private JFileChooser imageChooser;
    private JComboBox condition = null;
    private JPanel imageListPanel = null;
    private JLabel cardQuantityLabel = null;

    private JComboBox type = null;
    private int counter = 0;
    private JPanel packIncludePanel = null;

    private JLabel packIncludeLabel = null;

    private JButton addIncludeConditionButton = null;

    private JComboBox cardValue = null;

    private JComboBox cardType = null;

    private static CardGenerator instance = null;

    public CardGenerator() {
        super();
        initialize();
    }

    protected void fillParameters(Map source, Map func, Map task) {
        source.put("coloda", Integer.valueOf(((String) coloda.getSelectedItem())).intValue() == 36 ? "5" : "1");
        source.put("colodaDesc", coloda.getSelectedItem());
        source.put("nabor", nabor.getSelectedItem());
    }

    public BaseGenerator createGenerator(Map source, Map func, Map task) {
        return new BaseGenerator(source, func, task);
    }


    public JFileChooser getImageChooser() {
        soMuch();
        if (imageChooser == null) {
            imageChooser = new JFileChooser(new File("." + File.separator + "tasks" + File.separator + "imgs"));
            imageChooser.setDialogTitle("Выберите файл-картинку к задаче...");
            imageChooser.setControlButtonsAreShown(true);
            imageChooser.removeChoosableFileFilter(imageChooser.getChoosableFileFilters()[0]);

//            hide(imageChooser.getComponents(), 0);
            imageChooser.setFileFilter(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) return true;
                    if (pathname.isFile()) {
                        String name = pathname.getName().toLowerCase();
                        if (name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                            return true;
                        }
                    }
                    return false;
                }

                public String getDescription() {
                    return "картинки (*.png, *.gif, *.jpg, *.jpeg)";
                }
            });
        }


        return imageChooser;
    }

    /**
     * This method initializes setPanel
     *
     * @return javax.swing.JPanel
     */
    protected ConstraintPanel getSetPanel() {
        if (setPanel == null) {
            setPanel = new ConstraintPanel(this);
            setPanel.setLayout(new BoxLayout(getSetPanel(), BoxLayout.X_AXIS));
            setPanel.add(getPackSizeLabel(), null);
            setPanel.add(getCollectionSizePanel(), null);
        }
        return setPanel;
    }

    /**
     * This method initializes collectionSizePanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getCollectionSizePanel() {
        if (collectionSizePanel == null) {
            FlowLayout flowLayout1 = new FlowLayout();
            flowLayout1.setAlignment(FlowLayout.RIGHT);
            collectionSizeLabel = new JLabel();
            collectionSizeLabel.setText("Размер набора");
            collectionSizePanel = new JPanel();
            collectionSizePanel.setLayout(flowLayout1);
            collectionSizePanel.add(collectionSizeLabel, null);
            collectionSizePanel.add(getNabor(), null);
        }
        return collectionSizePanel;
    }

    /**
     * This method initializes packSizePanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPackSizeLabel() {
        if (packSizePanel == null) {
            packSizeLabel = new JLabel();
            packSizeLabel.setText("Размер колоды ");
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            packSizePanel = new JPanel();
            packSizePanel.setLayout(flowLayout);
            packSizePanel.add(packSizeLabel, null);
            packSizePanel.add(getColoda(), null);
        }
        return packSizePanel;
    }

    /**
     * This method initializes coloda
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getColoda() {
        if (coloda == null) {
            coloda = new JComboBox();
            coloda.setModel(new DefaultComboBoxModel(new String[]{"36", "52"}));
        }
        return coloda;
    }

    /**
     * This method initializes nabor
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getNabor() {
        if (nabor == null) {
            nabor = new JComboBox();
            nabor.setModel(new DefaultComboBoxModel(new String[]{"1", "2", "3", "4", "5", "6"}));
            nabor.setSelectedIndex(4);
        }
        return nabor;
    }

    /**
     * This method initializes cardQuantityPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel3() {
        if (cardQuantityPanel == null) {
            cardQuantityLabel = new JLabel();
            cardQuantityLabel.setText("Количество");
            FlowLayout flowLayout2 = new FlowLayout();
            flowLayout2.setAlignment(FlowLayout.RIGHT);
            cardQuantityPanel = new JPanel();
            cardQuantityPanel.setLayout(flowLayout2);
            cardQuantityPanel.add(cardQuantityLabel, null);
            cardQuantityPanel.add(getTypes(), null);
            cardQuantityPanel.add(getCondition(), null);
            cardQuantityPanel.add(getValue(), null);
            cardQuantityPanel.add(getAddQuantityCardButton(), null);
        }
        return cardQuantityPanel;
    }

    /**
     * This method initializes value
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getValue() {
        if (value == null) {
            value = new JComboBox();
            value.setModel(new DefaultComboBoxModel(new String[]{"0", "1", "2", "3", "4", "5", "6"}));
        }
        return value;
    }

    /**
     * This method initializes addQuantityCardButton
     *
     * @return javax.swing.JButton
     */
    private JButton getAddQuantityCardButton() {
        if (addQuantityCardButton == null) {
            addQuantityCardButton = new JButton();
            addQuantityCardButton.setText("Добавить");
            addQuantityCardButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    DefaultListModel model = (DefaultListModel) functionList.getModel();
                    ParseElement el = new ParseElement((ListElement) type.getSelectedItem(), (String) condition.getSelectedItem(), (String) value.getSelectedItem());

                    ((DefaultListModel) functionList.getModel()).add(model.getSize(), el);
                }
            });
        }
        return addQuantityCardButton;
    }

    /**
     * This method initializes condition
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getCondition() {
        if (condition == null) {
            condition = new JComboBox();
            condition.setModel(new DefaultComboBoxModel(new String[]{"<", "=", ">"}));
            condition.setSelectedIndex(1);

        }
        return condition;
    }

    /**
     * This method initializes type
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getTypes() {
        if (type == null) {
            type = new JComboBox();
            type.setModel(new TypeModell(TypeModell.type_cards));
            type.setSelectedIndex(0);
        }
        return type;
    }

    protected Dimension getRightPanelDimension() {
        return new Dimension(435, 190);
    }

    /**
     * This method initializes functionPanel
     *
     * @return javax.swing.JPanel
     */
    protected ConstraintPanel getFunctionPanel() {
        if (functionPanel == null) {
            functionPanel = new ConstraintPanel(this);
            functionPanel.setLayout(new BoxLayout(getFunctionPanel(), BoxLayout.Y_AXIS));
            functionPanel.add(getJPanel3(), null);
            functionPanel.add(getPackIncludePanel(), null);
        }
        return functionPanel;
    }

    /**
     * This method initializes packIncludePanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPackIncludePanel() {
        if (packIncludePanel == null) {
            FlowLayout flowLayout5 = new FlowLayout();
            flowLayout5.setAlignment(FlowLayout.RIGHT);
            packIncludeLabel = new JLabel();
            packIncludeLabel.setText("В наборе имеется ");
            packIncludePanel = new JPanel();
            packIncludePanel.setLayout(flowLayout5);
            packIncludePanel.add(packIncludeLabel, null);
            packIncludePanel.add(getCardValue(), null);
            packIncludePanel.add(getCardType(), null);
            packIncludePanel.add(getAddIncludeConditionButton(), null);
        }
        return packIncludePanel;
    }

    private void soMuch() {
        if (functionList.getModel().getSize() > Integer.parseInt(getNabor().getSelectedItem().toString())) {
            JOptionPane optionPane = new JOptionPane("Вы уверены? КОличество элементов в наборе меньше чем количество добавленных вами элементов",
                    JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION);
            optionPane.createDialog(CardGenerator.this, "Логическая ошибка").setVisible(true);
        }
    }

    /**
     * This method initializes addIncludeConditionButton
     *
     * @return javax.swing.JButton
     */
    private JButton getAddIncludeConditionButton() {
        if (addIncludeConditionButton == null) {
            addIncludeConditionButton = new JButton();
            addIncludeConditionButton.setText("Добавить");
            addIncludeConditionButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    soMuch();
                    DefaultListModel model = (DefaultListModel) functionList.getModel();
                    OneCard el = new OneCard((ListElement) getCardValue().getSelectedItem(), (ListElement) getCardType().getSelectedItem());
                    ((DefaultListModel) functionList.getModel()).add(model.getSize(), el);
                    counter++;
                }


            });
        }
        return addIncludeConditionButton;
    }

    /**
     * This method initializes cardValue
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getCardValue() {
        if (cardValue == null) {
            cardValue = new JComboBox();
            cardValue.setModel(new TypeModell(TypeModell.valueType_cards));
            cardValue.setSelectedIndex(5);
        }
        return cardValue;
    }

    /**
     * This method initializes cardType
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getCardType() {
        if (cardType == null) {
            cardType = new JComboBox();
            cardType.setModel(new TypeModell(TypeModell.valueMast_cards));
            cardType.setSelectedIndex(0);
        }
        return cardType;
    }

    public static void main(String[] args) {
        new CardGenerator().setVisible(true);
    }


    protected Dimension getGeneratorSize() {
        return new Dimension(1050, 500);
    }

    public String getHelpString() {
        //return "Конструктор задач с картами";
        return "Редактор \"Колода карт\"";
    }

    public static CardGenerator getInstance() {
        if (instance == null) {
            return instance = new CardGenerator();
        }
        return instance;
    }
}