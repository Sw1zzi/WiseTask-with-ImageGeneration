package ru.spb.ipo.generator.image_generator.service;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class NumbersImageGenerator {
    private static final int IMAGE_WIDTH = 500;
    private static final int IMAGE_HEIGHT = 110;
    private static final int ELEMENT_SIZE = 35;
    private static final int OPERATOR_WIDTH = 30;

    private Random random = new Random();

    public BufferedImage generateImage(ProblemContext context) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Случайный фон
        drawBackground(g2d);

        // Получаем параметры
        int digits = context.getDigits();
        int maxDigit = context.getMaxDigit();
        boolean firstNotZero = context.isFirstNotZero();
        boolean hasComparison = context.hasComparison();

        // Рисуем заголовок
        drawTitle(g2d, digits, maxDigit, firstNotZero, context);

        // Рисуем операцию сравнения (если есть)
        if (hasComparison) {
            drawComparison(g2d, context.getCompareLeft(), context.getCompareRight(),
                    context.getCompareOperator());
        } else {
            // Или просто показываем набор цифр
            drawSimpleSet(g2d, digits);
        }

        g2d.dispose();
        return image;
    }

    private void drawBackground(Graphics2D g2d) {
        BackgroundGenerator.Style[] styles = {
                BackgroundGenerator.Style.NUMBERS,
                BackgroundGenerator.Style.GRID
        };
        BackgroundGenerator.Style style = styles[random.nextInt(styles.length)];
        BackgroundGenerator.drawBackground(g2d, IMAGE_WIDTH, IMAGE_HEIGHT, style);
    }

    private void drawTitle(Graphics2D g2d, int digits, int maxDigit,
                           boolean firstNotZero, ProblemContext context) {

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.BLACK);

        String baseText = "Набор из ";
        FontMetrics fm = g2d.getFontMetrics();
        int baseTextWidth = fm.stringWidth(baseText);

        int currentX = (IMAGE_WIDTH - getTitleWidth(g2d, digits, maxDigit, firstNotZero)) / 2;
        int titleY = 20;

        // "Набор из "
        g2d.drawString(baseText, currentX, titleY);
        currentX += baseTextWidth;

        // Количество цифр (красный)
        g2d.setColor(Color.RED);
        String digitsText = digits + "";
        g2d.drawString(digitsText, currentX, titleY);
        currentX += fm.stringWidth(digitsText);

        // " цифр ("
        g2d.setColor(Color.BLACK);
        String middleText = " цифр (";
        g2d.drawString(middleText, currentX, titleY);
        currentX += fm.stringWidth(middleText);

        // Диапазон цифр (красный)
        g2d.setColor(Color.RED);
        String rangeText = firstNotZero ? "1-" + maxDigit : "0-" + maxDigit;
        g2d.drawString(rangeText, currentX, titleY);
        currentX += fm.stringWidth(rangeText);

        // ")"
        g2d.setColor(Color.BLACK);
        g2d.drawString(")", currentX, titleY);
        currentX += fm.stringWidth(")");

        // Условие первой цифры
        g2d.setColor(Color.BLACK);
        String firstDigitText = firstNotZero ? " | первая ≠ 0" : " | первая может быть 0";
        g2d.drawString(firstDigitText, currentX, titleY);
    }

    private int getTitleWidth(Graphics2D g2d, int digits, int maxDigit, boolean firstNotZero) {
        FontMetrics fm = g2d.getFontMetrics();

        int width = 0;
        width += fm.stringWidth("Набор из ");
        width += fm.stringWidth(digits + "");
        width += fm.stringWidth(" цифр (");
        width += fm.stringWidth(firstNotZero ? "1-" + maxDigit : "0-" + maxDigit);
        width += fm.stringWidth(")");
        width += fm.stringWidth(firstNotZero ? " | первая ≠ 0" : " | первая может быть 0");

        return width;
    }

    private void drawComparison(Graphics2D g2d, List<String> leftPositions,
                                List<String> rightPositions, String operator) {

        int centerY = IMAGE_HEIGHT / 2 + 15;

        // Левая часть
        int leftWidth = leftPositions.size() * ELEMENT_SIZE;
        int leftStartX = (IMAGE_WIDTH - (leftWidth + OPERATOR_WIDTH + rightPositions.size() * ELEMENT_SIZE)) / 2;

        // Рисуем левые элементы (ЧЕРНЫЕ)
        for (int i = 0; i < leftPositions.size(); i++) {
            int posX = leftStartX + i * ELEMENT_SIZE;
            drawPositionElement(g2d, posX, centerY, leftPositions.get(i));
        }

        // Оператор сравнения
        int operatorX = leftStartX + leftWidth + 5;
        drawComparisonOperator(g2d, operatorX, centerY, operator);

        // Правая часть
        int rightStartX = operatorX + 25;

        // Рисуем правые элементы (ЧЕРНЫЕ)
        for (int i = 0; i < rightPositions.size(); i++) {
            int posX = rightStartX + i * ELEMENT_SIZE;
            drawPositionElement(g2d, posX, centerY, rightPositions.get(i));
        }
    }

    private void drawPositionElement(Graphics2D g2d, int x, int y, String position) {
        // Только позиция черным цветом
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(Color.BLACK); // <--- ИЗМЕНЕНО: черный вместо цветного
        g2d.drawString(position, x, y);

        // Убраны подписи "0-9"
    }

    private void drawComparisonOperator(Graphics2D g2d, int x, int y, String operator) {
        // Оператор сравнения красным
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(Color.RED);

        FontMetrics fm = g2d.getFontMetrics();
        int opWidth = fm.stringWidth(operator);
        g2d.drawString(operator, x, y);
    }

    private void drawSimpleSet(Graphics2D g2d, int digits) {
        int centerY = IMAGE_HEIGHT / 2 + 15;
        int totalWidth = digits * ELEMENT_SIZE;
        int startX = (IMAGE_WIDTH - totalWidth) / 2;

        for (int i = 0; i < digits; i++) {
            int posX = startX + i * ELEMENT_SIZE;
            // Черные позиции
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.setColor(Color.BLACK);
            g2d.drawString("[" + (i+1) + "]", posX, centerY);
        }
    }
}