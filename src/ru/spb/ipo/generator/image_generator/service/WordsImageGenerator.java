package ru.spb.ipo.generator.image_generator.service;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class WordsImageGenerator {
    private static final int IMAGE_WIDTH = 500;
    private static final int IMAGE_HEIGHT = 110;
    private static final int LETTER_SIZE = 25;
    private static final int LETTER_SPACING = 5;

    private Random random = new Random();

    public BufferedImage generateImage(ProblemContext context) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2d);

        String alphabet = context.getAlphabet();
        int wordLength = context.getWordLength();
        boolean uniqueLetters = context.isUniqueLetters();

        drawAlphabetAndWordInfo(g2d, alphabet, wordLength, uniqueLetters);

        if (wordLength > 0) {
            drawWordVisualization(g2d, wordLength);
        }

        g2d.dispose();
        return image;
    }

    private void drawBackground(Graphics2D g2d) {
        BackgroundGenerator.Style[] styles = {
                BackgroundGenerator.Style.SYMBOLS,
                BackgroundGenerator.Style.GRID
        };
        BackgroundGenerator.Style style = styles[random.nextInt(styles.length)];
        BackgroundGenerator.drawBackground(g2d, IMAGE_WIDTH, IMAGE_HEIGHT, style);
    }

    private void drawAlphabetAndWordInfo(Graphics2D g2d, String alphabet, int wordLength, boolean uniqueLetters) {
        int titleY = 20;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.BLACK);

        String alphabetText;
        if (alphabet == null || alphabet.isEmpty()) {
            alphabetText = "Алфавит: не задан";
            g2d.setColor(Color.RED);
        } else {
            String formattedAlphabet = formatAlphabet(alphabet);
            alphabetText = "Алфавит: " + formattedAlphabet;
            g2d.setColor(Color.BLACK);
        }

        if (alphabet != null && !alphabet.isEmpty() && alphabetText.length() > 50) {
            int maxLength = 47;
            alphabetText = alphabetText.substring(0, maxLength) + "...}";
        }

        int alphabetWidth = g2d.getFontMetrics().stringWidth(alphabetText);
        g2d.drawString(alphabetText, (IMAGE_WIDTH - alphabetWidth) / 2, titleY);

        int infoY = titleY + 25;
        g2d.setColor(Color.BLACK);

        String lengthText = "Длина слова: " + (wordLength > 0 ? wordLength : "не задана");
        String uniqueText = uniqueLetters ? " | Буквы не повторяются" : " | Буквы могут повторяться";
        String infoText = lengthText + uniqueText;

        int infoWidth = g2d.getFontMetrics().stringWidth(infoText);
        g2d.drawString(infoText, (IMAGE_WIDTH - infoWidth) / 2, infoY);
    }

    private String formatAlphabet(String alphabet) {
        if (alphabet == null || alphabet.isEmpty()) {
            return "{}";
        }

        StringBuilder formatted = new StringBuilder("{");
        char[] chars = alphabet.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            formatted.append(chars[i]);
            if (i < chars.length - 1) {
                formatted.append(", ");
            }
        }
        formatted.append("}");

        return formatted.toString();
    }

    private void drawWordVisualization(Graphics2D g2d, int wordLength) {
        if (wordLength <= 0) {
            return;
        }

        int centerY = IMAGE_HEIGHT / 2 + 40;

        int totalWidth = wordLength * (LETTER_SIZE + LETTER_SPACING) - LETTER_SPACING;
        int startX = (IMAGE_WIDTH - totalWidth) / 2;

        for (int i = 0; i < wordLength; i++) {
            int boxX = startX + i * (LETTER_SIZE + LETTER_SPACING);
            drawLetterBox(g2d, boxX, centerY, i + 1);
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.DARK_GRAY);
        String label = "Позиции букв в слове";
        int labelWidth = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label, startX + (totalWidth - labelWidth) / 2, centerY + 30);
    }

    private void drawLetterBox(Graphics2D g2d, int x, int y, int position) {
        g2d.setColor(new Color(60, 60, 180));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y - LETTER_SIZE, LETTER_SIZE, LETTER_SIZE);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(new Color(60, 60, 180));

        String posText = String.valueOf(position);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(posText);
        int textHeight = fm.getAscent();

        int textX = x + (LETTER_SIZE - textWidth) / 2;
        int textY = y - LETTER_SIZE + (LETTER_SIZE + textHeight) / 2 - 2;

        g2d.drawString(posText, textX, textY);
    }
}