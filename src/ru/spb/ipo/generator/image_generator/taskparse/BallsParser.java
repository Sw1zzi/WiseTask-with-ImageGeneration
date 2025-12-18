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
 * Парсер для задач "Шары и урны" (Basket/Urn problems)
 * Генерирует CDSL код в формате для генератора изображений
 */
public class BallsParser {

    private ProblemContext lastContext;

    /**
     * Основной метод парсинга задачи с шарами и урнами
     */
    public String parse(String taskTitle, String taskText) {
        // 1. Анализируем текст задачи и преобразуем в CDSL
        String cdslCode = analyzeAndConvertToCDSL(taskText);

        System.out.println("=== BallsParser CDSL Code ===");
        System.out.println(cdslCode);
        System.out.println("=============================");

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

        // 1. TASK BALLS "Название задачи"
        cdsl.append("TASK BALLS \"").append(extractTaskName(text)).append("\"\n");

        // 2. URN [<цвет1> <количество>, ...]
        Map<String, Integer> urnContents = extractUrnContents(text);
        cdsl.append("URN [");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : urnContents.entrySet()) {
            if (!first) cdsl.append(", ");
            cdsl.append(entry.getKey()).append(" ").append(entry.getValue());
            first = false;
        }
        cdsl.append("]\n");

        // 3. DRAW [<цвет1> <количество>, ...]
        Map<String, Integer> drawContents = extractDrawContents(text);
        cdsl.append("DRAW [");
        first = true;
        for (Map.Entry<String, Integer> entry : drawContents.entrySet()) {
            if (!first) cdsl.append(", ");
            cdsl.append(entry.getKey()).append(" ").append(entry.getValue());
            first = false;
        }
        // Если ничего не нашли, добавляем демо-вытягивание
        if (drawContents.isEmpty()) {
            cdsl.append("RED 1");
            // Если есть синие шары в урне, добавляем и их
            if (urnContents.containsKey("BLUE") && urnContents.get("BLUE") > 0) {
                cdsl.append(", BLUE 1");
            }
        }
        cdsl.append("]\n");

        // 4. SEQUENTIAL | SIMULTANEOUS
        String drawType = extractDrawType(text);
        cdsl.append(drawType);

        return cdsl.toString();
    }

    /**
     * Извлекает содержимое урны
     */
    private Map<String, Integer> extractUrnContents(String text) {
        Map<String, Integer> urnContents = new LinkedHashMap<>();
        String lowerText = text.toLowerCase();

        // Маппинг русских названий на английские коды
        Map<String, String> colorMapping = new HashMap<>();
        colorMapping.put("красн", "RED");
        colorMapping.put("син", "BLUE");
        colorMapping.put("зелен", "GREEN");
        colorMapping.put("бел", "WHITE");
        colorMapping.put("черн", "BLACK");
        colorMapping.put("желт", "YELLOW");

        // Проверяем каждый цвет
        for (Map.Entry<String, String> entry : colorMapping.entrySet()) {
            String russianColor = entry.getKey();
            String englishColor = entry.getValue();

            // Ищем паттерны типа: "5 красных шаров", "красных шаров 5", "5 красных"
            Pattern[] patterns = {
                    Pattern.compile("(\\d+)\\s*" + russianColor + "[а-я]*\\s*шар"),
                    Pattern.compile(russianColor + "[а-я]*\\s*шар[а-я]*\\s*(\\d+)"),
                    Pattern.compile(russianColor + "[а-я]*\\s*(\\d+)"),
                    Pattern.compile("(\\d+)\\s*" + russianColor + "[а-я]*")
            };

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(lowerText);
                if (matcher.find()) {
                    try {
                        int count = Integer.parseInt(matcher.group(1));
                        urnContents.put(englishColor, count);
                        break; // Нашли для этого цвета
                    } catch (NumberFormatException e) {
                        // Продолжаем поиск
                    }
                }
            }
        }

        // Если ничего не нашли, создаем демо-содержимое
        if (urnContents.isEmpty()) {
            urnContents.put("RED", 5);
            urnContents.put("BLUE", 3);
            urnContents.put("GREEN", 2);
        }

        return urnContents;
    }

    /**
     * Извлекает что нужно вытянуть
     */
    private Map<String, Integer> extractDrawContents(String text) {
        Map<String, Integer> drawContents = new LinkedHashMap<>();
        String lowerText = text.toLowerCase();

        // Маппинг цветов
        Map<String, String> colorMapping = new HashMap<>();
        colorMapping.put("красн", "RED");
        colorMapping.put("син", "BLUE");
        colorMapping.put("зелен", "GREEN");
        colorMapping.put("бел", "WHITE");
        colorMapping.put("черн", "BLACK");
        colorMapping.put("желт", "YELLOW");

        // Ключевые слова для вытягивания
        List<String> drawKeywords = Arrays.asList(
                "вытягивают", "вынимают", "извлекают", "достают",
                "нужно получить", "требуется получить", "необходимо извлечь"
        );

        // Ищем текст после ключевых слов вытягивания
        for (String keyword : drawKeywords) {
            int idx = lowerText.indexOf(keyword);
            if (idx != -1) {
                String afterKeyword = lowerText.substring(idx + keyword.length());

                // Проверяем каждый цвет в тексте после ключевого слова
                for (Map.Entry<String, String> entry : colorMapping.entrySet()) {
                    String russianColor = entry.getKey();
                    String englishColor = entry.getValue();

                    // Паттерны для поиска количества
                    Pattern[] patterns = {
                            Pattern.compile("(\\d+)\\s*" + russianColor + "[а-я]*"),
                            Pattern.compile(russianColor + "[а-я]*\\s*(\\d+)"),
                            Pattern.compile(russianColor + "[а-я]*\\s*шар[а-я]*\\s*(\\d+)")
                    };

                    for (Pattern pattern : patterns) {
                        Matcher matcher = pattern.matcher(afterKeyword);
                        if (matcher.find()) {
                            try {
                                int count = Integer.parseInt(matcher.group(1));
                                drawContents.put(englishColor, count);
                                break;
                            } catch (NumberFormatException e) {
                                // continue
                            }
                        }
                    }
                }
            }
        }

        // Если не нашли через ключевые слова, ищем общие условия
        if (drawContents.isEmpty()) {
            // Проверяем условия типа: "ровно 2 красных", "хотя бы один синий"
            Pattern exactPattern = Pattern.compile("ровно\\s+(\\d+)\\s+(" +
                    String.join("|", colorMapping.keySet()) + ")[а-я]*");
            Matcher exactMatcher = exactPattern.matcher(lowerText);
            if (exactMatcher.find()) {
                try {
                    int count = Integer.parseInt(exactMatcher.group(1));
                    String color = findColorCode(exactMatcher.group(2), colorMapping);
                    if (color != null) {
                        drawContents.put(color, count);
                    }
                } catch (NumberFormatException e) {
                    // continue
                }
            }

            // Проверяем "хотя бы один"
            Pattern atLeastPattern = Pattern.compile("хотя\\s+бы\\s+один\\s+(" +
                    String.join("|", colorMapping.keySet()) + ")[а-я]*");
            Matcher atLeastMatcher = atLeastPattern.matcher(lowerText);
            if (atLeastMatcher.find()) {
                String color = findColorCode(atLeastMatcher.group(1), colorMapping);
                if (color != null) {
                    drawContents.put(color, 1);
                }
            }
        }

        return drawContents;
    }

    /**
     * Находит код цвета по русскому названию
     */
    private String findColorCode(String russianColor, Map<String, String> colorMapping) {
        for (Map.Entry<String, String> entry : colorMapping.entrySet()) {
            if (russianColor.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Извлекает тип вытягивания (SEQUENTIAL или SIMULTANEOUS)
     */
    private String extractDrawType(String text) {
        String lowerText = text.toLowerCase();

        if (lowerText.contains("одновременно") ||
                lowerText.contains("вместе") ||
                lowerText.contains("разом") ||
                lowerText.contains("пачкой")) {
            return "SIMULTANEOUS";
        }

        // По умолчанию - последовательное
        return "SEQUENTIAL";
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