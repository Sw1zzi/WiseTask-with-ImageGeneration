/*
 * Decompiled with CFR 0_114.
 */
package ru.spb.ipo.generator.base.startup;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import ru.spb.ipo.generator.base.startup.startup;
import ru.spb.ipo.generator.base.ui.CollectionEditor;
import ru.spb.ipo.generator.basket.BasketGenerator;
import ru.spb.ipo.generator.cards.CardGenerator;
import ru.spb.ipo.generator.chess.ChessGenerator;
import ru.spb.ipo.generator.digits.divs.DivGenerator;
import ru.spb.ipo.generator.digits.mods.ModGenerator;
import ru.spb.ipo.generator.equation.EquationGenerator;
import ru.spb.ipo.generator.numbers.NumberGenerator;
import ru.spb.ipo.generator.word.IndexWordGenerator;
import ru.spb.ipo.generator.word.WordGenerator;

public class MenuFrame
extends JFrame {
    private JPanel jContentPane = null;
    private JPanel mainPanel = null;
    private JButton systemButton = null;
    private JPanel systemPanel = null;
    private JPanel constructorsPanel = null;
    private JPanel constructorsBorderPanel = null;
    private JPanel baseGeneratorPanel;
    private JPanel baseinfo = null;
    private JButton baseGenerator = null;
   private Object[][] constructors = new Object[][]{{"Генератор задач \"Карты\"", "ru.spb.ipo.generator.cards.CardGenerator"},
       
       {"Генератор задач \"Работа со словами\"", "ru.spb.ipo.generator.word.WordGenerator"},
       {"Генератор задач \"Работа с числами\"", "ru.spb.ipo.generator.numbers.NumberGenerator"}, 
       {"Генератор задач \"Работа с уравнениями\"", "ru.spb.ipo.generator.equation.EquationGenerator"}, 
       {"Генератор задач \"Шары и Урны\"", "ru.spb.ipo.generator.basket.BasketGenerator"}, 
       {"Генератор задач \"Делимости\"", "ru.spb.ipo.generator.digits.divs.DivGenerator"}, 
       {"Генератор задач \"Остатки\"", "ru.spb.ipo.generator.digits.mods.ModGenerator"},
       {"Генератор задач \"Шахматы\"", "ru.spb.ipo.generator.chess.ChessGenerator"}};

    public MenuFrame() {
        this.initialize();
    }

    private void initialize() {
        this.setSize(390, 441);
        this.setDefaultCloseOperation(2);
        this.setTitle("Мое меню - Wise Tasks");
        this.setContentPane(this.getJContentPane());
        this.setLocationRelativeTo(null);
    }

    private JPanel getJContentPane() {
        if (this.jContentPane == null) {
            this.jContentPane = new JPanel();
            this.jContentPane.setLayout(new BorderLayout());
            this.jContentPane.setSize(new Dimension(320, 391));
            this.jContentPane.add((Component)this.getMainPanel(), "Center");
        }
        return this.jContentPane;
    }

    private JPanel getMainPanel() {
        if (this.mainPanel == null) {
            this.mainPanel = new JPanel();
            this.mainPanel.setLayout(new BoxLayout(this.getMainPanel(), 1));
            this.mainPanel.add((Component)this.getSystemPanel(), (Object)null);
            this.mainPanel.add((Component)this.getConstructorsBorderPanel(), (Object)null);
            this.mainPanel.add((Component)this.getBaseGeneratorPanel(), (Object)null);
            this.mainPanel.add((Component)this.getBaseInfo(), (Object) null);
        }
        return this.mainPanel;
    }

    private JButton getSystemButton() {
        if (this.systemButton == null) {
            this.systemButton = new JButton();
            this.systemButton.setText("Решение задач");
            this.systemButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    try {
                        startup.getEngineLoader().loadClass("ru.spb.ipo.client.ui.ClientUI").getMethod("main", Class.forName("[Ljava.lang.String;")).invoke(null, new Object[]{null});
                    }
                    catch (Exception var2_2) {
                        JOptionPane.showMessageDialog(MenuFrame.this, "Вылетела ошибка: \n" + var2_2, "Ошибка при выходе из ClientUI", 0);
                        var2_2.printStackTrace();
                    }
                }
            });
        }
        return this.systemButton;
    }

    private JPanel getSystemPanel() {
        if (this.systemPanel == null) {
            this.systemPanel = new JPanel();
            this.systemPanel.setLayout(new BorderLayout());
            this.systemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            this.systemPanel.add((Component)this.getSystemButton(), "Center");
        }
        return this.systemPanel;
    }

    private JPanel getConstructorsPanel() {
        if (this.constructorsPanel == null) {
            this.constructorsPanel = new JPanel();
            this.constructorsPanel.setLayout(new BoxLayout(this.getConstructorsPanel(), 1));
            for (int i = 0; i < this.constructors.length; ++i) {
                Object[] arrobject = this.constructors[i];
                String string = (String)arrobject[0];
                final String string2 = (String)arrobject[1];
                JPanel jPanel = new JPanel();
                jPanel.setLayout(new BorderLayout());
                jPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                JButton jButton = new JButton(string);
                jButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        try {
                            startup.getLoader().loadClass(string2).getMethod("main", Class.forName("[Ljava.lang.String;")).invoke(null, new Object[]{null});
                        }
                        catch (Exception var2_2) {
                            JOptionPane.showMessageDialog(MenuFrame.this, "Вылетела ошибка: \n" + var2_2, "В методе построения всей этой штуки", 0);
                            var2_2.printStackTrace();
                        }
                    }
                });
                jPanel.add((Component)jButton, "Center");
                this.constructorsPanel.add(jPanel);
            }
           
        }
        return this.constructorsPanel;
    }

    private JPanel getConstructorsBorderPanel() {
        if (this.constructorsBorderPanel == null) {
            this.constructorsBorderPanel = new JPanel();
            this.constructorsBorderPanel.setLayout(new BorderLayout());
            this.constructorsBorderPanel.add((Component)this.getConstructorsPanel(), "Center");
        }
        return this.constructorsBorderPanel;
    }

    private JPanel getBaseGeneratorPanel() {
        if (this.baseGeneratorPanel == null) {
            this.baseGeneratorPanel = new JPanel();
            this.baseGeneratorPanel.setLayout(new BorderLayout());
            this.baseGeneratorPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            this.baseGeneratorPanel.add((Component)this.getBaseGenerator(), "Center");
        }
        return this.baseGeneratorPanel;
    }

    private JPanel getBaseInfoPanel()
    {
        if (this.baseinfo == null) {
            this.baseinfo = new JPanel();
           // this.baseinfo.setLayout(new BoxLayout(this.getConstructorsPanel(), 1));
            this.baseinfo.add((Component) this.getBaseInfo(), "Center");
        }
            return this.baseinfo;
    }
    
    private JPanel getBaseInfo()
    {
            JPanel jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JLabel jLabel = new JLabel("Последняя редакция - Май 2016 Кондратюк/Неботов");
            jPanel.add((Component) jLabel, "Center");
            return jPanel;
    }
    
    private JButton getBaseGenerator() {
        if (this.baseGenerator == null) {
            this.baseGenerator = new JButton();
            this.baseGenerator.setText("Основной редактор задач");
            this.baseGenerator.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    try {
                        startup.getLoader().loadClass("ru.spb.ipo.taskgenerator.ui.TaskGenerator").getMethod("main", Class.forName("[Ljava.lang.String;")).invoke(null, new Object[]{null});
                    }
                    catch (Exception var2_2) {
                        JOptionPane.showMessageDialog(MenuFrame.this, "Вылетела ошибка: \n" + var2_2, "Ошбика при работе в ОСНОВНОМ РЕДАКТОРЕ", 0);
                        var2_2.printStackTrace();
                    }
                }
            });
        }
        return this.baseGenerator;
    }

}