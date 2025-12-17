package ru.spb.ipo.generator.image_generator.service;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BallsImageGenerator {
    private Map<String, Image> imageCache = new HashMap<>();
    private ArrayList<String> availableImages = new ArrayList<>();
    private Random random = new Random();

    private static final int IMAGE_WIDTH = 500;
    private static final int IMAGE_HEIGHT = 110;
    private static final int BALL_SIZE = 26;
    private static final int BASKET_WIDTH = 290;
    private static final int BASKET_HEIGHT = 220;

    // Цвета для шаров
    private static final Map<String, Color> BALL_COLORS = new HashMap<>();

    static {
        BALL_COLORS.put("RED", new Color(255, 100, 100));
        BALL_COLORS.put("BLUE", new Color(100, 150, 255));
        BALL_COLORS.put("GREEN", new Color(100, 200, 100));
        BALL_COLORS.put("WHITE", new Color(230, 230, 230));
        BALL_COLORS.put("BLACK", new Color(80, 80, 80));
    }

    public BallsImageGenerator() {
        scanAvailableImages();
    }

    private void scanAvailableImages() {
        try {
            URL resourcesUrl = getClass().getClassLoader().getResource("imgs");
            if (resourcesUrl != null) {
                java.io.File imgsDir = new java.io.File(resourcesUrl.getFile());
                if (imgsDir.exists() && imgsDir.isDirectory()) {
                    String[] files = imgsDir.list((dir, name) ->
                            name.toLowerCase().endsWith(".png"));
                    if (files != null) {
                        availableImages = new ArrayList<>(Arrays.asList(files));
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибку сканирования
        }
    }

    private Image loadImage(String filename) {
        if (imageCache.containsKey(filename)) {
            return imageCache.get(filename);
        }

        try {
            Image image = ImageIO.read(getClass().getResourceAsStream("/imgs/" + filename));
            if (image != null) {
                imageCache.put(filename, image);
                return image;
            }
        } catch (Exception e) {
            // Игнорируем ошибку загрузки
        }
        return null;
    }

    private Image getBasketImage() {
        // Сначала пробуем basket.png
        Image basket = loadImage("basket.png");
        if (basket != null) {
            return basket;
        }

        // Если нет, ищем другие варианты
        String[] possibleNames = {"korzina.png", "basket1.png", "basket2.png", "basket3.png"};
        for (String name : possibleNames) {
            Image image = loadImage(name);
            if (image != null) {
                return image;
            }
        }

        // Если совсем нет файлов, создаем простую большую корзину
        return createLargeBasket();
    }

    private Image createLargeBasket() {
        BufferedImage image = new BufferedImage(BASKET_WIDTH, BASKET_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Большая плетеная корзина
        Color basketColor = new Color(160, 120, 80);
        Color basketDark = new Color(140, 100, 60);

        // Основная часть
        g2d.setColor(basketColor);
        g2d.fillRoundRect(10, 20, BASKET_WIDTH - 20, BASKET_HEIGHT - 30, 20, 20);

        // Ободок сверху (толще)
        g2d.setColor(basketDark);
        g2d.fillRoundRect(5, 15, BASKET_WIDTH - 10, 15, 8, 8);

        // Металлическая отделка ободка
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(5, 15, BASKET_WIDTH - 10, 15, 8, 8);

        // Ручки (побольше)
        g2d.setColor(new Color(120, 80, 40));
        g2d.setStroke(new BasicStroke(4));
        // Левая ручка
        g2d.drawArc(0, 20, 20, 25, 90, 180);
        // Правая ручка
        g2d.drawArc(BASKET_WIDTH - 20, 20, 20, 25, 270, 180);

        // Плетение (полоски)
        g2d.setColor(basketDark);
        g2d.setStroke(new BasicStroke(2));
        // Вертикальные полоски
        for (int i = 15; i < BASKET_WIDTH - 15; i += 20) {
            g2d.drawLine(i, 35, i, BASKET_HEIGHT - 10);
        }
        // Горизонтальные полоски
        for (int i = 40; i < BASKET_HEIGHT - 10; i += 15) {
            g2d.drawLine(12, i, BASKET_WIDTH - 12, i);
        }

        g2d.dispose();
        return image;
    }

    public BufferedImage generateImage(ProblemContext context) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Мягкий фон
        drawBackground(g2d);

        // Получаем данные
        Map<String, Integer> urnContents = extractUrnContents(context);
        Map<String, Integer> drawBalls = extractDrawBalls(context);
        boolean isSequential = extractDrawType(context);

        // Рисуем
        drawVisualization(g2d, urnContents, drawBalls, isSequential);

        g2d.dispose();
        return image;
    }

    private void drawBackground(Graphics2D g2d) {
        // Сетка (без шариков)
        BackgroundGenerator.drawBackground(g2d, IMAGE_WIDTH, IMAGE_HEIGHT,
                BackgroundGenerator.Style.GRID);
    }

    private Map<String, Integer> extractUrnContents(ProblemContext context) {
        Map<String, Integer> contents = new HashMap<>();

        Object contentsParam = context.getParameter("contents");
        if (contentsParam instanceof String) {
            parseBallsString((String) contentsParam, contents);
        }

        if (contents.isEmpty()) {
            for (String key : context.getAllParameters().keySet()) {
                if (key.startsWith("ball_")) {
                    String color = key.substring(5).toUpperCase();
                    if (BALL_COLORS.containsKey(color)) {
                        int count = context.getIntParameter(key);
                        if (count > 0) contents.put(color, count);
                    }
                }
            }
        }

        if (contents.isEmpty()) {
            contents.put("RED", 4);
            contents.put("BLUE", 3);
            contents.put("GREEN", 2);
            contents.put("WHITE", 1);
        }

        return contents;
    }

    private Map<String, Integer> extractDrawBalls(ProblemContext context) {
        Map<String, Integer> drawBalls = new HashMap<>();

        Object drawParam = context.getParameter("draw_balls");
        if (drawParam instanceof String) {
            parseBallsString((String) drawParam, drawBalls);
        }

        if (drawBalls.isEmpty()) {
            for (String key : context.getAllParameters().keySet()) {
                if (key.startsWith("draw_")) {
                    String color = key.substring(5).toUpperCase();
                    if (BALL_COLORS.containsKey(color)) {
                        int count = context.getIntParameter(key);
                        if (count > 0) drawBalls.put(color, count);
                    }
                }
            }
        }

        if (drawBalls.isEmpty()) {
            int drawCount = context.getIntParameter("drawCount");
            if (drawCount > 0) {
                drawBalls.put("RED", Math.min(1, drawCount));
                if (drawCount > 1) drawBalls.put("BLUE", 1);
                if (drawCount > 2) drawBalls.put("GREEN", 1);
            } else {
                drawBalls.put("RED", 1);
                drawBalls.put("BLUE", 1);
            }
        }

        return drawBalls;
    }

    private boolean extractDrawType(ProblemContext context) {
        Object drawTypeParam = context.getParameter("drawType");
        if (drawTypeParam instanceof String) {
            String drawType = ((String) drawTypeParam).toUpperCase();
            return "SEQUENTIAL".equals(drawType) || "DRAW_SEQUENTIAL".equals(drawType);
        }
        return true;
    }

    private void parseBallsString(String ballsStr, Map<String, Integer> result) {
        if (ballsStr == null || ballsStr.isEmpty()) return;

        String[] parts = ballsStr.split("[,\\s]+");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].toUpperCase();
            if (BALL_COLORS.containsKey(part)) {
                int count = 1;
                if (i + 1 < parts.length && parts[i + 1].matches("\\d+")) {
                    count = Integer.parseInt(parts[i + 1]);
                    i++;
                }
                result.put(part, result.getOrDefault(part, 0) + count);
            }
        }
    }

    private void drawVisualization(Graphics2D g2d, Map<String, Integer> urnContents,
                                   Map<String, Integer> drawBalls, boolean isSequential) {
        int basketX = -60;
        int basketY = (IMAGE_HEIGHT - BASKET_HEIGHT) / 2 - 8;

        // Рисуем большую корзину с шариками внутри
        drawBasketWithBalls(g2d, basketX, basketY, urnContents);

        int arrowStartX = basketX + BASKET_WIDTH - 40;
        int arrowEndX = IMAGE_WIDTH - 180; // СДВИНУЛИ ВЛЕВО на 40px
        int arrowY = basketY + BASKET_HEIGHT / 2 + 10;

        drawArrow(g2d, arrowStartX, arrowY, arrowEndX, arrowY, isSequential);

        // Рисуем вытягиваемые шары (тоже сдвигаем влево)
        int drawStartX = arrowEndX + 20;

        // Центральная Y-координата для размещения шаров
        int centerY = arrowY - BALL_SIZE / 2;

        drawExtractedBalls(g2d, drawStartX, centerY, drawBalls, isSequential);
    }

    private void drawBasketWithBalls(Graphics2D g2d, int x, int y, Map<String, Integer> contents) {
        // Рисуем большую корзину из файла
        Image basket = getBasketImage();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));

        g2d.drawImage(basket, x, y, BASKET_WIDTH, BASKET_HEIGHT, null);

        // Преобразуем содержимое в список для упорядоченного рисования
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(contents.entrySet());

        // Перемешиваем порядок шаров для случайного расположения
        Collections.shuffle(entries, random);

        // Рассчитываем позиции для шаров внутри большой корзины
        int centerX = x + BASKET_WIDTH / 2;
        int centerY = y + BASKET_HEIGHT / 2 + 10;

        // В зависимости от количества шаров выбираем layout
        int ballCount = Math.min(entries.size(), 5); // Максимум 5 шаров

        for (int i = 0; i < ballCount; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            String color = entry.getKey();
            int count = entry.getValue();
            Color ballColor = BALL_COLORS.get(color);

            // Позиционируем шары внутри корзины в тех же позициях, но в случайном порядке
            int ballX, ballY;

            if (ballCount <= 3) {
                // 1-3 шара: в верхней части корзины
                int startX = centerX - (ballCount * (BALL_SIZE + 8)) / 2 + (BALL_SIZE + 8) / 2;
                startX -= 11;
                ballX = startX + i * (BALL_SIZE + 8);
                ballY = centerY - 17;
            } else {
                // 4-5 шаров: первые 3 сверху, остальные снизу
                if (i < 3) {
                    // Верхний ряд
                    int startX = centerX - (3 * (BALL_SIZE + 8)) / 2 + (BALL_SIZE + 8) / 2;
                    startX -= 13; // СДВИГ ВЛЕВО
                    ballX = startX + i * (BALL_SIZE + 8);
                    ballY = centerY - 16;
                } else {
                    // Нижний ряд (для 4го и 5го шаров)
                    int startX = centerX - ((ballCount - 3) * (BALL_SIZE + 12)) / 2 + (BALL_SIZE + 12) / 2;
                    startX -= 12; // СДВИГ ВЛЕВО (МЕНЬШЕ ЧЕМ У ВЕРХНИХ)
                    ballX = startX + (i - 3) * (BALL_SIZE + 12);
                    ballY = centerY + 12;
                }
            }

            // Рисуем шар с цифрой внутри корзина
            drawBallWithNumber(g2d, ballX, ballY, ballColor, count);
        }
    }

    private void drawBallWithNumber(Graphics2D g2d, int x, int y, Color color, int count) {
        // 1. Тень (смещение вниз-вправо)
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillOval(x + 2, y + 2, BALL_SIZE, BALL_SIZE);

        // 2. Основной шар с мягким градиентом
        RadialGradientPaint gradient = new RadialGradientPaint(
                new Point(x + BALL_SIZE/4, y + BALL_SIZE/4),
                BALL_SIZE * 0.7f,
                new float[]{0.0f, 1.0f},
                new Color[]{color.brighter(), color.darker()}
        );
        g2d.setPaint(gradient);
        g2d.fillOval(x, y, BALL_SIZE, BALL_SIZE);

        // 3. Обводка с эффектом объема
        g2d.setColor(color.darker().darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x, y, BALL_SIZE, BALL_SIZE);

        // 4. Яркий блик (маленький круг в левом верхнем углу)
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.fillOval(x + 4, y + 4, 8, 8);

        // 5. Контрастный круг для цифры
        int circleSize = 20;
        int circleX = x + (BALL_SIZE - circleSize) / 2;
        int circleY = y + (BALL_SIZE - circleSize) / 2;

        // Круг с градиентом
        GradientPaint circleGradient = new GradientPaint(
                circleX, circleY, Color.WHITE,
                circleX + circleSize, circleY + circleSize, new Color(240, 240, 240),
                true
        );
        g2d.setPaint(circleGradient);
        g2d.fillOval(circleX, circleY, circleSize, circleSize);

        // Обводка круга
        g2d.setColor(new Color(180, 180, 180));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawOval(circleX, circleY, circleSize, circleSize);

        // 6. Цифра (ПРАВИЛЬНОЕ ЦЕНТРИРОВАНИЕ)
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String countStr = String.valueOf(count);
        FontMetrics fm = g2d.getFontMetrics();

        // Центр шара
        int centerX = x + BALL_SIZE / 2;
        int centerY = y + BALL_SIZE / 2;

        // ПРАВИЛЬНОЕ ВЫЧИСЛЕНИЕ позиции текста
        int textWidth = fm.stringWidth(countStr);
        int textX = centerX - textWidth / 2;

        // Это правильный способ центрирования по вертикали:
        // centerY + (fm.getAscent() - fm.getDescent()) / 2
        int textY = centerY + (fm.getAscent() - fm.getDescent()) / 2;

        // Небольшая тень у цифры для объема
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.drawString(countStr, textX + 1, textY + 1);

        // Основная цифра (теперь точно по центру)
        g2d.setColor(Color.BLACK);
        g2d.drawString(countStr, textX, textY);
    }

    private void drawArrow(Graphics2D g2d, int startX, int y, int endX, int endY, boolean isSequential) {
        // ЧЕРНАЯ ЖИРНАЯ СТРЕЛКА
        Color arrowColor = Color.BLACK;
        g2d.setColor(arrowColor);
        g2d.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Основная линия стрелки
        g2d.drawLine(startX, y, endX, endY);

        // Наконечник стрелки
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(endX, endY);
        arrowHead.addPoint(endX - 10, endY - 5);
        arrowHead.addPoint(endX - 10, endY + 5);
        g2d.fill(arrowHead);

        // 1. НАДПИСЬ "ВЫТАСКИВАЮТСЯ" НАД СТРЕЛКОЙ - БЕЗ ФОНА
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.BLACK);
        String drawText = "Вытаскиваются";
        FontMetrics fm1 = g2d.getFontMetrics();
        int drawTextWidth = fm1.stringWidth(drawText);
        int drawTextX = (startX + endX) / 2 - drawTextWidth / 2;
        int drawTextY = y - 10;
        g2d.drawString(drawText, drawTextX, drawTextY);

        // 2. Текст под стрелкой - "Последовательно" или "Одновременно" - БЕЗ ФОНА
        String methodText = isSequential ? "Последовательно" : "Одновременно";
        FontMetrics fm2 = g2d.getFontMetrics();
        int methodX = (startX + endX) / 2 - fm2.stringWidth(methodText) / 2;
        int methodY = y + 20;
        g2d.drawString(methodText, methodX, methodY);
    }

    private void drawExtractedBalls(Graphics2D g2d, int startX, int centerY,
                                    Map<String, Integer> drawBalls, boolean isSequential) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(drawBalls.entrySet());
        Collections.shuffle(entries, random);

        int totalBalls = entries.size();

        if (isSequential) {
            // ПОСЛЕДОВАТЕЛЬНОЕ ВЫТЯГИВАНИЕ
            if (totalBalls <= 3) {
                // 1-3 шара: в один ряд со стрелками
                drawSequentialSingleRow(g2d, startX, centerY, entries, totalBalls);
            } else {
                // 4+ шаров: в несколько рядов как при одновременном
                drawSequentialMultiRow(g2d, startX, centerY, entries, totalBalls);
            }
        } else {
            // ОДНОВРЕМЕННОЕ ВЫТЯГИВАНИЕ: два ряда, нижние между верхними
            drawSimultaneousTwoRows(g2d, startX, centerY, entries, totalBalls);
        }
    }

    private void drawSequentialSingleRow(Graphics2D g2d, int startX, int startY,
                                         List<Map.Entry<String, Integer>> entries, int totalBalls) {
        // Все шары в одном ряду
        int ballX = startX;
        for (int i = 0; i < totalBalls; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            String color = entry.getKey();
            int count = entry.getValue();
            Color ballColor = BALL_COLORS.get(color);

            // Рисуем шар
            drawBallWithNumber(g2d, ballX, startY, ballColor, count);

            // Рисуем УМЕНЬШЕННУЮ стрелку ПОД шаром
            drawSequentialArrow(g2d, ballX, startY, i + 1);

            ballX += BALL_SIZE + 25;
        }
    }

    private void drawSequentialMultiRow(Graphics2D g2d, int startX, int startY,
                                        List<Map.Entry<String, Integer>> entries, int totalBalls) {
        // Рассчитываем сколько шаров в верхнем и нижнем ряду
        int topRowBalls = Math.min(totalBalls, 3);
        int bottomRowBalls = Math.max(0, totalBalls - 3);

        // Расстояние между рядами
        int bottomY = startY + BALL_SIZE + 20;

        // Верхний ряд
        int ballX = startX;
        for (int i = 0; i < topRowBalls; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            String color = entry.getKey();
            int count = entry.getValue();
            Color ballColor = BALL_COLORS.get(color);

            drawBallWithNumber(g2d, ballX, startY, ballColor, count);

            // Рисуем стрелку под верхним рядом
            drawSequentialArrow(g2d, ballX, startY, i + 1);

            ballX += BALL_SIZE + 15;
        }

        // Нижний ряд (если больше 3 шаров)
        if (bottomRowBalls > 0) {
            int bottomStartX;
            int bottomSpacing;

            if (bottomRowBalls == 1) {
                // 1 нижний шар (4 шарика всего) - ПОД ЦЕНТРОМ МЕЖДУ 1 И 2 ВЕРХНИМИ
                bottomStartX = startX + BALL_SIZE/2 + 8;
                bottomSpacing = 0;
            } else if (bottomRowBalls == 2) {
                // 2 нижних шара (5 шариков всего) - МЕЖДУ ВЕРХНИМИ
                bottomStartX = startX + BALL_SIZE - 5;
                bottomSpacing = BALL_SIZE + 15;
            } else {
                // 3+ нижних шара (6+ шариков) - равномерно
                bottomStartX = startX;
                bottomSpacing = (BALL_SIZE + 15) * 3 / bottomRowBalls;
            }

            for (int i = 0; i < bottomRowBalls; i++) {
                Map.Entry<String, Integer> entry = entries.get(i + 3);
                String color = entry.getKey();
                int count = entry.getValue();
                Color ballColor = BALL_COLORS.get(color);

                int bottomBallX;
                if (bottomRowBalls == 1) {
                    bottomBallX = bottomStartX;
                } else {
                    bottomBallX = bottomStartX + i * bottomSpacing;
                }

                drawBallWithNumber(g2d, bottomBallX, bottomY, ballColor, count);

                // Рисуем стрелку под нижним рядом (номер продолжается)
                drawSequentialArrow(g2d, bottomBallX, bottomY, i + 4);
            }
        }
    }

    private void drawSimultaneousTwoRows(Graphics2D g2d, int startX, int centerY,
                                         List<Map.Entry<String, Integer>> entries, int totalBalls) {
        // Рассчитываем сколько шаров в верхнем и нижнем ряду
        int topRowBalls = Math.min(totalBalls, 3);
        int bottomRowBalls = Math.max(0, totalBalls - 3);

        // Координаты для рядов - центрируем оба ряда вокруг centerY
        int topY = centerY - BALL_SIZE / 2;
        int bottomY = centerY + BALL_SIZE / 2 + 10;

        // Верхний ряд - всегда по центру
        int ballX = startX;
        for (int i = 0; i < topRowBalls; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            String color = entry.getKey();
            int count = entry.getValue();
            Color ballColor = BALL_COLORS.get(color);

            drawBallWithNumber(g2d, ballX, topY, ballColor, count);
            ballX += BALL_SIZE + 15;
        }

        // Нижний ряд (если больше 3 шаров) - нижние шары между верхними
        if (bottomRowBalls > 0) {
            int bottomStartX;
            int bottomSpacing;

            if (bottomRowBalls == 1) {
                // 1 нижний шар (4 шарика всего) - МЕЖДУ 1 И 2 ВЕРХНИМИ ШАРАМИ
                bottomStartX = startX + BALL_SIZE + 8;
                bottomSpacing = 0;
            } else if (bottomRowBalls == 2) {
                // 2 нижних шара (5 шариков всего) - МЕЖДУ ВЕРХНИМИ ШАРАМИ
                // Первый нижний между 1 и 2 верхними, второй между 2 и 3 верхними
                bottomStartX = startX + BALL_SIZE/2 + 8;
                bottomSpacing = BALL_SIZE + 15;
            } else {
                // 3+ нижних шара (6+ шариков) - равномерно, начиная от startX
                bottomStartX = startX;
                bottomSpacing = BALL_SIZE + 15;
            }

            for (int i = 0; i < bottomRowBalls; i++) {
                Map.Entry<String, Integer> entry = entries.get(i + 3);
                String color = entry.getKey();
                int count = entry.getValue();
                Color ballColor = BALL_COLORS.get(color);

                int bottomBallX;
                if (bottomRowBalls == 1) {
                    bottomBallX = bottomStartX;
                } else {
                    bottomBallX = bottomStartX + i * bottomSpacing;
                }

                drawBallWithNumber(g2d, bottomBallX, bottomY, ballColor, count);
            }
        }
    }

    private void drawSequentialArrow(Graphics2D g2d, int ballX, int ballY, int stepNumber) {
        // Центр шара
        int centerX = ballX + BALL_SIZE / 2;

        // Позиция под шаром
        int arrowY = ballY + BALL_SIZE + 12;

        // УМЕНЬШЕННАЯ длина стрелки (было 20, стало 15)
        int arrowLength = 15;
        int arrowStartX = centerX - arrowLength / 2;
        int arrowEndX = centerX + arrowLength / 2;

        // 1. Рисуем номер шага слева от стрелки - БЕЗ ФОНА
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(Color.BLACK);
        String stepText = stepNumber + ".";
        FontMetrics fm = g2d.getFontMetrics();
        int stepTextX = arrowStartX - fm.stringWidth(stepText) - 3;
        int stepTextY = arrowY + 4;
        g2d.drawString(stepText, stepTextX, arrowY + 4);

        // 2. Рисуем жирную стрелку
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Линия стрелки (ЕЩЕ КОРОЧЕ)
        g2d.drawLine(arrowStartX, arrowY, arrowEndX, arrowY);

        // Наконечник стрелки (еще меньше)
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(arrowEndX + 10, arrowY);
        arrowHead.addPoint(arrowEndX - 5, arrowY - 3);
        arrowHead.addPoint(arrowEndX - 5, arrowY + 3);
        g2d.fill(arrowHead);
    }
}