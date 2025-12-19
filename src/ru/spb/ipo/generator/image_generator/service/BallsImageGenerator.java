package ru.spb.ipo.generator.image_generator.service;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class BallsImageGenerator {
    private Map<String, Image> imageCache = new HashMap<>();
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
        BALL_COLORS.put("YELLOW", new Color(255, 255, 100));
        BALL_COLORS.put("ORANGE", new Color(255, 180, 50));
        BALL_COLORS.put("PURPLE", new Color(180, 100, 220));
    }

    public BallsImageGenerator() {
        // Пустой конструктор
    }

    private Image loadImage(String filename) {
        if (imageCache.containsKey(filename)) {
            return imageCache.get(filename);
        }

        // Пробуем несколько путей
        String[] possiblePaths = {
                "imgs/" + filename,
                "tasks/imgs/" + filename,
                filename
        };

        for (String path : possiblePaths) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    Image image = ImageIO.read(file);
                    if (image != null) {
                        imageCache.put(filename, image);
                        return image;
                    }
                }
            } catch (IOException e) {
                // Игнорируем
            }
        }

        return null;
    }

    private Image getBasketImage() {
        String[] possibleNames = {
                "basket.png",
                "korzina.png",
                "basket1.png",
                "basket2.png",
                "basket3.png"
        };

        for (String name : possibleNames) {
            Image image = loadImage(name);
            if (image != null) {
                return image;
            }
        }

        // Если нет файлов, создаем простую корзину
        return createLargeBasket();
    }

    private Image createLargeBasket() {
        BufferedImage image = new BufferedImage(BASKET_WIDTH, BASKET_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color basketColor = new Color(160, 120, 80);
        Color basketDark = new Color(140, 100, 60);

        g2d.setColor(basketColor);
        g2d.fillRoundRect(10, 20, BASKET_WIDTH - 20, BASKET_HEIGHT - 30, 20, 20);

        g2d.setColor(basketDark);
        g2d.fillRoundRect(5, 15, BASKET_WIDTH - 10, 15, 8, 8);

        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(5, 15, BASKET_WIDTH - 10, 15, 8, 8);

        g2d.setColor(new Color(120, 80, 40));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawArc(0, 20, 20, 25, 90, 180);
        g2d.drawArc(BASKET_WIDTH - 20, 20, 20, 25, 270, 180);

        g2d.setColor(basketDark);
        g2d.setStroke(new BasicStroke(2));
        for (int i = 15; i < BASKET_WIDTH - 15; i += 20) {
            g2d.drawLine(i, 35, i, BASKET_HEIGHT - 10);
        }
        for (int i = 40; i < BASKET_HEIGHT - 10; i += 15) {
            g2d.drawLine(12, i, BASKET_WIDTH - 12, i);
        }

        g2d.dispose();
        return image;
    }

    public BufferedImage generateImage(ProblemContext context) {
        System.out.println("\n=== BALLS IMAGE GENERATOR ===");

        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Фон
        BackgroundGenerator.drawBackground(g2d, IMAGE_WIDTH, IMAGE_HEIGHT,
                BackgroundGenerator.Style.GRID);

        // Извлекаем данные БЕЗ заглушек
        Map<String, Integer> urnContents = extractUrnContents(context);
        List<DrawBall> drawBalls = extractDrawBalls(context);
        boolean isSequential = extractDrawType(context);

        System.out.println("Урна: " + urnContents);
        System.out.println("Вытягиваемые шары: " + drawBalls);
        System.out.println("Последовательно: " + isSequential);

        // Рисуем
        drawVisualization(g2d, urnContents, drawBalls, isSequential);

        g2d.dispose();
        return image;
    }

    private Map<String, Integer> extractUrnContents(ProblemContext context) {
        Map<String, Integer> contents = new HashMap<>();

        // 1. Параметр "contents" (строка вида "RED 3, BLUE 5")
        Object contentsParam = context.getParameter("contents");
        if (contentsParam instanceof String) {
            String contentStr = (String) contentsParam;
            if (!contentStr.trim().isEmpty()) {
                parseBallsString(contentStr, contents);
            }
        }

        // 2. Параметры вида "ball_red", "ball_blue"
        for (String key : context.getAllParameters().keySet()) {
            if (key.startsWith("ball_")) {
                String color = key.substring(5).toUpperCase();
                if (BALL_COLORS.containsKey(color)) {
                    int count = context.getIntParameter(key);
                    if (count > 0) {
                        contents.put(color, contents.getOrDefault(color, 0) + count);
                    }
                }
            }
        }

        // 3. Если ничего не нашли, используем данные из контекста
        if (contents.isEmpty() && context.getParameter("totalBalls") != null) {
            int totalBalls = context.getIntParameter("totalBalls");
            if (totalBalls > 0) {
                // Создаем демо-набор (но лучше пустую урну, чем заглушку)
                contents.put("RED", 3);
                contents.put("BLUE", 2);
                contents.put("GREEN", 1);
            }
        }

        return contents;
    }

    private List<DrawBall> extractDrawBalls(ProblemContext context) {
        List<DrawBall> drawBalls = new ArrayList<>();

        // 1. Параметр "draw_balls" (строка вида "RED 1, BLUE 1")
        Object drawParam = context.getParameter("draw_balls");
        if (drawParam instanceof String) {
            String drawStr = (String) drawParam;
            if (!drawStr.trim().isEmpty()) {
                parseDrawBallsString(drawStr, drawBalls);
            }
        }

        // 2. Параметры вида "draw_red", "draw_blue"
        for (String key : context.getAllParameters().keySet()) {
            if (key.startsWith("draw_")) {
                String color = key.substring(5).toUpperCase();
                if (BALL_COLORS.containsKey(color)) {
                    int count = context.getIntParameter(key);
                    for (int i = 0; i < count; i++) {
                        drawBalls.add(new DrawBall(color, i + 1));
                    }
                }
            }
        }

        // 3. Параметр "drawCount" - количество шаров для вытягивания
        int drawCount = context.getIntParameter("drawCount");
        if (drawCount > 0 && drawBalls.isEmpty()) {
            // Если указано количество, но не указаны цвета - рисуем стрелку без шаров
            // или можно не рисовать шары вообще
            System.out.println("Указано drawCount=" + drawCount + ", но цвета не указаны");
        }

        // 4. Если drawCount больше чем указанных шаров, добавляем неизвестные
        int specifiedCount = drawBalls.size();
        if (drawCount > specifiedCount) {
            for (int i = specifiedCount + 1; i <= drawCount; i++) {
                drawBalls.add(new DrawBall("UNKNOWN", i));
            }
        }

        return drawBalls;
    }

    private boolean extractDrawType(ProblemContext context) {
        Object drawTypeParam = context.getParameter("drawType");
        if (drawTypeParam instanceof String) {
            String drawType = ((String) drawTypeParam).toUpperCase();
            return "SEQUENTIAL".equals(drawType);
        }
        return true; // По умолчанию - последовательно
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

    private void parseDrawBallsString(String ballsStr, List<DrawBall> result) {
        if (ballsStr == null || ballsStr.isEmpty()) return;

        String[] parts = ballsStr.split("[,\\s]+");
        int ballNumber = 1;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].toUpperCase();
            if (BALL_COLORS.containsKey(part)) {
                int count = 1;
                if (i + 1 < parts.length && parts[i + 1].matches("\\d+")) {
                    count = Integer.parseInt(parts[i + 1]);
                    i++;
                }

                for (int j = 0; j < count; j++) {
                    result.add(new DrawBall(part, ballNumber++));
                }
            }
        }
    }

    private void drawVisualization(Graphics2D g2d, Map<String, Integer> urnContents,
                                   List<DrawBall> drawBalls, boolean isSequential) {
        int basketX = -60;
        int basketY = (IMAGE_HEIGHT - BASKET_HEIGHT) / 2 - 8;

        // Рисуем корзину с шарами
        drawBasketWithBalls(g2d, basketX, basketY, urnContents);

        int arrowStartX = basketX + BASKET_WIDTH - 40;
        int arrowEndX = IMAGE_WIDTH - 180;
        int arrowY = basketY + BASKET_HEIGHT / 2 + 10;

        // Рисуем стрелку ВСЕГДА, даже если нет вытягиваемых шаров
        drawArrow(g2d, arrowStartX, arrowY, arrowEndX, arrowY, isSequential, drawBalls.size());

        // Рисуем вытягиваемые шары ТОЛЬКО если они есть
        if (!drawBalls.isEmpty()) {
            int drawStartX = arrowEndX + 20;
            int centerY = arrowY - BALL_SIZE / 2;
            drawExtractedBalls(g2d, drawStartX, centerY, drawBalls, isSequential);
        }
    }

    private void drawBasketWithBalls(Graphics2D g2d, int x, int y, Map<String, Integer> contents) {
        Image basket = getBasketImage();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        g2d.drawImage(basket, x, y, BASKET_WIDTH, BASKET_HEIGHT, null);

        if (contents.isEmpty()) {
            return; // Пустая урна
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(contents.entrySet());
        Collections.shuffle(entries, random);

        int centerX = x + BASKET_WIDTH / 2;
        int centerY = y + BASKET_HEIGHT / 2 + 10;
        int ballCount = Math.min(entries.size(), 5);

        for (int i = 0; i < ballCount; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            String color = entry.getKey();
            int count = entry.getValue();
            Color ballColor = BALL_COLORS.get(color);

            int ballX, ballY;

            if (ballCount <= 3) {
                int startX = centerX - (ballCount * (BALL_SIZE + 8)) / 2 + (BALL_SIZE + 8) / 2;
                startX -= 11;
                ballX = startX + i * (BALL_SIZE + 8);
                ballY = centerY - 17;
            } else {
                if (i < 3) {
                    int startX = centerX - (3 * (BALL_SIZE + 8)) / 2 + (BALL_SIZE + 8) / 2;
                    startX -= 13;
                    ballX = startX + i * (BALL_SIZE + 8);
                    ballY = centerY - 16;
                } else {
                    int startX = centerX - ((ballCount - 3) * (BALL_SIZE + 12)) / 2 + (BALL_SIZE + 12) / 2;
                    startX -= 12;
                    ballX = startX + (i - 3) * (BALL_SIZE + 12);
                    ballY = centerY + 12;
                }
            }

            drawBallWithNumber(g2d, ballX, ballY, ballColor, count);
        }
    }

    private void drawBallWithNumber(Graphics2D g2d, int x, int y, Color color, int number) {
        // Тень
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillOval(x + 2, y + 2, BALL_SIZE, BALL_SIZE);

        // Градиент
        RadialGradientPaint gradient = new RadialGradientPaint(
                new Point(x + BALL_SIZE/4, y + BALL_SIZE/4),
                BALL_SIZE * 0.7f,
                new float[]{0.0f, 1.0f},
                new Color[]{color.brighter(), color.darker()}
        );
        g2d.setPaint(gradient);
        g2d.fillOval(x, y, BALL_SIZE, BALL_SIZE);

        // Обводка
        g2d.setColor(color.darker().darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x, y, BALL_SIZE, BALL_SIZE);

        // Блик
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.fillOval(x + 4, y + 4, 8, 8);

        // Круг для цифры
        int circleSize = 20;
        int circleX = x + (BALL_SIZE - circleSize) / 2;
        int circleY = y + (BALL_SIZE - circleSize) / 2;

        GradientPaint circleGradient = new GradientPaint(
                circleX, circleY, Color.WHITE,
                circleX + circleSize, circleY + circleSize, new Color(240, 240, 240),
                true
        );
        g2d.setPaint(circleGradient);
        g2d.fillOval(circleX, circleY, circleSize, circleSize);

        g2d.setColor(new Color(180, 180, 180));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawOval(circleX, circleY, circleSize, circleSize);

        // Цифра
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String countStr = String.valueOf(number);
        FontMetrics fm = g2d.getFontMetrics();

        int centerX = x + BALL_SIZE / 2;
        int centerY = y + BALL_SIZE / 2;
        int textX = centerX - fm.stringWidth(countStr) / 2;
        int textY = centerY + (fm.getAscent() - fm.getDescent()) / 2;

        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.drawString(countStr, textX + 1, textY + 1);

        g2d.setColor(Color.BLACK);
        g2d.drawString(countStr, textX, textY);
    }

    private void drawArrow(Graphics2D g2d, int startX, int y, int endX, int endY,
                           boolean isSequential, int ballCount) {
        Color arrowColor = Color.BLACK;
        g2d.setColor(arrowColor);
        g2d.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Основная линия
        g2d.drawLine(startX, y, endX, endY);

        // Наконечник
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(endX, endY);
        arrowHead.addPoint(endX - 10, endY - 5);
        arrowHead.addPoint(endX - 10, endY + 5);
        g2d.fill(arrowHead);

        // Текст "Вытаскиваются"
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.BLACK);
        String drawText = ballCount > 0 ? "Вытаскиваются" : "Процесс вытягивания";
        FontMetrics fm1 = g2d.getFontMetrics();
        int drawTextWidth = fm1.stringWidth(drawText);
        int drawTextX = (startX + endX) / 2 - drawTextWidth / 2;
        int drawTextY = y - 10;
        g2d.drawString(drawText, drawTextX, drawTextY);

        // Текст метода вытягивания
        String methodText = isSequential ? "Последовательно" : "Одновременно";
        FontMetrics fm2 = g2d.getFontMetrics();
        int methodX = (startX + endX) / 2 - fm2.stringWidth(methodText) / 2;
        int methodY = y + 20;
        g2d.drawString(methodText, methodX, methodY);
    }

    private void drawExtractedBalls(Graphics2D g2d, int startX, int centerY,
                                    List<DrawBall> drawBalls, boolean isSequential) {
        if (drawBalls.isEmpty()) {
            return;
        }

        if (isSequential) {
            drawSequentialBalls(g2d, startX, centerY, drawBalls);
        } else {
            drawSimultaneousBalls(g2d, startX, centerY, drawBalls);
        }
    }

    private void drawSequentialBalls(Graphics2D g2d, int startX, int startY, List<DrawBall> drawBalls) {
        int ballX = startX;

        for (int i = 0; i < drawBalls.size(); i++) {
            DrawBall ball = drawBalls.get(i);
            Color ballColor = BALL_COLORS.get(ball.color);
            if (ballColor == null) {
                ballColor = Color.GRAY; // Неизвестный цвет
            }

            // Рисуем шар
            drawBallWithNumber(g2d, ballX, startY, ballColor, 1);

            // Рисуем номер шага под шаром
            drawStepNumber(g2d, ballX, startY, ball.stepNumber);

            ballX += BALL_SIZE + 25;
        }
    }

    private void drawSimultaneousBalls(Graphics2D g2d, int startX, int centerY, List<DrawBall> drawBalls) {
        int topRowBalls = Math.min(drawBalls.size(), 3);
        int bottomRowBalls = Math.max(0, drawBalls.size() - 3);

        int topY = centerY - BALL_SIZE / 2;
        int bottomY = centerY + BALL_SIZE / 2 + 10;

        // Верхний ряд
        int ballX = startX;
        for (int i = 0; i < topRowBalls; i++) {
            DrawBall ball = drawBalls.get(i);
            Color ballColor = BALL_COLORS.get(ball.color);
            if (ballColor == null) {
                ballColor = Color.GRAY;
            }
            drawBallWithNumber(g2d, ballX, topY, ballColor, 1);
            ballX += BALL_SIZE + 15;
        }

        // Нижний ряд
        if (bottomRowBalls > 0) {
            int bottomStartX;
            if (bottomRowBalls == 1) {
                bottomStartX = startX + BALL_SIZE + 8;
            } else if (bottomRowBalls == 2) {
                bottomStartX = startX + BALL_SIZE/2 + 8;
            } else {
                bottomStartX = startX;
            }

            for (int i = 0; i < bottomRowBalls; i++) {
                DrawBall ball = drawBalls.get(i + 3);
                Color ballColor = BALL_COLORS.get(ball.color);
                if (ballColor == null) {
                    ballColor = Color.GRAY;
                }

                int bottomBallX;
                if (bottomRowBalls == 1) {
                    bottomBallX = bottomStartX;
                } else {
                    bottomBallX = bottomStartX + i * (BALL_SIZE + 15);
                }

                drawBallWithNumber(g2d, bottomBallX, bottomY, ballColor, 1);
            }
        }
    }

    private void drawStepNumber(Graphics2D g2d, int ballX, int ballY, int stepNumber) {
        int centerX = ballX + BALL_SIZE / 2;
        int arrowY = ballY + BALL_SIZE + 12;

        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(Color.BLACK);
        String stepText = stepNumber + ".";
        FontMetrics fm = g2d.getFontMetrics();
        int stepTextX = centerX - 15 - fm.stringWidth(stepText);
        g2d.drawString(stepText, stepTextX, arrowY + 4);

        // Маленькая стрелка
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(centerX - 7, arrowY, centerX + 7, arrowY);

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(centerX + 7, arrowY);
        arrowHead.addPoint(centerX + 2, arrowY - 3);
        arrowHead.addPoint(centerX + 2, arrowY + 3);
        g2d.fill(arrowHead);
    }

    // Вспомогательный класс для вытягиваемого шара
    private static class DrawBall {
        String color;
        int stepNumber; // Порядковый номер при последовательном вытягивании

        DrawBall(String color, int stepNumber) {
            this.color = color;
            this.stepNumber = stepNumber;
        }

        @Override
        public String toString() {
            return color + "(" + stepNumber + ")";
        }
    }
}