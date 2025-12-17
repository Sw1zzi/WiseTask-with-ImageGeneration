package ru.spb.ipo.generator.image_generator.service;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.imageio.ImageIO;

public class EquationsImageGenerator {
    private static final int IMAGE_WIDTH = 500;
    private static final int IMAGE_HEIGHT = 110;
    private static final int LEFT_MARGIN = 20;
    private static final int RIGHT_MARGIN = 20;

    private Random random = new Random();

    // Стили уравнений
    private enum EquationStyle {
        CLASSIC,  // x₁ + x₂ + ... + xₙ = sum
        SIGMA     // Σ xᵢ = sum (i=0..n)
    }

    public BufferedImage generateImage(ProblemContext context) {
        // Случайно выбираем стиль (50/50)
        EquationStyle style = random.nextBoolean() ? EquationStyle.CLASSIC : EquationStyle.SIGMA;

        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Случайный фон для каждой генерации
        drawBackground(g2d);

        // Рисуем выбранный стиль
        if (style == EquationStyle.CLASSIC) {
            drawClassicEquation(g2d, context);
        } else {
            drawSigmaEquation(g2d, context);
        }

        g2d.dispose();
        return image;
    }

    private void drawBackground(Graphics2D g2d) {
        // Используем математические символы
        BackgroundGenerator.drawBackground(g2d, IMAGE_WIDTH, IMAGE_HEIGHT,
                BackgroundGenerator.Style.SYMBOLS);
    }

    // ========== ВАРИАНТ 1: КЛАССИЧЕСКОЕ УРАВНЕНИЕ ==========
    private void drawClassicEquation(Graphics2D g2d, ProblemContext context) {
        int unknowns = context.getUnknowns();
        int sum = context.getSum();

        // Основной шрифт
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics mainFontMetrics = g2d.getFontMetrics();

        // Шрифт для индексов - ТЕПЕРЬ ЖИРНЫЙ (Font.BOLD)
        Font indexFont = new Font("Arial", Font.BOLD, 14); // Font.PLAIN → Font.BOLD
        FontMetrics indexFontMetrics = g2d.getFontMetrics(indexFont);

        // Рассчитываем ширину всего уравнения
        int totalWidth = calculateClassicWidth(unknowns, sum, mainFontMetrics, indexFontMetrics);
        int maxAllowedWidth = IMAGE_WIDTH - LEFT_MARGIN - RIGHT_MARGIN;

        int startX;
        boolean isCentered;

        // Решаем: центрировать или начинать слева
        if (totalWidth <= maxAllowedWidth) {
            startX = (IMAGE_WIDTH - totalWidth) / 2;
            isCentered = true;
        } else {
            startX = LEFT_MARGIN;
            isCentered = false;
        }

        int centerY = IMAGE_HEIGHT / 2 + 10;

        int currentX = startX;

        // Рисуем уравнение: x₁ + x₂ + ... + xₙ = sum
        for (int i = 1; i <= unknowns; i++) {
            if (i > 1) {
                g2d.setFont(new Font("Arial", Font.BOLD, 36));
                g2d.setColor(Color.BLACK);
                g2d.drawString("+", currentX, centerY);
                currentX += mainFontMetrics.stringWidth("+") + 10;
            }

            // Буква x
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.setColor(Color.BLACK);
            int xWidth = mainFontMetrics.stringWidth("x");
            g2d.drawString("x", currentX, centerY);

            // Индекс внизу - ТЕПЕРЬ ЖИРНЫЙ
            g2d.setFont(indexFont);
            g2d.setColor(Color.BLACK);
            String indexStr = String.valueOf(i);
            int indexWidth = indexFontMetrics.stringWidth(indexStr);

            int indexX = currentX + xWidth - indexWidth/2 + 3;
            int indexY = centerY + 7;

            g2d.drawString(indexStr, indexX, indexY);

            currentX += xWidth + 12;
        }

        // Знак равенства
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(Color.BLACK);
        g2d.drawString("=", currentX, centerY);
        currentX += mainFontMetrics.stringWidth("=") + 10;

        // Сумма
        String sumStr = String.valueOf(sum);
        g2d.drawString(sumStr, currentX, centerY);
    }

    private int calculateClassicWidth(int unknowns, int sum,
                                      FontMetrics mainFM, FontMetrics indexFM) {
        int totalWidth = 0;

        for (int i = 1; i <= unknowns; i++) {
            int xWidth = mainFM.stringWidth("x");
            totalWidth += xWidth + 8;

            if (i > 1) {
                totalWidth += mainFM.stringWidth("+") + 8;
            }
        }

        totalWidth += mainFM.stringWidth("=") + 10;
        totalWidth += mainFM.stringWidth(String.valueOf(sum));

        return totalWidth;
    }

    // ========== ВАРИАНТ 2: СИГМА-НОТАЦИЯ ==========
    private void drawSigmaEquation(Graphics2D g2d, ProblemContext context) {
        int unknowns = context.getUnknowns();
        int sum = context.getSum();

        // Основной шрифт
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics mainFM = g2d.getFontMetrics();

        // Маленький шрифт - ТЕПЕРЬ ЖИРНЫЙ (Font.BOLD)
        Font smallFont = new Font("Arial", Font.BOLD, 14); // Font.PLAIN → Font.BOLD
        FontMetrics smallFM = g2d.getFontMetrics(smallFont);

        // Пробуем загрузить символ суммы
        Image sigmaImage = loadSigmaImage();
        boolean hasSigmaImage = sigmaImage != null;

        // Рассчитываем ширину
        int sigmaWidth = hasSigmaImage ? 40 : mainFM.stringWidth("Σ");
        int xWidth = mainFM.stringWidth("x");
        int iWidth = smallFM.stringWidth("i");
        int equalsWidth = mainFM.stringWidth("=");
        int sumWidth = mainFM.stringWidth(String.valueOf(sum));
        int bottomTextWidth = smallFM.stringWidth("i = 0");
        int topTextWidth = smallFM.stringWidth("n = " + unknowns);

        int totalWidth = sigmaWidth + 5 + xWidth + iWidth + 10 + equalsWidth + 10 + sumWidth;

        // Центрируем сигма-нотацию
        int startX = (IMAGE_WIDTH - totalWidth) / 2;
        int centerY = IMAGE_HEIGHT / 2 + 10;
        int currentX = startX;

        // 1. Символ сигмы (Σ)
        if (hasSigmaImage) {
            g2d.drawImage(sigmaImage, currentX, centerY - 30, 40, 40, null);
            currentX += 40 + 5;
        } else {
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Σ", currentX, centerY + 5);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            currentX += mainFM.stringWidth("Σ") + 5;
        }

        // 2. Буква x
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(Color.BLACK);
        g2d.drawString("x", currentX, centerY);

        // 3. Индекс i (внизу справа от x) - ТЕПЕРЬ ЖИРНЫЙ
        g2d.setFont(smallFont);
        g2d.setColor(Color.BLACK);
        int indexX = currentX + xWidth - iWidth/2 + 2;
        int indexY = centerY + 7;
        g2d.drawString("i", indexX, indexY);

        currentX += xWidth + 10;

        // 4. Знак равенства
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(Color.BLACK);
        g2d.drawString("=", currentX, centerY);
        currentX += equalsWidth + 10;

        // 5. Сумма
        String sumStr = String.valueOf(sum);
        g2d.drawString(sumStr, currentX, centerY);

        // 6. НИЖНИЙ ТЕКСТ: i = 0 - ТЕПЕРЬ ЖИРНЫЙ и ВЫШЕ (centerY + 23 → centerY + 20)
        g2d.setFont(smallFont);
        g2d.setColor(Color.BLACK);
        int bottomX = startX + sigmaWidth/2 - bottomTextWidth/2;
        int bottomY = centerY + 25; // Было 28, стало 20 - ПОДНЯЛИ ВЫШЕ
        g2d.drawString("i = 0", bottomX, bottomY);

        // 7. ВЕРХНИЙ ТЕКСТ: n = unknowns - ТЕПЕРЬ ЖИРНЫЙ и ВЫШЕ (centerY - 20 → centerY - 25)
        int topX = startX + sigmaWidth/2 - topTextWidth/2;
        int topY = centerY - 33; // Было -20, стало -25 - ПОДНЯЛИ ЕЩЕ ВЫШЕ
        g2d.drawString("n = " + unknowns, topX, topY);
    }

    private Image loadSigmaImage() {
        try {
            return ImageIO.read(getClass().getResourceAsStream("/imgs/sum.png"));
        } catch (Exception e) {
            return null;
        }
    }
}