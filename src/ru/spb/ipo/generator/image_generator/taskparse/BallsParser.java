package ru.spb.ipo.generator.image_generator.taskparse;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemInterpreter;
import ru.spb.ipo.generator.image_generator.cdsl.model.ProblemType;
import ru.spb.ipo.generator.image_generator.cdsl.parser.ASTNode;
import ru.spb.ipo.generator.image_generator.cdsl.parser.CDSLParser;
import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.CDSLTokenizer;
import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.Token;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсер для задач на шары и урны (BALLS_AND_URNS)
 */
public class BallsParser {

    private ProblemContext lastContext;

    /**
     * Основной метод парсинга задачи на шары и урны
     */
    public String parse(String taskTitle, String taskText) {
        try {
            // 1. Анализируем текст задачи и преобразуем в CDSL
            String cdslCode = analyzeAndConvertToCDSL(taskText);

            // 2. Токенизация и парсинг CDSL кода
            List<Token> tokens = CDSLTokenizer.tokenize(cdslCode);

            // 3. Парсинг в AST
            CDSLParser parser = new CDSLParser(tokens);
            ASTNode ast = parser.parse();

            // 4. Интерпретация AST
            ProblemContext context = ProblemInterpreter.interpret(ast);

            // 5. Дополнительно заполняем контекст
            enrichContextFromText(context, taskText, taskTitle);

            this.lastContext = context;
            return cdslCode;

        } catch (Exception e) {
            e.printStackTrace();
            // Если CDSL парсинг не сработал, создаем контекст напрямую
            return createContextDirectly(taskTitle, taskText);
        }
    }

    /**
     * Прямой метод для получения контекста
     */
    public ProblemContext getContext() {
        return lastContext;
    }

    /**
     * Анализирует текст задачи и преобразует в CDSL синтаксис
     */
    private String analyzeAndConvertToCDSL(String text) {
        StringBuilder cdsl = new StringBuilder();

        // 1. TASK BALLS "Название"
        cdsl.append("TASK BALLS \"").append(extractTaskName(text)).append("\"\n");

        // 2. URN ["<цвет>" <количество>, ...]
        Map<String, Integer> urnContents = extractUrnContents(text);
        if (!urnContents.isEmpty()) {
            cdsl.append("URN [");
            boolean first = true;
            for (Map.Entry<String, Integer> entry : urnContents.entrySet()) {
                if (!first) cdsl.append(", ");
                cdsl.append("\"").append(entry.getKey()).append("\" ").append(entry.getValue());
                first = false;
            }
            cdsl.append("]\n");
        }

        // 3. DRAW_SEQUENTIAL | DRAW_SIMULTANEOUS
        String drawType = extractDrawType(text);
        cdsl.append(drawType).append("\n");

        // 4. DRAW_COUNT <n>
        int drawCount = extractDrawCount(text);
        cdsl.append("DRAW_COUNT ").append(drawCount).append("\n");

        // 5. WITH_REPLACEMENT | WITHOUT_REPLACEMENT
        boolean withReplacement = extractReplacementType(text);
        cdsl.append(withReplacement ? "WITH_REPLACEMENT\n" : "WITHOUT_REPLACEMENT\n");

        // 6. TARGET ["<цвет>" <количество>, ...]
        Map<String, Integer> targetBalls = extractTargetBalls(text);
        if (!targetBalls.isEmpty()) {
            cdsl.append("TARGET [");
            boolean first = true;
            for (Map.Entry<String, Integer> entry : targetBalls.entrySet()) {
                if (!first) cdsl.append(", ");
                cdsl.append("\"").append(entry.getKey()).append("\" ").append(entry.getValue());
                first = false;
            }
            cdsl.append("]\n");
        }

        // 7. Условия на количество шаров каждого цвета
        List<String> countConditions = extractCountConditions(text);
        if (!countConditions.isEmpty()) {
            cdsl.append("CONDITIONS [");
            for (int i = 0; i < countConditions.size(); i++) {
                if (i > 0) cdsl.append(", ");
                cdsl.append(countConditions.get(i));
            }
            cdsl.append("]\n");
        }

        // 8. CALCULATE PROBABILITY или COMBINATIONS
        String calculateType = determineCalculateType(text);
        cdsl.append("CALCULATE ").append(calculateType);

        return cdsl.toString();
    }

    /**
     * Извлекает содержимое урны
     */
    private Map<String, Integer> extractUrnContents(String text) {
        Map<String, Integer> urnContents = new LinkedHashMap<>();
        String lowerText = text.toLowerCase();

        // Паттерны для поиска шаров в урне
        Pattern[] patterns = {
                // "1 белый, 2 черных, 3 красных шара"
                Pattern.compile("(\\d+)\\s+([а-яё]+)\\s+шар[аову]?", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(\\d+)\\s+([а-яё]+)\\s+шаров?", Pattern.CASE_INSENSITIVE),
                Pattern.compile("шар[аову]?:?\\s+([а-яё]+)\\s+-\\s+(\\d+)", Pattern.CASE_INSENSITIVE),

                // "белых шаров: 5, черных: 3"
                Pattern.compile("([а-яё]+)\\s+шар[аову]?:?\\s+(\\d+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("([а-яё]+):\\s+(\\d+)", Pattern.CASE_INSENSITIVE),

                // "в урне находятся 3 красных и 5 синих шаров"
                Pattern.compile("находит[ся]+\\s+([^,.]+?\\s+шар[аов]?)", Pattern.CASE_INSENSITIVE)
        };

        // Сначала ищем явные описания шаров
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text); // Используем оригинальный текст
            while (matcher.find()) {
                if (pattern.pattern().contains("(\\d+)\\s+([а-яё]+)")) {
                    try {
                        int count = Integer.parseInt(matcher.group(1));
                        String color = matcher.group(2).trim();
                        urnContents.put(color.toUpperCase(), count);
                    } catch (NumberFormatException e) {
                        // Пропускаем
                    }
                } else if (pattern.pattern().contains("([а-яё]+):\\s+(\\d+)")) {
                    try {
                        String color = matcher.group(1).trim();
                        int count = Integer.parseInt(matcher.group(2));
                        urnContents.put(color.toUpperCase(), count);
                    } catch (NumberFormatException e) {
                        // Пропускаем
                    }
                }
            }
        }

        // Если не нашли явные описания, ищем цвета в тексте
        if (urnContents.isEmpty()) {
            // Стандартные цвета для задач
            String[] standardColors = {"белый", "черный", "красный", "синий", "зеленый", "желтый",
                    "оранжевый", "фиолетовый", "коричневый", "розовый"};

            for (String color : standardColors) {
                if (lowerText.contains(color)) {
                    // Пробуем найти количество для этого цвета
                    Pattern colorPattern = Pattern.compile(color + "\\s+шар[аову]?\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
                    Matcher colorMatcher = colorPattern.matcher(text);

                    int count = 1; // По умолчанию 1
                    if (colorMatcher.find()) {
                        try {
                            count = Integer.parseInt(colorMatcher.group(1));
                        } catch (NumberFormatException e) {
                            // Оставляем 1
                        }
                    } else {
                        // Ищем число перед цветом
                        Pattern beforePattern = Pattern.compile("(\\d+)\\s+" + color, Pattern.CASE_INSENSITIVE);
                        Matcher beforeMatcher = beforePattern.matcher(text);
                        if (beforeMatcher.find()) {
                            try {
                                count = Integer.parseInt(beforeMatcher.group(1));
                            } catch (NumberFormatException e) {
                                // Оставляем 1
                            }
                        }
                    }

                    urnContents.put(color.toUpperCase(), count);
                }
            }
        }

        // Если все еще пусто, добавляем демо-данные
        if (urnContents.isEmpty()) {
            urnContents.put("БЕЛЫЙ", 3);
            urnContents.put("ЧЕРНЫЙ", 5);
            urnContents.put("КРАСНЫЙ", 2);
        }

        return urnContents;
    }

    /**
     * Определяет тип вытягивания
     */
    private String extractDrawType(String text) {
        String lowerText = text.toLowerCase();

        // Одновременное вытягивание
        if (lowerText.contains("одновременно") ||
                lowerText.contains("вместе") ||
                lowerText.contains("за один раз") ||
                lowerText.contains("сразу")) {
            return "DRAW_SIMULTANEOUS";
        }

        // Последовательное вытягивание
        if (lowerText.contains("последовательно") ||
                lowerText.contains("по очереди") ||
                lowerText.contains("по одному") ||
                lowerText.contains("вынимают по")) {
            return "DRAW_SEQUENTIAL";
        }

        // Если есть фраза "вытаскивают n шаров" - обычно одновременно
        Pattern drawPattern = Pattern.compile("вытаскивают\\s+(\\d+)\\s+шар", Pattern.CASE_INSENSITIVE);
        Matcher drawMatcher = drawPattern.matcher(lowerText);
        if (drawMatcher.find()) {
            return "DRAW_SIMULTANEOUS";
        }

        // По умолчанию: одновременное вытягивание
        return "DRAW_SIMULTANEOUS";
    }

    /**
     * Извлекает количество вытягиваемых шаров
     */
    private int extractDrawCount(String text) {
        String lowerText = text.toLowerCase();

        // Паттерны для поиска количества вытягиваемых шаров
        Pattern[] patterns = {
                Pattern.compile("вытаскивают\\s+(\\d+)\\s+шар", Pattern.CASE_INSENSITIVE),
                Pattern.compile("вынимают\\s+(\\d+)\\s+шар", Pattern.CASE_INSENSITIVE),
                Pattern.compile("достают\\s+(\\d+)\\s+шар", Pattern.CASE_INSENSITIVE),
                Pattern.compile("извлекают\\s+(\\d+)\\s+шар", Pattern.CASE_INSENSITIVE),
                Pattern.compile("берут\\s+(\\d+)\\s+шар", Pattern.CASE_INSENSITIVE),

                // "n шаров" в контексте вытягивания
                Pattern.compile("(\\d+)\\s+шар[аов]?\\s+(из|извлекают|вынимают|вытаскивают)", Pattern.CASE_INSENSITIVE),

                // "вытащить n шаров"
                Pattern.compile("вытащить\\s+(\\d+)\\s+шар", Pattern.CASE_INSENSITIVE)
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                try {
                    int count = Integer.parseInt(matcher.group(1));
                    if (count > 0 && count <= 20) { // разумные пределы
                        return count;
                    }
                } catch (NumberFormatException e) {
                    // continue
                }
            }
        }

        // Проверяем целевые шары
        Map<String, Integer> targetBalls = extractTargetBalls(text);
        int totalTarget = targetBalls.values().stream().mapToInt(Integer::intValue).sum();
        if (totalTarget > 0) {
            return totalTarget;
        }

        // По умолчанию: 2 шара
        return 2;
    }

    /**
     * Определяет, с возвращением или без
     */
    private boolean extractReplacementType(String text) {
        String lowerText = text.toLowerCase();

        // С возвращением
        if (lowerText.contains("с возвращением") ||
                lowerText.contains("возвращая обратно") ||
                lowerText.contains("после извлечения возвращают") ||
                lowerText.contains("с возвратом")) {
            return true;
        }

        // Без возвращения (явно)
        if (lowerText.contains("без возвращения") ||
                lowerText.contains("без возврата") ||
                lowerText.contains("не возвращая")) {
            return false;
        }

        // Если "вытаскивают одновременно" или "вынимают n шаров" - обычно без возвращения
        if (lowerText.contains("одновременно") || lowerText.contains("вместе")) {
            return false;
        }

        // Если "последовательно" или "по очереди" - может быть с возвращением
        if (lowerText.contains("последовательно") || lowerText.contains("по очереди")) {
            // Проверяем контекст
            if (lowerText.contains("возвращают") || lowerText.contains("с возвращением")) {
                return true;
            }
            return false; // По умолчанию без возвращения для последовательного
        }

        // По умолчанию: без возвращения
        return false;
    }

    /**
     * Извлекает целевые шары (что нужно вытащить)
     */
    private Map<String, Integer> extractTargetBalls(String text) {
        Map<String, Integer> targetBalls = new LinkedHashMap<>();
        String lowerText = text.toLowerCase();

        // Паттерны для целевых шаров
        Pattern[] patterns = {
                // "вытащить 1 белый и 2 черных шара"
                Pattern.compile("вытащить\\s+([^,.]+?\\s+шар)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("извлечь\\s+([^,.]+?\\s+шар)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("достать\\s+([^,.]+?\\s+шар)", Pattern.CASE_INSENSITIVE),

                // "найти вероятность того, что среди извлеченных будет 1 белый"
                Pattern.compile("будет\\s+([^,.]+?\\s+шар)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("среди\\s+[^\\d]+\\s+будет\\s+([^,.]+)", Pattern.CASE_INSENSITIVE),

                // "ровно n шаров цвета X"
                Pattern.compile("ровно\\s+(\\d+)\\s+([а-яё]+)\\s+шар", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(\\d+)\\s+([а-яё]+)\\s+шар[аов]?\\s+среди", Pattern.CASE_INSENSITIVE)
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text); // Используем оригинальный текст
            while (matcher.find()) {
                String match = matcher.group(1);

                // Парсим количество и цвет
                Pattern countColorPattern = Pattern.compile("(\\d+)\\s+([а-яё]+)", Pattern.CASE_INSENSITIVE);
                Matcher countColorMatcher = countColorPattern.matcher(match);

                while (countColorMatcher.find()) {
                    try {
                        int count = Integer.parseInt(countColorMatcher.group(1));
                        String color = countColorMatcher.group(2).trim();
                        targetBalls.put(color.toUpperCase(), count);
                    } catch (NumberFormatException e) {
                        // Пропускаем
                    }
                }

                // Проверяем, есть ли упоминание цвета без количества
                String[] colors = {"белый", "черный", "красный", "синий", "зеленый", "желтый"};
                for (String color : colors) {
                    if (match.toLowerCase().contains(color) && !targetBalls.containsKey(color.toUpperCase())) {
                        targetBalls.put(color.toUpperCase(), 1); // По умолчанию 1
                    }
                }
            }
        }

        // Если целевые шары не указаны явно, определяем по условиям
        if (targetBalls.isEmpty()) {
            List<String> countConditions = extractCountConditions(text);
            for (String condition : countConditions) {
                // Пример: "WHITE >= 1"
                String[] parts = condition.split("\\s+");
                if (parts.length >= 3) {
                    String color = parts[0];
                    String operator = parts[1];
                    try {
                        int count = Integer.parseInt(parts[2]);
                        targetBalls.put(color, count);
                    } catch (NumberFormatException e) {
                        // Пропускаем
                    }
                }
            }
        }

        return targetBalls;
    }

    /**
     * Извлекает условия на количество шаров
     */
    private List<String> extractCountConditions(String text) {
        List<String> conditions = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // Условия типа "ровно n шаров цвета X"
        if (lowerText.contains("ровно")) {
            Pattern exactlyPattern = Pattern.compile("ровно\\s+(\\d+)\\s+([а-яё]+)\\s+шар", Pattern.CASE_INSENSITIVE);
            Matcher exactlyMatcher = exactlyPattern.matcher(text);
            while (exactlyMatcher.find()) {
                String count = exactlyMatcher.group(1);
                String color = exactlyMatcher.group(2).toUpperCase();
                conditions.add(color + " = " + count);
            }
        }

        // Условия типа "не менее n шаров"
        if (lowerText.contains("не менее") || lowerText.contains("хотя бы")) {
            Pattern atLeastPattern = Pattern.compile("(не менее|хотя бы)\\s+(\\d+)\\s+([а-яё]+)\\s+шар", Pattern.CASE_INSENSITIVE);
            Matcher atLeastMatcher = atLeastPattern.matcher(text);
            while (atLeastMatcher.find()) {
                String count = atLeastMatcher.group(2);
                String color = atLeastMatcher.group(3).toUpperCase();
                conditions.add(color + " >= " + count);
            }
        }

        // Условия типа "не более n шаров"
        if (lowerText.contains("не более") || lowerText.contains("не больше")) {
            Pattern atMostPattern = Pattern.compile("(не более|не больше)\\s+(\\d+)\\s+([а-яё]+)\\s+шар", Pattern.CASE_INSENSITIVE);
            Matcher atMostMatcher = atMostPattern.matcher(text);
            while (atMostMatcher.find()) {
                String count = atMostMatcher.group(2);
                String color = atMostMatcher.group(3).toUpperCase();
                conditions.add(color + " <= " + count);
            }
        }

        // Условия типа "больше n шаров"
        if (lowerText.contains("больше чем") || lowerText.contains("больше")) {
            Pattern greaterPattern = Pattern.compile("больше\\s+(чем\\s+)?(\\d+)\\s+([а-яё]+)\\s+шар", Pattern.CASE_INSENSITIVE);
            Matcher greaterMatcher = greaterPattern.matcher(text);
            while (greaterMatcher.find()) {
                String count = greaterMatcher.group(2);
                String color = greaterMatcher.group(3).toUpperCase();
                conditions.add(color + " > " + count);
            }
        }

        // Условия типа "меньше n шаров"
        if (lowerText.contains("меньше чем") || lowerText.contains("меньше")) {
            Pattern lessPattern = Pattern.compile("меньше\\s+(чем\\s+)?(\\d+)\\s+([а-яё]+)\\s+шар", Pattern.CASE_INSENSITIVE);
            Matcher lessMatcher = lessPattern.matcher(text);
            while (lessMatcher.find()) {
                String count = lessMatcher.group(2);
                String color = lessMatcher.group(3).toUpperCase();
                conditions.add(color + " < " + count);
            }
        }

        // Условия на все шары одного цвета
        if (lowerText.contains("все шары") || lowerText.contains("все одного цвета")) {
            Pattern allPattern = Pattern.compile("все\\s+([а-яё]+)\\s+шар", Pattern.CASE_INSENSITIVE);
            Matcher allMatcher = allPattern.matcher(text);
            if (allMatcher.find()) {
                String color = allMatcher.group(1).toUpperCase();
                conditions.add("ALL_" + color);
            }
        }

        return conditions;
    }

    /**
     * Определяет тип расчета
     */
    private String determineCalculateType(String text) {
        String lowerText = text.toLowerCase();

        if (lowerText.contains("вероятность") ||
                lowerText.contains("вероятност") ||
                lowerText.contains("шанс") ||
                lowerText.contains("какова вероятность") ||
                lowerText.contains("найти вероятность")) {
            return "PROBABILITY";
        }

        if (lowerText.contains("сколько") ||
                lowerText.contains("количество") ||
                lowerText.contains("найти количество") ||
                lowerText.contains("определить количество")) {
            return "COMBINATIONS";
        }

        // Для задач с урнами обычно вероятность
        return "PROBABILITY";
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

        // Если название слишком короткое, добавляем контекст
        if (result.split("\\s+").length < 3) {
            result += " (шары и урны)";
        }

        return result;
    }

    /**
     * Обогащает контекст данными из текста
     */
    private void enrichContextFromText(ProblemContext context, String text, String taskTitle) {
        if (context == null) return;

        // Устанавливаем тип задачи
        context.setProblemType(ProblemType.BALLS_AND_URNS);
        context.setTaskName(taskTitle);

        // Устанавливаем параметры урны
        Map<String, Integer> urnContents = extractUrnContents(text);
        context.setParameter("urnContents", urnContents);

        // Устанавливаем общее количество шаров
        int totalBalls = urnContents.values().stream().mapToInt(Integer::intValue).sum();
        context.setParameter("totalBalls", totalBalls);

        // Устанавливаем тип вытягивания
        String drawType = extractDrawType(text);
        boolean simultaneous = "DRAW_SIMULTANEOUS".equals(drawType);
        context.setParameter("simultaneousDraw", simultaneous);

        // Устанавливаем количество вытягиваемых шаров
        int drawCount = extractDrawCount(text);
        context.setParameter("drawCount", drawCount);

        // Устанавливаем тип возвращения
        boolean withReplacement = extractReplacementType(text);
        context.setParameter("withReplacement", withReplacement);

        // Устанавливаем целевые шары
        Map<String, Integer> targetBalls = extractTargetBalls(text);
        context.setParameter("targetBalls", targetBalls);

        // Устанавливаем расчет
        String calculateType = determineCalculateType(text);
        context.setCalculationType(calculateType);

        // Добавляем условия
        List<String> countConditions = extractCountConditions(text);
        for (String condition : countConditions) {
            context.addGeneralCondition(condition);
        }

        // Сохраняем исходный текст
        context.setParameter("originalText", text);

        // Устанавливаем описание урны
        StringBuilder urnDescription = new StringBuilder();
        for (Map.Entry<String, Integer> entry : urnContents.entrySet()) {
            if (urnDescription.length() > 0) urnDescription.append(", ");
            urnDescription.append(entry.getValue()).append(" ").append(entry.getKey().toLowerCase());
        }
        context.setParameter("urnDescription", urnDescription.toString());
    }

    /**
     * Создает контекст напрямую (если CDSL парсинг не сработал)
     */
    private String createContextDirectly(String taskTitle, String text) {
        try {
            ProblemContext context = new ProblemContext();

            // Заполняем контекст
            enrichContextFromText(context, text, taskTitle);

            // Генерируем CDSL код
            String cdslCode = analyzeAndConvertToCDSL(text);

            this.lastContext = context;
            return cdslCode;

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }
}