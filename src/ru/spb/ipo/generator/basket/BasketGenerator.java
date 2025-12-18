package ru.spb.ipo.generator.basket;

import ru.spb.ipo.generator.base.BaseGenerator;
import ru.spb.ipo.generator.base.ui.BaseGeneratorUI;
import ru.spb.ipo.generator.base.ui.ConstraintPanel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Map;

public class BasketGenerator extends BaseGeneratorUI {

    public BasketGenerator() {
        initialize();
    }

    protected Dimension getGeneratorSize() {
        return new Dimension(800, 450);
    }

    protected boolean checkCanSave() {
        if (getFunctionList().getModel().getSize() == 0) {
            JOptionPane.showMessageDialog(this, "Не указаны шары, вытаскиваемые из урны!", "Не указаны шары, вытаскиваемые из урны", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return super.checkCanSave();
    }

    private JFileChooser imageChooser;

    public JFileChooser getImageChooser() {
        if (imageChooser == null) {
            imageChooser = new JFileChooser(new File("." + File.separator + "tasks" + File.separator + "imgs" + File.separator + "maras"));
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
            setPanel = new BasketSetPanel();
        }
        return setPanel;
    }

    protected ConstraintPanel getFunctionPanel() {
        if (functionPanel == null) {
            functionPanel = new BasketConditions(this);
        }
        return functionPanel;
    }

    public static void main(String[] args) {
        new BasketGenerator().setVisible(true);
    }

    public BaseGenerator createGenerator(Map source, Map func, Map task) {
        return new BasketXmlGenerator(source, func, task);
    }

    protected void clear() {
        ((BasketSetPanel) getSetPanel()).clear();
        super.clear();
    }

    public String getHelpString() {
        return "Редактор \"Шары и урны\"";
    }

    protected Dimension getRightPanelDimension() {
        return new Dimension(305, 190);
    }

    /**
     * Метод для отображения изображения для задачи с шарами
     * Аналогично методу в BaseGeneratorUI, но с другим именем файла
     */
//    @Override
    protected void displayGeneratedImage(java.awt.image.BufferedImage image) {
        try {
            // Очищаем панель с изображениями
            getImageListPanel().removeAll();
            getImagesList().clear();

            // Конвертируем BufferedImage в ImageIcon
            ImageIcon icon = new ImageIcon(image);

            // Создаем JLabel с изображением
            JLabel imageLabel = new JLabel(icon);

            // Добавляем на панель
            getImageListPanel().add(imageLabel);

            // Сохраняем имя в список - другое имя файла для шаров
            getImagesList().add("generated_balls_image.png");

            // Обновляем интерфейс
            getImageListPanel().revalidate();
            getImageListPanel().repaint();

            System.out.println("Изображение для задачи 'Шары и урны' отображено в интерфейсе");

        } catch (Exception e) {
            System.out.println("Ошибка отображения изображения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}