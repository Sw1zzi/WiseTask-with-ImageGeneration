package ru.spb.ipo.generator.image_generator.service;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Генератор изображений для карточных задач.
 */
public class CardsImageGenerator {
    private Random random = new Random();

    // Константы размеров изображения
    private static final int IMAGE_WIDTH = 500;
    private static final int IMAGE_HEIGHT = 110;
    private static final int CARD_WIDTH = 60;
    private static final int CARD_HEIGHT = 90;

    // Текущая выбранная рубашка
    private String currentCardBack = "rubashka.png";

    /**
     * Класс для хранения информации о карте
     */
    private static class SimpleCard {
        String rank;
        String suit;

        SimpleCard(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }

        @Override
        public String toString() {
            return rank + " " + suit;
        }
    }

    /**
     * Генерирует изображение для карточной задачи
     */
    public BufferedImage generateImage(ProblemContext context) {
        System.out.println("\n=== CARDS IMAGE GENERATOR ===");

        // Получаем карты из контекста через reflection (так как нет прямого доступа)
        List<SimpleCard> targetCards = extractCardsFromContext(context);

        System.out.println("Найдено карт: " + targetCards.size());
        for (SimpleCard card : targetCards) {
            System.out.println("  - " + card);
        }

        // Выбираем рубашку
        selectRandomCardBack();

        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Включаем сглаживание
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Рисуем фон и визуализацию
        drawBackground(g2d);
        drawVisualization(g2d, context, targetCards);

        g2d.dispose();
        return image;
    }

    /**
     * Извлекает карты из ProblemContext через reflection
     */
    private List<SimpleCard> extractCardsFromContext(ProblemContext context) {
        List<SimpleCard> cards = new ArrayList<>();

        try {
            // Пытаемся получить список карт из контекста
            java.lang.reflect.Method getTargetCardsMethod = context.getClass().getMethod("getTargetCards");
            Object cardsList = getTargetCardsMethod.invoke(context);

            if (cardsList instanceof List) {
                for (Object cardObj : (List<?>) cardsList) {
                    // Получаем ранг и масть карты
                    java.lang.reflect.Method getRankMethod = cardObj.getClass().getMethod("getRank");
                    java.lang.reflect.Method getSuitMethod = cardObj.getClass().getMethod("getSuit");

                    String rank = (String) getRankMethod.invoke(cardObj);
                    String suit = (String) getSuitMethod.invoke(cardObj);

                    cards.add(new SimpleCard(rank, suit));
                }
            }
        } catch (Exception e) {
            System.out.println("Не удалось извлечь карты из контекста: " + e.getMessage());
            // Добавляем демо-карты
            cards.add(new SimpleCard("7", "DIAMONDS"));
            cards.add(new SimpleCard("ACE", "HEARTS"));
            cards.add(new SimpleCard("KING", "SPADES"));
        }

        return cards;
    }

    /**
     * Выбирает случайную рубашку карты
     */
    private void selectRandomCardBack() {
        // Проверяем какие рубашки есть
        String[] possibleNames = {
                "rubashka.png",
                "rubashka1.png",
                "rubashka2.png",
                "rubashka3.png",
                "rubashka4.png",
                "card_back.png",
                "back.png",
                "deck_back.png"
        };

        List<String> existingFiles = new ArrayList<>();

        for (String fileName : possibleNames) {
            File file = new File("imgs/" + fileName);
            if (file.exists()) {
                existingFiles.add(fileName);
            }
        }

        if (!existingFiles.isEmpty()) {
            currentCardBack = existingFiles.get(random.nextInt(existingFiles.size()));
        }

        System.out.println("Используется рубашка: " + currentCardBack);
    }

    /**
     * Загружает изображение из файла
     */
    private Image loadImage(String filename) {
        try {
            File file = new File("imgs/" + filename);
            if (file.exists()) {
                return ImageIO.read(file);
            }

            // Пробуем tasks/imgs
            file = new File("tasks/imgs/" + filename);
            if (file.exists()) {
                return ImageIO.read(file);
            }

            System.err.println("Файл не найден: " + filename);
            return null;

        } catch (IOException e) {
            System.err.println("Ошибка загрузки " + filename + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Возвращает изображение текущей рубашки карты
     */
    private Image getCurrentCardBack() {
        Image image = loadImage(currentCardBack);
        if (image != null) {
            return image;
        }

        // Создаем простую рубашку
        return createSimpleCardBack();
    }

    /**
     * Возвращает изображение конкретной карты
     */
    private Image getCardImage(SimpleCard card) {
        System.out.println("Попытка загрузить карту: " + card.rank + " " + card.suit);

        String fileName = convertCardToFilename(card.rank, card.suit);
        System.out.println("Ищу файл: " + fileName);

        Image image = loadImage(fileName);
        if (image != null) {
            System.out.println("Карта найдена: " + fileName);
            return image;
        }

        // Если не нашли, создаем временную карту
        System.out.println("Карта не найдена, создаю временное изображение");
        return createTempCardImage(card.rank, card.suit);
    }

    /**
     * Конвертирует карту в имя файла
     */
    private String convertCardToFilename(String rank, String suit) {
        String fileRank = convertRank(rank);
        String fileSuit = convertSuit(suit);
        return fileRank + fileSuit + ".png";
    }

    /**
     * Конвертирует ранг
     */
    private String convertRank(String rank) {
        return switch (rank.toUpperCase()) {
            case "ACE" -> "as";
            case "KING" -> "king";
            case "QUEEN" -> "dam";
            case "JACK" -> "valet";
            default -> rank.toLowerCase();
        };
    }

    /**
     * Конвертирует масть
     */
    private String convertSuit(String suit) {
        return switch (suit.toUpperCase()) {
            case "DIAMONDS" -> "bub";
            case "HEARTS" -> "heart";
            case "SPADES" -> "pik";
            case "CLUBS" -> "tref";
            default -> suit.toLowerCase();
        };
    }

    /**
     * Создает простую рубашку карты
     */
    private Image createSimpleCardBack() {
        BufferedImage image = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Фон рубашки
        Color[] backColors = {
                new Color(30, 30, 120),
                new Color(120, 30, 30),
                new Color(30, 120, 30),
                new Color(120, 120, 30)
        };

        g2d.setColor(backColors[random.nextInt(backColors.length)]);
        g2d.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT);

        // Внутренний прямоугольник
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(5, 5, CARD_WIDTH - 10, CARD_HEIGHT - 10);

        g2d.dispose();
        return image;
    }

    /**
     * Создает временное изображение карты
     */
    private Image createTempCardImage(String rank, String suit) {
        BufferedImage image = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Белый фон
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT);

        // Цветная рамка
        if (suit.equalsIgnoreCase("HEARTS") || suit.equalsIgnoreCase("DIAMONDS")) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.BLACK);
        }
        g2d.drawRect(0, 0, CARD_WIDTH - 1, CARD_HEIGHT - 1);

        // Символ масти
        String suitSymbol = getSuitSymbol(suit);

        // Текст карты
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(rank, 5, 15);
        g2d.drawString(suitSymbol, 5, 30);

        // Большой символ в центре
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(suitSymbol);
        int x = (CARD_WIDTH - textWidth) / 2;
        int y = CARD_HEIGHT / 2 + fm.getAscent() / 2;
        g2d.drawString(suitSymbol, x, y);

        g2d.dispose();
        return image;
    }

    /**
     * Возвращает символ масти
     */
    private String getSuitSymbol(String suit) {
        return switch (suit.toUpperCase()) {
            case "HEARTS" -> "♥";
            case "DIAMONDS" -> "♦";
            case "SPADES" -> "♠";
            case "CLUBS" -> "♣";
            default -> suit.substring(0, 1);
        };
    }

    /**
     * Рисует фон
     */

    private void drawBackground(Graphics2D g2d) {
        // Используем готовый BackgroundGenerator с конкретным стилем
        BackgroundGenerator.drawBackground(g2d, IMAGE_WIDTH, IMAGE_HEIGHT,
                BackgroundGenerator.Style.CARDS);
    }

    /**
     * Рисует основную визуализацию
     */
    private void drawVisualization(Graphics2D g2d, ProblemContext context, List<SimpleCard> targetCards) {
        int verticalMargin = (IMAGE_HEIGHT - CARD_HEIGHT) / 2;
        int deckStartX = 10;
        int deckStartY = 12;
        int cardSpacing = 5;

        // Получаем параметры из контекста
        int deckSize = 36;
        int drawCount = 5;

        try {
            java.lang.reflect.Method getDeckSizeMethod = context.getClass().getMethod("getDeckSize");
            java.lang.reflect.Method getDrawCountMethod = context.getClass().getMethod("getDrawCount");

            deckSize = (int) getDeckSizeMethod.invoke(context);
            drawCount = (int) getDrawCountMethod.invoke(context);
        } catch (Exception e) {
            System.out.println("Не удалось получить параметры из контекста: " + e.getMessage());
        }

        // Количество карт для отображения
        int cardsToShow = Math.max(drawCount, targetCards.size());
        cardsToShow = Math.min(cardsToShow, 6);

        // Рассчитываем позиции
        int totalCardsWidth = cardsToShow * CARD_WIDTH + (cardsToShow - 1) * cardSpacing;
        int resultStartX = IMAGE_WIDTH - 10 - totalCardsWidth;
        int resultStartY = verticalMargin;

        // Позиции стрелки
        int arrowStartX = deckStartX + CARD_WIDTH + 8;
        int arrowEndX = resultStartX - 5;
        int arrowY = deckStartY + CARD_HEIGHT / 2;

        // Рисуем все компоненты
        drawDeck(g2d, deckSize, deckStartX, deckStartY);
        drawArrow(g2d, arrowStartX, arrowEndX, arrowY, cardsToShow);
        drawResultCards(g2d, targetCards, resultStartX, resultStartY, cardsToShow, cardSpacing);
    }

    /**
     * Рисует колоду карт
     */
    private void drawDeck(Graphics2D g2d, int deckSize, int x, int y) {
        Image cardBack = getCurrentCardBack();

        // Рисуем стопку
        for (int i = 0; i < 3; i++) {
            int offsetX = i * 2;
            int offsetY = -i * 2;

            if (cardBack != null) {
                g2d.drawImage(cardBack, x + offsetX, y + offsetY, CARD_WIDTH, CARD_HEIGHT, null);
            } else {
                g2d.setColor(new Color(30, 30, 120));
                g2d.fillRect(x + offsetX, y + offsetY, CARD_WIDTH, CARD_HEIGHT);
                g2d.setColor(Color.YELLOW);
                g2d.fillRect(x + offsetX + 5, y + offsetY + 5, CARD_WIDTH - 10, CARD_HEIGHT - 10);
            }
        }

        // Размер колоды
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String sizeText = String.valueOf(deckSize);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(sizeText);
        int centerX = x + CARD_WIDTH / 2 + 4;
        int centerY = y + CARD_HEIGHT / 2;
        int textX = centerX - textWidth / 2;
        int textY = centerY + fm.getAscent() / 2 - 5;

        // Фон
        g2d.setColor(new Color(0, 0, 0, 180));
        int padding = 4;
        g2d.fillRoundRect(textX - padding, textY - fm.getAscent() - padding / 2,
                textWidth + padding * 2, fm.getHeight() + padding, 5, 5);

        // Текст
        g2d.setColor(Color.WHITE);
        g2d.drawString(sizeText, textX, textY);
    }

    /**
     * Рисует стрелку
     */
    private void drawArrow(Graphics2D g2d, int startX, int endX, int y, int cardsToShow) {
        if (cardsToShow == 6) {
            return;
        }

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int baseLength;
        if (cardsToShow == 1) {
            baseLength = 400;
        } else {
            baseLength = Math.max(80, Math.min(400, (IMAGE_WIDTH - startX - 20) - (cardsToShow * 15)));
        }

        int actualEndX = Math.min(endX, startX + baseLength);
        int lineStartX = startX + 12;
        int lineEndX = actualEndX - 28;

        if (lineEndX <= lineStartX) {
            lineEndX = lineStartX + 50;
        }

        // Линия
        g2d.drawLine(lineStartX, y, lineEndX, y);

        // Наконечник
        int tipX = lineEndX + 10;
        int tipWidth = 14;
        int tipHeight = 10;

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(tipX, y);
        arrowHead.addPoint(tipX - tipWidth, y - tipHeight / 2);
        arrowHead.addPoint(tipX - tipWidth, y + tipHeight / 2);

        g2d.fill(arrowHead);

        // Точка в начале
        g2d.fillOval(lineStartX - 4, y - 4, 8, 8);

        // Подпись
        if (cardsToShow > 0 && cardsToShow != 1) {
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            String cardWord;
            switch (cardsToShow) {
                case 2:
                case 3:
                case 4: cardWord = "карты"; break;
                default: cardWord = "карт"; break;
            }

            String countText = cardsToShow + " " + cardWord;
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(countText);
            int textX = (lineStartX + lineEndX) / 2 - textWidth / 2;
            int textY = y - 10;

            g2d.setColor(new Color(255, 255, 255, 220));
            g2d.fillRoundRect(textX - 5, textY - fm.getAscent() + 3,
                    textWidth + 10, fm.getHeight(), 6, 6);

            g2d.setColor(Color.BLACK);
            g2d.drawString(countText, textX, textY);
        }
    }

    /**
     * Рисует вытянутые карты
     */
    private void drawResultCards(Graphics2D g2d, List<SimpleCard> targetCards, int startX, int startY,
                                 int cardsToShow, int spacing) {
        System.out.println("Рисую " + cardsToShow + " карт. Целевых карт: " + targetCards.size());

        // Создаем список для отображения
        List<SimpleCard> cardsToDisplay = new ArrayList<>(targetCards);

        // Добиваем рубашками если нужно
        while (cardsToDisplay.size() < cardsToShow) {
            cardsToDisplay.add(null); // null означает рубашку
        }

        // Перемешиваем
        Collections.shuffle(cardsToDisplay, random);

        // Рисуем
        for (int i = 0; i < cardsToShow; i++) {
            int cardX = startX + i * (CARD_WIDTH + spacing);
            SimpleCard card = cardsToDisplay.get(i);

            if (card != null) {
                // Конкретная карта
                System.out.println("Карта " + (i+1) + ": " + card);
                Image cardImage = getCardImage(card);
                if (cardImage != null) {
                    g2d.drawImage(cardImage, cardX, startY, CARD_WIDTH, CARD_HEIGHT, null);
                } else {
                    drawSimpleCard(g2d, cardX, startY, card.rank, card.suit);
                }
            } else {
                // Рубашка
                Image cardBack = getCurrentCardBack();
                if (cardBack != null) {
                    g2d.drawImage(cardBack, cardX, startY, CARD_WIDTH, CARD_HEIGHT, null);
                } else {
                    g2d.setColor(new Color(50, 50, 150));
                    g2d.fillRect(cardX, startY, CARD_WIDTH, CARD_HEIGHT);
                }
            }
        }
    }

    /**
     * Рисует простую карту
     */
    private void drawSimpleCard(Graphics2D g2d, int x, int y, String rank, String suit) {
        // Белый фон
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 10, 10);

        // Рамка
        if (suit.equalsIgnoreCase("HEARTS") || suit.equalsIgnoreCase("DIAMONDS")) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.BLACK);
        }
        g2d.drawRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 10, 10);

        // Текст
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString(rank, x + 5, y + 20);
        g2d.drawString(suit.substring(0, 1), x + 5, y + 35);
    }
}