package ru.spb.ipo.generator.image_generator.taskparse;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemInterpreter;
import ru.spb.ipo.generator.image_generator.cdsl.parser.ASTNode;
import ru.spb.ipo.generator.image_generator.cdsl.parser.CDSLParser;
import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.CDSLTokenizer;
import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.Token;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсер для шахматных задач
 * Генерирует CDSL код в формате для генератора изображений
 */
public class ChessParser {

    private ProblemContext lastContext;

    /**
     * Основной метод парсинга шахматной задачи
     */
    public String parse(String taskTitle, String taskText) {
        // 1. Анализируем текст задачи и преобразуем в CDSL
        String cdslCode = analyzeAndConvertToCDSL(taskText);

        System.out.println("=== ChessParser CDSL Code ===");
        System.out.println(cdslCode);
        System.out.println("==============================");

        // 2. Токенизация и парсинг CDSL кода
        try {
            List<Token> tokens = CDSLTokenizer.tokenize(cdslCode);

            // 3. Парсинг в AST
            CDSLParser parser = new CDSLParser(tokens);
            ASTNode ast = parser.parse();

            // 4. Интерпретация AST
            ProblemContext context = ProblemInterpreter.interpret(ast);
            this.lastContext = context;

        } catch (Exception e) {
            System.out.println("Ошибка парсинга CDSL: " + e.getMessage());
            e.printStackTrace();

        }

        return cdslCode; // Возвращаем CDSL код
    }

    /**
     * Анализирует текст задачи и преобразует в CDSL синтаксис
     * в формате для генератора изображений
     */
    private String analyzeAndConvertToCDSL(String text) {
        StringBuilder cdsl = new StringBuilder();

        // 1. TASK CHESS "Название задачи"
        cdsl.append("TASK CHESS \"").append(extractTaskName(text)).append("\"\n");

        // 2. BOARD_HEIGHT <высота> и BOARD_WIDTH <ширина>
        int height = extractBoardSize(text, true); // высота
        int width = extractBoardSize(text, false); // ширина

        cdsl.append("BOARD_HEIGHT ").append(height).append("\n");
        cdsl.append("BOARD_WIDTH ").append(width).append("\n");

        // 3. PIECES [<фигура1> <количество>, ...]
        Map<String, Integer> pieces = extractPieces(text);
        cdsl.append("PIECES [");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : pieces.entrySet()) {
            if (!first) cdsl.append(", ");
            cdsl.append(entry.getKey()).append(" ").append(entry.getValue());
            first = false;
        }
        // Если ничего не нашли, добавляем демо-фигуры
        if (pieces.isEmpty()) {
            cdsl.append("CHESS_ROOK 2, CHESS_KNIGHT 1");
        }
        cdsl.append("]\n");

        // 4. ATTACKING | NON_ATTACKING
        String placementType = extractPlacementType(text);
        cdsl.append(placementType);

        return cdsl.toString();
    }

    /**
     * Извлекает размер доски
     * @param isHeight true - ищем высоту, false - ищем ширину
     */
    private int extractBoardSize(String text, boolean isHeight) {
        String lowerText = text.toLowerCase();

        // Ключевые слова для поиска
        String[] heightKeywords = {"высот", "вертикал", "строк", "рядов", "высота"};
        String[] widthKeywords = {"ширин", "горизонтал", "столбцов", "колонок", "ширина"};

        String[] keywords = isHeight ? heightKeywords : widthKeywords;

        // Паттерны для поиска
        Pattern[] patterns = {
                // "доска размером 8×8", "доска 8 на 8"
                Pattern.compile("доск[а-я]*\\s*(?:размером\\s+)?(\\d+)\\s*[×xна\\*]\\s*(\\d+)"),
                // "шахматная доска 8×8"
                Pattern.compile("шахматн[а-я]*\\s+доск[а-я]*\\s+(\\d+)\\s*[×xна\\*]\\s*(\\d+)"),
                // "размер доски 8×8"
                Pattern.compile("размер[а-я]*\\s+доск[а-я]*\\s+(\\d+)\\s*[×xна\\*]\\s*(\\d+)"),
                // "8×8" просто размер
                Pattern.compile("(\\d+)\\s*[×xна\\*]\\s*(\\d+)"),
                // "высотой 8", "шириной 8"
                Pattern.compile("(" + String.join("|", keywords) + ")[а-я]*\\s+(?:в\\s+)?(\\d+)"),
                // "8 клеток в высоту", "8 клеток в ширину"
                Pattern.compile("(\\d+)\\s+клет[а-я]*\\s+(?:в\\s+)(" + String.join("|", keywords) + ")[а-я]*")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                try {
                    // Для паттернов с двумя числами (8×8)
                    if (matcher.groupCount() >= 2) {
                        int num1 = Integer.parseInt(matcher.group(1));
                        int num2 = Integer.parseInt(matcher.group(2));

                        // Определяем какое число высота, а какое ширина
                        if (pattern.pattern().contains("[×xна\\*]")) {
                            // Для формата 8×8 - первое обычно ширина, второе высота
                            // но в шахматах обычно сначала указывают ширину (буквы), потом высоту (цифры)
                            return isHeight ? Math.max(num1, num2) : Math.min(num1, num2);
                        }

                        // Для других паттернов пытаемся определить по контексту
                        String match = matcher.group(0);
                        if (isHeight) {
                            for (String keyword : heightKeywords) {
                                if (match.contains(keyword)) {
                                    return num1;
                                }
                            }
                        } else {
                            for (String keyword : widthKeywords) {
                                if (match.contains(keyword)) {
                                    return num1;
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // continue
                }
            }
        }

        // Если не нашли, возвращаем стандартный размер
        return 8; // Стандартная шахматная доска
    }

    /**
     * Извлекает шахматные фигуры
     */
    private Map<String, Integer> extractPieces(String text) {
        Map<String, Integer> pieces = new LinkedHashMap<>();
        String lowerText = text.toLowerCase();

        // Маппинг русских названий на английские коды
        Map<String, String> pieceMapping = new HashMap<>();
        pieceMapping.put("ладь", "CHESS_ROOK");
        pieceMapping.put("тура", "CHESS_ROOK");
        pieceMapping.put("кон", "CHESS_KNIGHT");
        pieceMapping.put("рыцар", "CHESS_KNIGHT");
        pieceMapping.put("слон", "CHESS_BISHOP");
        pieceMapping.put("офицер", "CHESS_BISHOP");
        pieceMapping.put("ферз", "CHESS_QUEEN");
        pieceMapping.put("королев", "CHESS_QUEEN");
        pieceMapping.put("корол", "CHESS_KING");
        pieceMapping.put("пешк", "CHESS_PAWN");
        pieceMapping.put("солда", "CHESS_PAWN");

        // Проверяем каждую фигуру
        for (Map.Entry<String, String> entry : pieceMapping.entrySet()) {
            String russianPiece = entry.getKey();
            String englishPiece = entry.getValue();

            // Ищем паттерны
            Pattern[] patterns = {
                    // "2 ладьи", "ладей 2", "2 шахматных ладьи"
                    Pattern.compile("(\\d+)\\s+" + russianPiece + "[а-я]*"),
                    Pattern.compile(russianPiece + "[а-я]*\\s+(?:шахматн[а-я]*\\s+)?(\\d+)"),
                    Pattern.compile("(\\d+)\\s+шахматн[а-я]*\\s+" + russianPiece + "[а-я]*"),
                    // "расставить две ладьи", "разместить трех коней"
                    Pattern.compile("(?:расстав|размещ|расстан)[а-я]*\\s+(?:\\w+\\s+)?(\\d+)\\s+" + russianPiece + "[а-я]*")
            };

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(lowerText);
                if (matcher.find()) {
                    try {
                        String numberStr = matcher.group(1);
                        // Преобразуем текстовые числа в цифры
                        int count = parseRussianNumber(numberStr);
                        pieces.put(englishPiece, count);
                        break;
                    } catch (NumberFormatException e) {
                        // Пробуем следующую группу
                        if (matcher.groupCount() > 1) {
                            try {
                                String numberStr = matcher.group(2);
                                int count = parseRussianNumber(numberStr);
                                pieces.put(englishPiece, count);
                                break;
                            } catch (NumberFormatException e2) {
                                // continue
                            }
                        }
                    }
                }
            }
        }

        // Также ищем общие формулировки
        if (pieces.isEmpty()) {
            // "фигур" общее упоминание
            Pattern generalPattern = Pattern.compile("(\\d+)\\s+фигур[а-я]*");
            Matcher generalMatcher = generalPattern.matcher(lowerText);
            if (generalMatcher.find()) {
                try {
                    int totalPieces = Integer.parseInt(generalMatcher.group(1));
                    // Распределяем фигуры
                    if (totalPieces >= 2) {
                        pieces.put("CHESS_ROOK", Math.min(2, totalPieces));
                        if (totalPieces > 2) {
                            pieces.put("CHESS_KNIGHT", Math.min(2, totalPieces - 2));
                        }
                    }
                } catch (NumberFormatException e) {
                    // continue
                }
            }
        }

        return pieces;
    }

    /**
     * Парсит русские числительные
     */
    private int parseRussianNumber(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            // Пробуем распознать русские числительные
            String lower = str.toLowerCase();
            if (lower.contains("один") || lower.contains("одна") || lower.contains("одно")) return 1;
            if (lower.contains("два") || lower.contains("две")) return 2;
            if (lower.contains("три")) return 3;
            if (lower.contains("четыре")) return 4;
            if (lower.contains("пять")) return 5;
            if (lower.contains("шесть")) return 6;
            if (lower.contains("семь")) return 7;
            if (lower.contains("восемь")) return 8;
            if (lower.contains("девять")) return 9;
            if (lower.contains("десять")) return 10;
            return 1; // По умолчанию
        }
    }

    /**
     * Извлекает тип размещения (ATTACKING или NON_ATTACKING)
     */
    private String extractPlacementType(String text) {
        String lowerText = text.toLowerCase();

        // Ключевые слова для NON_ATTACKING
        String[] nonAttackingKeywords = {
                "не атак", "не бьют", "не угрожают", "без атак",
                "так чтобы не били", "не находящихся под боем",
                "не напада", "без угроз"
        };

        // Ключевые слова для ATTACKING
        String[] attackingKeywords = {
                "атак", "бьют", "угрожают", "под боем", "находятся под атакой",
                "так чтобы били", "напада"
        };

        // Сначала проверяем NON_ATTACKING
        for (String keyword : nonAttackingKeywords) {
            if (lowerText.contains(keyword)) {
                return "NON_ATTACKING";
            }
        }

        // Затем проверяем ATTACKING
        for (String keyword : attackingKeywords) {
            if (lowerText.contains(keyword)) {
                return "ATTACKING";
            }
        }

        // По умолчанию - NON_ATTACKING (чаще встречается в задачах)
        return "NON_ATTACKING";
    }

    /**
     * Извлекает название задачи
     */
    private String extractTaskName(String text) {
        // Берем первые 5-7 слов
        String[] words = text.split("\\s+");
        StringBuilder name = new StringBuilder();
        int maxWords = Math.min(7, words.length);

        for (int i = 0; i < maxWords; i++) {
            if (i > 0) name.append(" ");
            name.append(words[i]);
        }

        // Убираем точку в конце если есть
        String result = name.toString().replace("\"", "'");
        if (result.endsWith(".")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * Геттер для последнего контекста
     */
    public ProblemContext getLastContext() {
        return lastContext;
    }
}