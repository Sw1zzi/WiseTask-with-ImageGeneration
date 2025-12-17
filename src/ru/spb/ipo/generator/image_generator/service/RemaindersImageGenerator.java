package ru.spb.ipo.generator.image_generator.service;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class RemaindersImageGenerator {
    private static final int IMAGE_WIDTH = 500;
    private static final int IMAGE_HEIGHT = 110;

    private Random random = new Random();

    private enum RemainderStyle {
        MOD_NOTATION,    // 245 mod 17 = ?
        FORMULA_STYLE,   // 245 = 17 × q + r
        PERCENT_STYLE    // 245 % 17 = ?
    }

    public BufferedImage generateImage(ProblemContext context) {
        // Выбираем случайный стиль
        RemainderStyle style = RemainderStyle.values()[random.nextInt(RemainderStyle.values().length)];

        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBackground(g2d);

        // Ищем dividend
        String dividendStr = extractDividend(context);

        // Ищем divisor
        int divisor = extractDivisor(context);

        // Рисуем выбранный стиль
        switch(style) {
            case MOD_NOTATION:
                drawModNotation(g2d, dividendStr, divisor);
                break;
            case FORMULA_STYLE:
                drawFormulaStyle(g2d, dividendStr, divisor);
                break;
            case PERCENT_STYLE:
                drawPercentStyle(g2d, dividendStr, divisor);
                break;
        }

        g2d.dispose();
        return image;
    }

    private String extractDividend(ProblemContext context) {
        // 1. Пробуем из getDividend()
        String dividendStr = context.getDividend();

        // 2. Если не нашли или это "DIVIDEND", ищем в параметрах
        if (dividendStr == null || dividendStr.isEmpty() || "DIVIDEND".equals(dividendStr)) {
            Object dividendParam = context.getParameter("dividend");
            if (dividendParam != null) {
                dividendStr = dividendParam.toString();
            }
        }

        // 3. Если все еще не нашли, ищем среди всех параметров
        if (dividendStr == null || dividendStr.isEmpty() || "DIVIDEND".equals(dividendStr)) {
            for (String key : context.getAllParameters().keySet()) {
                if (key.toLowerCase().contains("dividend") ||
                        key.toLowerCase().contains("number") ||
                        key.toLowerCase().contains("делимое")) {
                    Object value = context.getParameter(key);
                    if (value != null && !value.toString().isEmpty() && !"DIVIDEND".equals(value.toString())) {
                        dividendStr = value.toString();
                        break;
                    }
                }
            }
        }

        // 4. Если все еще нет, используем "X"
        if (dividendStr == null || dividendStr.isEmpty() || "DIVIDEND".equals(dividendStr)) {
            dividendStr = "X";
        }

        return dividendStr;
    }

    private int extractDivisor(ProblemContext context) {
        // 1. Из getDivisor()
        int divisor = context.getDivisor();

        // 2. Если 0, ищем в параметрах
        if (divisor <= 0) {
            Object divisorParam = context.getParameter("divisor");
            if (divisorParam instanceof Integer) {
                divisor = (Integer) divisorParam;
            } else if (divisorParam instanceof String) {
                try {
                    divisor = Integer.parseInt((String) divisorParam);
                } catch (NumberFormatException e) {
                    divisor = 1;
                }
            } else {
                divisor = 1;
            }
        }

        // Защита от деления на 0
        if (divisor <= 0) divisor = 1;

        return divisor;
    }

    private void drawBackground(Graphics2D g2d) {
        // Используем стиль с математическими символами
        BackgroundGenerator.drawBackground(g2d, IMAGE_WIDTH, IMAGE_HEIGHT,
                BackgroundGenerator.Style.SYMBOLS);
    }

    // Стиль 1: 245 mod 17 = ?
    private void drawModNotation(Graphics2D g2d, String dividend, int divisor) {
        Font mainFont = new Font("Arial", Font.BOLD, 36);
        g2d.setFont(mainFont);
        FontMetrics mainFM = g2d.getFontMetrics();

        String text = dividend + " mod " + divisor + " = ?";
        int textWidth = mainFM.stringWidth(text);
        int x = (IMAGE_WIDTH - textWidth) / 2;
        int y = IMAGE_HEIGHT / 2 + 10;

        // Рисуем текст
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, x, y);
    }

    // Стиль 2: 245 = 17 × q + r
    private void drawFormulaStyle(Graphics2D g2d, String dividend, int divisor) {
        Font mainFont = new Font("Arial", Font.BOLD, 36);
        g2d.setFont(mainFont);
        FontMetrics mainFM = g2d.getFontMetrics();

        String text = dividend + " = " + divisor + " × q + r";
        int textWidth = mainFM.stringWidth(text);
        int x = (IMAGE_WIDTH - textWidth) / 2;
        int y = IMAGE_HEIGHT / 2 + 10;

        g2d.setColor(Color.BLACK);
        g2d.drawString(text, x, y);

        // Объяснение внизу - СДЕЛАНО БОЛЬШЕ И ЖИРНЫМ
        g2d.setFont(new Font("Arial", Font.BOLD, 14)); // Было 12, стало 14 и Font.BOLD
        g2d.setColor(new Color(80, 80, 80)); // Темнее для лучшей читаемости
        String explanation = "где q - частное, r - остаток";
        int expWidth = g2d.getFontMetrics().stringWidth(explanation);
        int expX = (IMAGE_WIDTH - expWidth) / 2;
        int expY = y + 25;
        g2d.drawString(explanation, expX, expY);
    }

    // Стиль 3: 245 % 17 = ?
    private void drawPercentStyle(Graphics2D g2d, String dividend, int divisor) {
        Font mainFont = new Font("Consolas", Font.BOLD, 36);
        g2d.setFont(mainFont);
        FontMetrics mainFM = g2d.getFontMetrics();

        String text = dividend + " % " + divisor + " = ?";
        int textWidth = mainFM.stringWidth(text);
        int x = (IMAGE_WIDTH - textWidth) / 2;
        int y = IMAGE_HEIGHT / 2 + 10;

        g2d.setColor(Color.BLACK);
        g2d.drawString(text, x, y);
    }
}