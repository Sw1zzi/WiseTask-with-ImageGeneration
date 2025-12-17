package ru.spb.ipo.generator.image_generator.service;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class DivisibilityImageGenerator {
    private static final int IMAGE_WIDTH = 500;
    private static final int IMAGE_HEIGHT = 110;
    private static final int DIGIT_WIDTH = 25; // Ширина одной цифры
    private static final int ARROW_WIDTH = 40; // Ширина стрелки

    private Random random = new Random();

    public BufferedImage generateImage(ProblemContext context) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Случайный фон
        drawBackground(g2d);

        // Получаем количество цифр
        int digits = context.getDigits();
        digits = Math.max(2, Math.min(8, digits)); // Ограничиваем от 2 до 8 цифр

        // Рисуем заголовок черным цветом
        drawTitle(g2d, digits);

        // Рисуем преобразование числа (левая часть + стрелка + правая часть)
        drawNumberTransformation(g2d, digits);

        g2d.dispose();
        return image;
    }

    private void drawBackground(Graphics2D g2d) {
        // Используем сетку или математические символы
        BackgroundGenerator.Style[] styles = {
                BackgroundGenerator.Style.GRID,
                BackgroundGenerator.Style.SYMBOLS,
                BackgroundGenerator.Style.NUMBERS
        };
        BackgroundGenerator.Style style = styles[random.nextInt(styles.length)];
        BackgroundGenerator.drawBackground(g2d, IMAGE_WIDTH, IMAGE_HEIGHT, style);
    }

    private void drawTitle(Graphics2D g2d, int digits) {
        // Заголовок сверху - ЧЕРНЫЙ цвет
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String title = digits + "-значное число";

        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);

        // Черный цвет для заголовка
        g2d.setColor(Color.BLACK);
        g2d.drawString(title, (IMAGE_WIDTH - titleWidth) / 2, 25);
    }

    private void drawNumberTransformation(Graphics2D g2d, int digits) {
        // Рассчитываем общую ширину
        int leftPanelWidth = digits * DIGIT_WIDTH;
        int rightPanelWidth = digits * DIGIT_WIDTH;
        int totalWidth = leftPanelWidth + ARROW_WIDTH + rightPanelWidth;

        int startX = (IMAGE_WIDTH - totalWidth) / 2;
        int centerY = IMAGE_HEIGHT / 2 + 10;

        // Левая панель: исходное число
        int leftStartX = startX;
        drawOriginalNumber(g2d, leftStartX, centerY, digits);

        // Стрелка
        int arrowX = leftStartX + leftPanelWidth + 5;
        drawArrow(g2d, arrowX, centerY);

        // Правая панель: новая комбинация (по умолчанию равна исходной)
        int rightStartX = arrowX + ARROW_WIDTH - 10;
        drawNewNumber(g2d, rightStartX, centerY, digits);
    }

    private void drawOriginalNumber(Graphics2D g2d, int startX, int centerY, int digits) {
        // Подпись "Исходное"
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(Color.DARK_GRAY);
        String label = "Исходное";
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);
        g2d.drawString(label, startX + (digits * DIGIT_WIDTH - labelWidth) / 2, centerY - 20);

        // Рисуем цифры в формате [1][2][3]...
        for (int i = 0; i < digits; i++) {
            int digitX = startX + i * DIGIT_WIDTH;
            drawOriginalDigit(g2d, digitX, centerY, i + 1);
        }
    }

    private void drawOriginalDigit(Graphics2D g2d, int x, int y, int position) {
        // Рисуем скобку с номером позиции [1], [2], ...
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(new Color(60, 60, 180)); // Синий цвет для скобок

        String bracket = "[" + position + "]";
        g2d.drawString(bracket, x, y);

        // Под цифрой диапазон (0-9)
        g2d.setFont(new Font("Arial", Font.PLAIN, 9));
        g2d.setColor(Color.DARK_GRAY);

        String range = "0-9";
        FontMetrics fm = g2d.getFontMetrics();
        int rangeWidth = fm.stringWidth(range);
        int rangeX = x + (DIGIT_WIDTH - rangeWidth) / 2;

        g2d.drawString(range, rangeX, y + 15);
    }

    private void drawArrow(Graphics2D g2d, int x, int y) {
        // Рисуем стрелку как в других генераторах
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));

        // Линия стрелки
        g2d.drawLine(x, y - 5, x + 25, y - 5);

        // Наконечник стрелки
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(x + 25, y - 5);
        arrowHead.addPoint(x + 18, y - 9);
        arrowHead.addPoint(x + 18, y - 1);
        g2d.fill(arrowHead);

        // Подпись "→"
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.RED);
        g2d.drawString("→", x + 8, y + 10);
    }

    private void drawNewDigit(Graphics2D g2d, int x, int y, int position, int totalDigits) {
        // По умолчанию рисуем такую же позицию как в исходном числе
        // Можно добавить вариативность: иногда показывать ту же позицию, иногда другую

        boolean keepSamePosition = random.nextBoolean();
        int displayPosition = keepSamePosition ? position : (random.nextInt(totalDigits) + 1);

        // Цвет для новых позиций - красный или зеленый
        Color bracketColor = keepSamePosition ?
                new Color(0, 150, 0) : // Зеленый для тех же позиций
                new Color(180, 60, 60); // Красный для измененных

        // Рисуем скобку с номером позиции
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(bracketColor);

        String bracket = "[" + displayPosition + "]";
        g2d.drawString(bracket, x, y);

        // Под цифрой: либо цифра, либо ?
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        boolean showDigit = random.nextBoolean();
        if (showDigit) {
            // Показываем случайную цифру
            int randomDigit = random.nextInt(10);
            g2d.setColor(Color.BLACK);
            String digit = String.valueOf(randomDigit);

            FontMetrics fm = g2d.getFontMetrics();
            int digitWidth = fm.stringWidth(digit);
            int digitX = x + (DIGIT_WIDTH - digitWidth) / 2;

            g2d.drawString(digit, digitX, y + 15);
        } else {
            // Показываем знак вопроса
            g2d.setColor(Color.GRAY);
            String question = "?";

            FontMetrics fm = g2d.getFontMetrics();
            int qWidth = fm.stringWidth(question);
            int qX = x + (DIGIT_WIDTH - qWidth) / 2;

            g2d.drawString(question, qX, y + 15);
        }
    }

    private void drawNewNumber(Graphics2D g2d, int startX, int centerY, int digits) {
        // Подпись "Новое"
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(Color.DARK_GRAY);
        String label = "Новое";
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);
        g2d.drawString(label, startX + (digits * DIGIT_WIDTH - labelWidth) / 2, centerY - 20);

        // Рисуем новую комбинацию (по умолчанию такая же как исходная)
        for (int i = 0; i < digits; i++) {
            int digitX = startX + i * DIGIT_WIDTH;
            drawNewDigit(g2d, digitX, centerY, i + 1, digits); // Добавляем digits как параметр
        }
    }
}