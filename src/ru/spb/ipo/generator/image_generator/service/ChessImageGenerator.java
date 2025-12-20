package ru.spb.ipo.generator.image_generator.service;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ChessImageGenerator {
    private Map<String, Image> imageCache = new HashMap<>();
    private Random random = new Random();

    private static final int IMAGE_WIDTH = 500;
    private static final int IMAGE_HEIGHT = 110;
    private static final int BOARD_SIZE = 74;
    private static final int PIECE_IMAGE_SIZE = 47;
    private static final int CELL_SIZE = BOARD_SIZE / 8;

    public ChessImageGenerator() {
        // Пустой конструктор
    }

    private Image loadImage(String filename) {
        // Проверяем кэш
        if (imageCache.containsKey(filename)) {
            return imageCache.get(filename);
        }

        // Пробуем несколько путей, как в CardsImageGenerator
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
                // Игнорируем и пробуем следующий путь
            }
        }

        // Если не нашли, пробуем из ресурсов
        try {
            // Для пути chess/... или /imgs/chess/...
            String resourcePath;
            if (filename.startsWith("chess/")) {
                resourcePath = "/imgs/" + filename;
            } else {
                resourcePath = "/imgs/" + filename;
            }

            Image image = ImageIO.read(getClass().getResourceAsStream(resourcePath));
            if (image != null) {
                imageCache.put(filename, image);
                return image;
            }
        } catch (Exception e) {
            // Игнорируем
        }

        System.err.println("Не удалось загрузить изображение: " + filename);
        return null;
    }

    private Image loadChessPieceImage(String pieceType, boolean isWhite) {
        // Пробуем несколько вариантов имен файлов
        String[] possibleFilenames = {
                // В папке chess
                "chess/" + pieceType.toLowerCase() + "_black.png",
                "chess/" + pieceType.toLowerCase() + "_white.png",
                // В корне
                pieceType.toLowerCase() + "_black.png",
                pieceType.toLowerCase() + "_white.png",
                // Без цвета
                "chess/" + pieceType.toLowerCase() + ".png",
                pieceType.toLowerCase() + ".png"
        };

        // Пробуем сначала черные (как в CardsImageGenerator)
        for (String filename : possibleFilenames) {
            Image image = loadImage(filename);
            if (image != null) {
                return image;
            }
        }

        // Если не нашли, создаем простую фигуру
        return createSimplePieceImage(pieceType);
    }

    private Image createSimplePieceImage(String pieceType) {
        BufferedImage image = new BufferedImage(PIECE_IMAGE_SIZE, PIECE_IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Фон - прозрачный
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, PIECE_IMAGE_SIZE, PIECE_IMAGE_SIZE);
        g2d.setComposite(AlphaComposite.SrcOver);

        // Цвет фигуры
        Color[] colors = {Color.BLACK, Color.DARK_GRAY, Color.GRAY};
        Color pieceColor = colors[random.nextInt(colors.length)];

        // Рисуем круг
        g2d.setColor(pieceColor);
        g2d.fillOval(5, 5, PIECE_IMAGE_SIZE - 10, PIECE_IMAGE_SIZE - 10);

        // Обводка
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(5, 5, PIECE_IMAGE_SIZE - 10, PIECE_IMAGE_SIZE - 10);

        // Буква фигуры
        String initial = getPieceInitial(pieceType);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = PIECE_IMAGE_SIZE / 2 - fm.stringWidth(initial) / 2;
        int textY = PIECE_IMAGE_SIZE / 2 + fm.getAscent() / 2 - 5;

        g2d.setColor(Color.WHITE);
        g2d.drawString(initial, textX, textY);

        g2d.dispose();
        return image;
    }

    private String getPieceInitial(String pieceType) {
        pieceType = pieceType.toUpperCase();
        if (pieceType.contains("ROOK") || pieceType.contains("ЛАДЬЯ")) return "R";
        if (pieceType.contains("KNIGHT") || pieceType.contains("КОНЬ")) return "N";
        if (pieceType.contains("BISHOP") || pieceType.contains("СЛОН")) return "B";
        if (pieceType.contains("QUEEN") || pieceType.contains("ФЕРЗЬ")) return "Q";
        if (pieceType.contains("KING") || pieceType.contains("КОРОЛЬ")) return "K";
        if (pieceType.contains("PAWN") || pieceType.contains("ПЕШКА")) return "P";
        return pieceType.substring(0, 1);
    }

    public BufferedImage generateImage(ProblemContext context) {
        System.out.println("\n=== CHESS IMAGE GENERATOR ===");
        System.out.println("Генерация изображения для шахматной задачи");

        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Мягкий фон
        drawBackground(g2d);

        // Получаем данные
        int boardHeight = context.getBoardHeight();
        int boardWidth = context.getBoardWidth();
        Map<String, Integer> pieces = context.getPieces();
        boolean attacking = context.isAttacking();

        System.out.println("Размер доски: " + boardWidth + "x" + boardHeight);
        System.out.println("Фигуры: " + pieces);
        System.out.println("Атакующие: " + attacking);

        // Если размеры не указаны, используем стандартные 8x8
        if (boardHeight <= 0) boardHeight = 8;
        if (boardWidth <= 0) boardWidth = 8;

        // Рисуем визуализацию
        drawChessVisualization(g2d, boardHeight, boardWidth, pieces, attacking);

        g2d.dispose();
        return image;
    }

    private void drawBackground(Graphics2D g2d) {
        // Используем шахматный паттерн
        BackgroundGenerator.drawBackground(g2d, IMAGE_WIDTH, IMAGE_HEIGHT,
                BackgroundGenerator.Style.CHESS);
    }

    private void drawChessVisualization(Graphics2D g2d, int boardHeight, int boardWidth,
                                        Map<String, Integer> pieces, boolean attacking) {
        // Левая часть: шахматная доска
        drawLeftPanel(g2d, boardHeight, boardWidth);

        // Правая часть: фигуры и информация
        drawRightPanel(g2d, pieces, attacking);
    }

    private void drawLeftPanel(Graphics2D g2d, int boardHeight, int boardWidth) {
        // Позиция доски в левой части
        int boardY = (IMAGE_HEIGHT - BOARD_SIZE) / 2 + 5;
        int boardX = 30;

        System.out.println("Пробую загрузить изображение доски...");

        // Сначала пробуем загрузить изображение доски
        Image boardImage = loadImage("chess_desk.png");

        if (boardImage == null) {
            // Пробуем другие варианты
            boardImage = loadImage("chess_board.png");
        }

        if (boardImage == null) {
            boardImage = loadImage("board.png");
        }

        if (boardImage != null) {
            System.out.println("Изображение доски найдено");
            g2d.drawImage(boardImage, boardX, boardY, BOARD_SIZE, BOARD_SIZE, null);
        } else {
            System.out.println("Изображение доски не найдено, рисую простую доску");
            drawSimpleBoard(g2d, boardX, boardY, boardWidth, boardHeight);
        }

        // Рисуем вертикальную стрелку с числом высоты слева
        drawVerticalArrowWithNumber(g2d, boardX, boardY, boardHeight);

        // Рисуем горизонтальную стрелку с числом ширины сверху
        drawHorizontalArrowWithNumber(g2d, boardX, boardY, boardWidth);
    }

    private void drawSimpleBoard(Graphics2D g2d, int x, int y, int width, int height) {
        // Цвета шахматной доски
        Color lightColor = new Color(240, 217, 181);
        Color darkColor = new Color(181, 136, 99);

        // Рисуем клетки
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Color cellColor = ((row + col) % 2 == 0) ? lightColor : darkColor;
                g2d.setColor(cellColor);

                int cellX = x + col * (BOARD_SIZE / 8);
                int cellY = y + row * (BOARD_SIZE / 8);
                g2d.fillRect(cellX, cellY, BOARD_SIZE / 8, BOARD_SIZE / 8);
            }
        }

        // Рамка доски
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, BOARD_SIZE, BOARD_SIZE);
    }

    private void drawVerticalArrowWithNumber(Graphics2D g2d, int boardX, int boardY, int boardHeight) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        int arrowX = boardX - 5;
        int arrowTopY = boardY + 3;
        int arrowBottomY = boardY + BOARD_SIZE - 3;
        int arrowLength = arrowBottomY - arrowTopY;

        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(arrowX, arrowTopY, arrowX, arrowBottomY);

        drawVerticalArrowHead(g2d, arrowX, arrowTopY, true);
        drawVerticalArrowHead(g2d, arrowX, arrowBottomY, false);

        String heightText = boardHeight + "";
        FontMetrics fm = g2d.getFontMetrics();

        int numberX = arrowX - fm.stringWidth(heightText) - 3;
        int numberY = arrowTopY + arrowLength / 2 + fm.getAscent() / 2 - 2;

        g2d.drawString(heightText, numberX, numberY);
    }

    private void drawHorizontalArrowWithNumber(Graphics2D g2d, int boardX, int boardY, int boardWidth) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        int arrowY = boardY - 5;
        int arrowLeftX = boardX + 3;
        int arrowRightX = boardX + BOARD_SIZE - 3;
        int arrowLength = arrowRightX - arrowLeftX;

        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(arrowLeftX, arrowY, arrowRightX, arrowY);

        drawHorizontalArrowHead(g2d, arrowLeftX, arrowY, true);
        drawHorizontalArrowHead(g2d, arrowRightX, arrowY, false);

        String widthText = boardWidth + "";
        FontMetrics fm = g2d.getFontMetrics();

        int numberX = arrowLeftX + arrowLength / 2 - fm.stringWidth(widthText) / 2;
        int numberY = arrowY - 5;

        g2d.drawString(widthText, numberX, numberY);
    }

    private void drawRightPanel(Graphics2D g2d, Map<String, Integer> pieces, boolean attacking) {
        int rightPanelStartX = 160;
        int rightPanelWidth = IMAGE_WIDTH - rightPanelStartX - 10;

        // Если нет фигур, рисуем ТОЛЬКО сообщение об отсутствии фигур
        if (pieces == null || pieces.isEmpty()) {
            drawNoPiecesMessage(g2d, rightPanelStartX, rightPanelWidth);
            return; // ВЫХОДИМ РАНЬШЕ, не рисуем про атакующие/неатакующие
        }

        // Заголовок "Фигуры:" по центру правой панели
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        String title = "Фигуры:";
        FontMetrics titleFm = g2d.getFontMetrics();
        int titleX = rightPanelStartX + rightPanelWidth / 2 - titleFm.stringWidth(title) / 2;
        int titleY = 15;
        g2d.drawString(title, titleX, titleY);

        // Преобразуем фигуры в список и перемешиваем для случайного порядка
        List<Map.Entry<String, Integer>> pieceList = new ArrayList<>(pieces.entrySet());
        Collections.shuffle(pieceList, random);

        int pieceSpacing = 50;
        int totalPiecesWidth = pieceList.size() * pieceSpacing - 10;
        int piecesStartX = rightPanelStartX + rightPanelWidth / 2 - totalPiecesWidth / 2;
        int piecesStartY = titleY + 10;

        // Шрифт для количества
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics countFm = g2d.getFontMetrics();

        // Рисуем все фигуры в случайном порядке
        for (int i = 0; i < pieceList.size(); i++) {
            Map.Entry<String, Integer> entry = pieceList.get(i);
            String pieceType = entry.getKey();
            int count = entry.getValue();

            System.out.println("Загрузка фигуры: " + pieceType);

            // Позиция фигуры
            int pieceX = piecesStartX + i * pieceSpacing;
            int pieceY = piecesStartY;

            // Загружаем изображение фигуры
            Image pieceImage = loadChessPieceImage(pieceType, true);

            // Рисуем фигуру
            if (pieceImage != null) {
                System.out.println("Изображение фигуры " + pieceType + " найдено");
                g2d.drawImage(pieceImage, pieceX, pieceY,
                        PIECE_IMAGE_SIZE, PIECE_IMAGE_SIZE, null);
            } else {
                System.out.println("Изображение фигуры " + pieceType + " не найдено, рисую простую версию");
                // Рисуем простую фигуру
                Color[] colors = {Color.RED, Color.BLUE, Color.GREEN,
                        Color.MAGENTA, Color.ORANGE, Color.CYAN};
                Color pieceColor = colors[i % colors.length];

                // Кружок
                g2d.setColor(pieceColor);
                g2d.fillOval(pieceX, pieceY, PIECE_IMAGE_SIZE, PIECE_IMAGE_SIZE);

                // Обводка
                g2d.setColor(Color.BLACK);
                g2d.drawOval(pieceX, pieceY, PIECE_IMAGE_SIZE, PIECE_IMAGE_SIZE);

                // Буква фигуры
                String initial = getPieceInitial(pieceType);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = pieceX + PIECE_IMAGE_SIZE/2 - fm.stringWidth(initial)/2;
                int textY = pieceY + PIECE_IMAGE_SIZE/2 + fm.getAscent()/2 - 3;
                g2d.setColor(Color.WHITE);
                g2d.drawString(initial, textX, textY);
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
            }

            // Количество под фигурой
            String countText = "×" + count;
            int countTextWidth = countFm.stringWidth(countText);
            int countX = pieceX + PIECE_IMAGE_SIZE/2 - countTextWidth/2;
            int countY = pieceY + PIECE_IMAGE_SIZE + 12;

            // Фон для количества
            g2d.setColor(new Color(255, 255, 255, 220));
            g2d.fillRoundRect(countX - 3, countY - countFm.getAscent() + 2,
                    countTextWidth + 6, countFm.getHeight() - 2, 4, 4);

            // Текст количества
            g2d.setColor(Color.BLACK);
            g2d.drawString(countText, countX, countY);
        }

        // Рисуем надпись о типе размещения ТОЛЬКО если есть фигуры
        int attackTextY = piecesStartY + PIECE_IMAGE_SIZE + 30;
        drawAttackTypeCentered(g2d, rightPanelStartX, rightPanelWidth, attacking, attackTextY);
    }

    private void drawNoPiecesMessage(Graphics2D g2d, int panelStartX, int panelWidth) {
        // Сообщение об отсутствии фигур (больше и по центру)
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.RED);

        String noPiecesText = "Фигуры не указаны";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = panelStartX + panelWidth / 2 - fm.stringWidth(noPiecesText) / 2;
        int textY = IMAGE_HEIGHT / 2;

        // Фон для сообщения
        g2d.setColor(new Color(255, 230, 230, 220));
        int padding = 10;
        g2d.fillRoundRect(textX - padding, textY - fm.getAscent() - padding/2,
                fm.stringWidth(noPiecesText) + padding*2, fm.getHeight() + padding,
                10, 10);

        // Текст
        g2d.setColor(Color.RED);
        g2d.drawString(noPiecesText, textX, textY);

        // Подсказка под сообщением
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(Color.DARK_GRAY);

        String hintText = "В условии задачи не указаны фигуры";
        FontMetrics fm2 = g2d.getFontMetrics();
        int hintX = panelStartX + panelWidth / 2 - fm2.stringWidth(hintText) / 2;
        int hintY = textY + 25;

        g2d.drawString(hintText, hintX, hintY);
    }

    private void drawAttackTypeCentered(Graphics2D g2d, int panelStartX, int panelWidth, boolean attacking) {
        int centerY = IMAGE_HEIGHT / 2;
        drawAttackTypeCentered(g2d, panelStartX, panelWidth, attacking, centerY);
    }

    private void drawAttackTypeCentered(Graphics2D g2d, int panelStartX, int panelWidth,
                                        boolean attacking, int yPos) {
        String attackText = attacking ? "Атакующие фигуры" : "Неатакующие фигуры";
        Color bgColor = attacking ? new Color(255, 200, 200, 220) : new Color(200, 255, 200, 220);

        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();

        int textWidth = fm.stringWidth(attackText);
        int textX = panelStartX + panelWidth / 2 - textWidth / 2;
        int textY = yPos;

        // Фон для текста
        g2d.setColor(bgColor);
        g2d.fillRoundRect(textX - 8, textY - fm.getAscent() - 3,
                textWidth + 16, fm.getHeight() + 4, 8, 8);

        // Текст
        g2d.setColor(Color.BLACK);
        g2d.drawString(attackText, textX, textY);
    }

    private void drawVerticalArrowHead(Graphics2D g2d, int x, int y, boolean pointingUp) {
        int arrowSize = 5;
        Polygon arrowHead = new Polygon();

        if (pointingUp) {
            arrowHead.addPoint(x, y - arrowSize);
            arrowHead.addPoint(x + arrowSize, y);
            arrowHead.addPoint(x - arrowSize, y);
        } else {
            arrowHead.addPoint(x, y + arrowSize);
            arrowHead.addPoint(x + arrowSize, y);
            arrowHead.addPoint(x - arrowSize, y);
        }

        g2d.fill(arrowHead);
    }

    private void drawHorizontalArrowHead(Graphics2D g2d, int x, int y, boolean pointingLeft) {
        int arrowSize = 5;
        Polygon arrowHead = new Polygon();

        if (pointingLeft) {
            arrowHead.addPoint(x - arrowSize, y);
            arrowHead.addPoint(x, y + arrowSize);
            arrowHead.addPoint(x, y - arrowSize);
        } else {
            arrowHead.addPoint(x + arrowSize, y);
            arrowHead.addPoint(x, y + arrowSize);
            arrowHead.addPoint(x, y - arrowSize);
        }

        g2d.fill(arrowHead);
    }
}