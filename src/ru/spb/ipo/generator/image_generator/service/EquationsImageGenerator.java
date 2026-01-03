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
    private static final int SIGMA_THRESHOLD = 7;

    private Random random = new Random();

    private enum EquationStyle {
        CLASSIC,
        SIGMA
    }

    public BufferedImage generateImage(ProblemContext context) {
        int unknowns = context.getUnknowns();
        EquationStyle style;

        if (unknowns >= SIGMA_THRESHOLD) {
            style = EquationStyle.SIGMA;
        } else {
            if (random.nextBoolean()) {
                style = EquationStyle.CLASSIC;
            } else {
                style = EquationStyle.SIGMA;
            }
        }

        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawRandomBackground(g2d);

        if (style == EquationStyle.CLASSIC) {
            drawClassicEquation(g2d, context);
        } else {
            drawSigmaEquation(g2d, context);
        }

        g2d.dispose();
        return image;
    }

    private void drawRandomBackground(Graphics2D g2d) {
        BackgroundGenerator.Style[] styles = BackgroundGenerator.Style.values();
        BackgroundGenerator.Style randomStyle = styles[random.nextInt(styles.length)];
        BackgroundGenerator.drawBackground(g2d, IMAGE_WIDTH, IMAGE_HEIGHT, randomStyle);
    }

    private void drawClassicEquation(Graphics2D g2d, ProblemContext context) {
        int unknowns = context.getUnknowns();
        int sum = context.getSum();

        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics mainFontMetrics = g2d.getFontMetrics();

        Font indexFont = new Font("Arial", Font.BOLD, 14);
        FontMetrics indexFontMetrics = g2d.getFontMetrics(indexFont);

        int totalWidth = calculateClassicWidth(unknowns, sum, mainFontMetrics, indexFontMetrics);
        int maxAllowedWidth = IMAGE_WIDTH - LEFT_MARGIN - RIGHT_MARGIN;

        int startX;
        boolean isCentered;

        if (totalWidth <= maxAllowedWidth) {
            startX = (IMAGE_WIDTH - totalWidth) / 2;
            isCentered = true;
        } else {
            startX = LEFT_MARGIN;
            isCentered = false;
        }

        int centerY = IMAGE_HEIGHT / 2 + 10;

        int currentX = startX;

        for (int i = 1; i <= unknowns; i++) {
            if (i > 1) {
                g2d.setFont(new Font("Arial", Font.BOLD, 36));
                g2d.setColor(Color.BLACK);
                g2d.drawString("+", currentX, centerY);
                currentX += mainFontMetrics.stringWidth("+") + 10;
            }

            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.setColor(Color.BLACK);
            int xWidth = mainFontMetrics.stringWidth("x");
            g2d.drawString("x", currentX, centerY);

            g2d.setFont(indexFont);
            g2d.setColor(Color.BLACK);
            String indexStr = String.valueOf(i);
            int indexWidth = indexFontMetrics.stringWidth(indexStr);

            int indexX = currentX + xWidth - indexWidth/2 + 3;
            int indexY = centerY + 7;

            g2d.drawString(indexStr, indexX, indexY);

            currentX += xWidth + 12;
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(Color.BLACK);
        g2d.drawString("=", currentX, centerY);
        currentX += mainFontMetrics.stringWidth("=") + 10;

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

    private void drawSigmaEquation(Graphics2D g2d, ProblemContext context) {
        int unknowns = context.getUnknowns();
        int sum = context.getSum();

        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics mainFM = g2d.getFontMetrics();

        Font smallFont = new Font("Arial", Font.BOLD, 14);
        FontMetrics smallFM = g2d.getFontMetrics(smallFont);

        Image sigmaImage = loadSigmaImage();
        boolean hasSigmaImage = sigmaImage != null;

        int sigmaWidth = hasSigmaImage ? 40 : mainFM.stringWidth("Σ");
        int xWidth = mainFM.stringWidth("x");
        int iWidth = smallFM.stringWidth("i");
        int equalsWidth = mainFM.stringWidth("=");
        int sumWidth = mainFM.stringWidth(String.valueOf(sum));
        int bottomTextWidth = smallFM.stringWidth("i = 0");
        int topTextWidth = smallFM.stringWidth("n = " + unknowns);

        int totalWidth = sigmaWidth + 5 + xWidth + iWidth + 10 + equalsWidth + 10 + sumWidth;

        int startX = (IMAGE_WIDTH - totalWidth) / 2;
        int centerY = IMAGE_HEIGHT / 2 + 10;
        int currentX = startX;

        // Символ сигмы (Σ)
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

        // Буква x
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(Color.BLACK);
        g2d.drawString("x", currentX, centerY);

        // Индекс i
        g2d.setFont(smallFont);
        g2d.setColor(Color.BLACK);
        int indexX = currentX + xWidth - iWidth/2 + 2;
        int indexY = centerY + 7;
        g2d.drawString("i", indexX, indexY);

        currentX += xWidth + 10;

        // Знак равенства
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(Color.BLACK);
        g2d.drawString("=", currentX, centerY);
        currentX += equalsWidth + 10;

        // Сумма
        String sumStr = String.valueOf(sum);
        g2d.drawString(sumStr, currentX, centerY);

        // i = 0
        g2d.setFont(smallFont);
        g2d.setColor(Color.BLACK);
        int bottomX = startX + sigmaWidth/2 - bottomTextWidth/2;
        int bottomY = centerY + 20;
        g2d.drawString("i = 0", bottomX, bottomY);

        // n = unknowns
        int topX = startX + sigmaWidth/2 - topTextWidth/2;
        int topY = centerY - 33;
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