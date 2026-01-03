package ru.spb.ipo.generator.image_generator.service;

import java.awt.*;
import java.util.Random;

public class BackgroundGenerator {
    private static final Random random = new Random();

    private static final Color[] COMMON_BACKGROUNDS = {
            new Color(240, 245, 255),  // Светло-голубой
            new Color(245, 255, 240),  // Светло-зеленый
            new Color(255, 245, 240),  // Светло-бежевый
            new Color(255, 240, 245),  // Светло-розовый
            new Color(245, 240, 255),  // Светло-фиолетовый
            new Color(240, 255, 245),  // Светло-мятный
            new Color(250, 245, 235),  // Бежевый
            new Color(255, 248, 220),  // Светло-желтый
            new Color(220, 255, 220),  // Светло-зеленый 2
            new Color(235, 245, 255)   // Очень светлый голубой
    };

    public enum Style {
        SIMPLE,        // Простой однотонный фон
        GRID,          // Сетка
        NUMBERS,       // Цифры в сетке
        LETTERS,       // Буквы в сетке
        SYMBOLS,       // Символы в сетке
        CARDS,         // Карточные масти в сетке
        CHESS          // Шахматный узор
    }

    /**
     * Создает стандартный фон для изображения
     */
    public static void drawBackground(Graphics2D g2d, int width, int height, Style style) {
        Color bgColor = COMMON_BACKGROUNDS[random.nextInt(COMMON_BACKGROUNDS.length)];

        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, width, height);

        drawPattern(g2d, width, height, bgColor, style);

        drawBorder(g2d, width, height, bgColor);
    }

    /**
     * Создает фон с случайным стилем
     */
    public static void drawRandomBackground(Graphics2D g2d, int width, int height) {
        Style[] styles = Style.values();
        Style randomStyle = styles[random.nextInt(styles.length)];
        drawBackground(g2d, width, height, randomStyle);
    }

    /**
     * Рисует узор на фоне - СЕТКА СИМВОЛОВ
     */
    private static void drawPattern(Graphics2D g2d, int width, int height, Color bgColor, Style style) {
        g2d.setColor(new Color(0, 0, 0, 12));

        switch(style) {
            case GRID:
                drawGrid(g2d, width, height);
                break;
            case NUMBERS:
                drawNumberGrid(g2d, width, height);
                break;
            case LETTERS:
                drawLetterGrid(g2d, width, height);
                break;
            case SYMBOLS:
                drawSymbolGrid(g2d, width, height);
                break;
            case CARDS:
                drawCardGrid(g2d, width, height);
                break;
            case CHESS:
                drawChessGrid(g2d, width, height);
                break;
            case SIMPLE:
                break;
        }
    }

    private static void drawGrid(Graphics2D g2d, int width, int height) {
        g2d.setStroke(new BasicStroke(0.7f));

        for (int i = 25; i < width; i += 25) {
            g2d.drawLine(i, 0, i, height);
        }

        for (int i = 25; i < height; i += 25) {
            g2d.drawLine(0, i, width, i);
        }
    }

    private static void drawNumberGrid(Graphics2D g2d, int width, int height) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        int cellSize = 25;
        int cols = width / cellSize;
        int rows = height / cellSize;

        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                int number = (col + row) % 10;
                int x = col * cellSize + cellSize/2 - 3;
                int y = row * cellSize + cellSize/2 + 3;
                g2d.drawString(String.valueOf(number), x, y);
            }
        }
    }

    private static void drawLetterGrid(Graphics2D g2d, int width, int height) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        String letters = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";

        int cellSize = 25;
        int cols = width / cellSize;
        int rows = height / cellSize;

        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                int index = (col * rows + row) % letters.length();
                char letter = letters.charAt(index);
                int x = col * cellSize + cellSize/2 - 3;
                int y = row * cellSize + cellSize/2 + 3;
                g2d.drawString(String.valueOf(letter), x, y);
            }
        }
    }

    private static void drawSymbolGrid(Graphics2D g2d, int width, int height) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        String[] symbols = {"+", "−", "×", "÷", "=", "√", "∑", "π", "∞"};

        int cellSize = 25;
        int cols = width / cellSize;
        int rows = height / cellSize;

        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                int index = (col + row * 2) % symbols.length;
                String symbol = symbols[index];
                int x = col * cellSize + cellSize/2 - 3;
                int y = row * cellSize + cellSize/2 + 3;
                g2d.drawString(symbol, x, y);
            }
        }
    }

    private static void drawCardGrid(Graphics2D g2d, int width, int height) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        String[] suits = {"♥", "♦", "♠", "♣"};

        int cellSize = 25;
        int cols = width / cellSize;
        int rows = height / cellSize;

        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                int suitIndex = (col + row) % suits.length;
                String suit = suits[suitIndex];

                if (suit.equals("♥") || suit.equals("♦")) {
                    g2d.setColor(new Color(200, 0, 0, 20)); // Красный
                } else {
                    g2d.setColor(new Color(0, 0, 0, 20)); // Черный
                }

                int x = col * cellSize + cellSize/2 - 3;
                int y = row * cellSize + cellSize/2 + 3;
                g2d.drawString(suit, x, y);

                g2d.setColor(new Color(0, 0, 0, 12));
            }
        }
    }

    private static void drawChessGrid(Graphics2D g2d, int width, int height) {
        int cellSize = 12;
        Color light = new Color(0, 0, 0, 8);
        Color dark = new Color(0, 0, 0, 18);

        for (int col = 0; col < width; col += cellSize) {
            for (int row = 0; row < height; row += cellSize) {
                if ((col/cellSize + row/cellSize) % 2 == 0) {
                    g2d.setColor(light);
                } else {
                    g2d.setColor(dark);
                }
                g2d.fillRect(col, row, cellSize, cellSize);
            }
        }
    }

    private static void drawBorder(Graphics2D g2d, int width, int height, Color bgColor) {
        g2d.setColor(bgColor.darker().darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(1, 1, width - 3, height - 3);

        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawRect(2, 2, width - 5, height - 5);
    }
}