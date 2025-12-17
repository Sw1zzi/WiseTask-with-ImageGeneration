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

        // Случайный фон
        drawBackground(g2d);

        // Получаем параметры
        String alphabet = context.getAlphabet();
        int wordLength = context.getWordLength();
        boolean uniqueLetters = context.isUniqueLetters();

        // Если алфавит не задан, используем русский по умолчанию
        if (alphabet == null || alphabet.isEmpty()) {
            alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
        }

        // Рисуем только алфавит и информацию о слове (без условий)
        drawAlphabetAndWordInfo(g2d, alphabet, wordLength, uniqueLetters);

        // Рисуем визуализацию слова
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

        // Форматируем алфавит
        String formattedAlphabet = formatAlphabet(alphabet);
        String alphabetText = "Алфавит: " + formattedAlphabet;

        // Укорачиваем если слишком длинный
        if (alphabetText.length() > 50) {
            int maxLength = 47;
            alphabetText = alphabetText.substring(0, maxLength) + "...}";
        }

        // Рисуем алфавит
        int alphabetWidth = g2d.getFontMetrics().stringWidth(alphabetText);
        g2d.drawString(alphabetText, (IMAGE_WIDTH - alphabetWidth) / 2, titleY);

        // Вторая строка: информация о слове
        int infoY = titleY + 25;

        String lengthText = "Длина слова: " + wordLength;
        String uniqueText = uniqueLetters ? " | буквы не повторяются" : " | буквы могут повторяться";
        String infoText = lengthText + uniqueText;

        int infoWidth = g2d.getFontMetrics().stringWidth(infoText);
        g2d.drawString(infoText, (IMAGE_WIDTH - infoWidth) / 2, infoY);

        // УСЛОВИЯ СЛОВА НЕ ВЫВОДЯТСЯ - этот блок полностью удален
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

        // Рассчитываем позиции для букв слова
        int totalWidth = wordLength * (LETTER_SIZE + LETTER_SPACING) - LETTER_SPACING;
        int startX = (IMAGE_WIDTH - totalWidth) / 2;

        // Рисуем квадратики для каждой позиции в слове
        for (int i = 0; i < wordLength; i++) {
            int boxX = startX + i * (LETTER_SIZE + LETTER_SPACING);
            drawLetterBox(g2d, boxX, centerY, i + 1);
        }

        // Подпись под квадратиками
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.DARK_GRAY);
        String label = "Позиции букв в слове";
        int labelWidth = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label, startX + (totalWidth - labelWidth) / 2, centerY + 30);
    }

    private void drawLetterBox(Graphics2D g2d, int x, int y, int position) {
        // Рисуем квадрат для буквы
        g2d.setColor(new Color(60, 60, 180));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y - LETTER_SIZE, LETTER_SIZE, LETTER_SIZE);

        // Номер позиции внутри квадрата
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(new Color(60, 60, 180));

        String posText = String.valueOf(position);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(posText);
        int textHeight = fm.getAscent();

        // Центрируем номер в квадрате
        int textX = x + (LETTER_SIZE - textWidth) / 2;
        int textY = y - LETTER_SIZE + (LETTER_SIZE + textHeight) / 2 - 2;

        g2d.drawString(posText, textX, textY);

        // Маленький номер позиции сверху
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        String smallPos = "[" + position + "]";
        int smallWidth = g2d.getFontMetrics().stringWidth(smallPos);
        g2d.drawString(smallPos, x + (LETTER_SIZE - smallWidth) / 2, y - LETTER_SIZE - 5);
    }
}