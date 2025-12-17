package ru.spb.ipo.generator.chess;

import ru.spb.ipo.generator.base.BaseGenerator;
import ru.spb.ipo.generator.base.ui.BaseGeneratorUI;
import ru.spb.ipo.generator.base.ui.ConstraintPanel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Map;

public class ChessGenerator extends BaseGeneratorUI {
    private JFileChooser imageChooser;
 private JButton removeAllFuntionsButton = null;
    public ChessGenerator() {
        initialize();
    }

    protected Dimension getGeneratorSize() {
        return new Dimension(840, 420);
    }

    protected ConstraintPanel getFunctionPanel() {
        if (functionPanel == null) {
            functionPanel = new ChessFuncPanel(this);
        }
        return functionPanel;
    }

    public JFileChooser getImageChooser() {
        if (imageChooser == null) {
            imageChooser = new JFileChooser(new File("." + File.separator + "tasks" + File.separator + "imgs" + File.separator + "chess"));
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
    

    protected ConstraintPanel getSetPanel() {
        if (setPanel == null) {
            setPanel = new ChessSetPanel(this);
        }
        return setPanel;
    }

    public static void main(String[] args) {
        new ChessGenerator().setVisible(true);
    }

    public BaseGenerator createGenerator(Map source, Map func, Map task) {
        clearIm();
        switch(ChessFuncPanel.ChooseY) {
            case 1: SetImage(new File("tasks" + File.separator + "imgs" + File.separator + "chess" + File.separator + "слон.png")); break;
            case 2: SetImage(new File("tasks" + File.separator + "imgs" + File.separator + "chess" + File.separator + "конь.png")); break;
            case 3: SetImage(new File("tasks" + File.separator + "imgs" + File.separator + "chess" + File.separator + "ладья.png")); break;      
            case 5: SetImage(new File("tasks" + File.separator + "imgs" + File.separator + "chess" + File.separator + "ферзь.png")); break;
            case 6: SetImage(new File("tasks" + File.separator + "imgs" + File.separator + "chess" + File.separator + "пешка.png")); break;  
            case 7: SetImage(new File("tasks" + File.separator + "imgs" + File.separator + "chess" + File.separator + "король.png")); break;
        }
       
        return new ChessXmlGenerator(source, func, task);
    }

    public String getHelpString() {
        return "Редактор \"Задач на шахматы\" Кондратюк/Неботов";

    }


}
