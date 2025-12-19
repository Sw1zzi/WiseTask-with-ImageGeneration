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
 * Парсер для задач с числами (NUMBERS)
 */
public class NumbersParser {

    private ProblemContext lastContext;

    /**
     * Основной метод парсинга числовой задачи
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

        // 1. TASK NUMBERS "Название"
        cdsl.append("TASK NUMBERS \"").append(extractTaskName(text)).append("\"\n");

        // 2. DIGITS <n>
        int digits = extractDigits(text);
        cdsl.append("DIGITS ").append(digits).append("\n");

        // 3. DISTINCT YES|NO
        boolean distinct = extractDistinctDigits(text);
        cdsl.append("DISTINCT ").append(distinct ? "YES" : "NO").append("\n");

        // 4. ADJACENT_DIFFERENT YES|NO
        boolean adjacentDifferent = extractAdjacentDifferent(text);
        cdsl.append("ADJACENT_DIFFERENT ").append(adjacentDifferent ? "YES" : "NO").append("\n");

        // 5. Порядок: INCREASING | NON_DECREASING | DECREASING | NON_INCREASING
        String order = extractOrder(text);
        if (order != null && !order.isEmpty()) {
            cdsl.append(order).append("\n");
        }

        // 6. Дополнительные условия (состав числа)
        List<String> compositionConditions = extractCompositionConditions(text);
        if (!compositionConditions.isEmpty()) {
            cdsl.append("COMPOSITION [");
            for (int i = 0; i < compositionConditions.size(); i++) {
                if (i > 0) cdsl.append(", ");
                cdsl.append(compositionConditions.get(i));
            }
            cdsl.append("]\n");
        }

        // 7. Делимость
        List<String> divisibilityConditions = extractDivisibilityConditions(text);
        if (!divisibilityConditions.isEmpty()) {
            cdsl.append("DIVISIBILITY [");
            for (int i = 0; i < divisibilityConditions.size(); i++) {
                if (i > 0) cdsl.append(", ");
                cdsl.append(divisibilityConditions.get(i));
            }
            cdsl.append("]\n");
        }

        // 8. Сравнение чисел
        String comparison = extractComparison(text);
        if (comparison != null && !comparison.isEmpty()) {
            cdsl.append(comparison).append("\n");
        }

        // 9. CALCULATE COMBINATIONS
        cdsl.append("CALCULATE COMBINATIONS");

        return cdsl.toString();
    }

    /**
     * Извлекает количество цифр в числе
     */
    private int extractDigits(String text) {
        String lowerText = text.toLowerCase();

        // Паттерны для поиска количества цифр
        Pattern[] patterns = {
                // "n-значных чисел"
                Pattern.compile("(\\d+)-значн[а-я]+"),
                Pattern.compile("(\\d+)-разрядн[а-я]+"),
                Pattern.compile("(\\d+)\\s+значн[а-я]+"),
                Pattern.compile("(\\d+)\\s+разрядн[а-я]+"),

                // "числа из n цифр"
                Pattern.compile("чис[ела]+\\s+из\\s+(\\d+)\\s+цифр"),
                Pattern.compile("из\\s+(\\d+)\\s+цифр"),

                // "n цифр"
                Pattern.compile("(\\d+)\\s+цифр[аы]?\\b"),

                // Специальные случаи: "двузначные", "трехзначные" и т.д.
                Pattern.compile("(двух?|трех?|четырех?|пяти|шести|семи|восьми|девяти|десяти)-?значн[а-я]+"),
                Pattern.compile("(одно|двух?|трех?|четырех?|пяти|шести|семи|восьми|девяти|десяти)-?разрядн[а-я]+")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                try {
                    // Если это числовой паттерн
                    if (pattern.pattern().contains("\\d+")) {
                        return Integer.parseInt(matcher.group(1));
                    }
                    // Если это словесный паттерн
                    else if (pattern.pattern().contains("двух?|трех?")) {
                        String word = matcher.group(1);
                        return convertWordToNumber(word);
                    }
                } catch (NumberFormatException e) {
                    // continue
                }
            }
        }

        // Проверяем словесные обозначения
        if (lowerText.contains("однозначн") || lowerText.contains("1-значн")) return 1;
        if (lowerText.contains("двузначн") || lowerText.contains("2-значн") || lowerText.contains("двухзначн")) return 2;
        if (lowerText.contains("трехзначн") || lowerText.contains("3-значн") || lowerText.contains("трёхзначн")) return 3;
        if (lowerText.contains("четырехзначн") || lowerText.contains("4-значн") || lowerText.contains("четырёхзначн")) return 4;
        if (lowerText.contains("пятизначн") || lowerText.contains("5-значн")) return 5;
        if (lowerText.contains("шестизначн") || lowerText.contains("6-значн")) return 6;
        if (lowerText.contains("семизначн") || lowerText.contains("7-значн")) return 7;
        if (lowerText.contains("восьмизначн") || lowerText.contains("8-значн")) return 8;
        if (lowerText.contains("девятизначн") || lowerText.contains("9-значн")) return 9;
        if (lowerText.contains("десятизначн") || lowerText.contains("10-значн")) return 10;

        // По умолчанию: 3-значные числа (самый частый случай)
        return 3;
    }

    /**
     * Преобразует словесное число в цифровое
     */
    private int convertWordToNumber(String word) {
        switch (word.toLowerCase()) {
            case "одно": case "одн": return 1;
            case "двух": case "дв": case "дву": return 2;
            case "трех": case "трёх": case "тре": case "тр": return 3;
            case "четырех": case "четырёх": case "четыре": case "чет": return 4;
            case "пяти": case "пя": return 5;
            case "шести": case "ше": return 6;
            case "семи": case "се": return 7;
            case "восьми": case "во": return 8;
            case "девяти": case "де": return 9;
            case "десяти": case "дес": return 10;
            default: return 3;
        }
    }

    /**
     * Определяет, должны ли цифры быть различными
     */
    private boolean extractDistinctDigits(String text) {
        String lowerText = text.toLowerCase();

        // Если явно указано, что цифры различны
        if (lowerText.contains("различные цифры") ||
                lowerText.contains("цифры не повторяются") ||
                lowerText.contains("без повторения цифр") ||
                lowerText.contains("все цифры различны") ||
                lowerText.contains("цифры различны")) {
            return true; // YES
        }

        // Если указано, что цифры могут повторяться
        if (lowerText.contains("цифры могут повторяться") ||
                lowerText.contains("цифры повторяются") ||
                lowerText.contains("с повторением цифр")) {
            return false; // NO
        }

        // Если указано "составленные из различных цифр"
        if (lowerText.contains("составленные из различных цифр") ||
                lowerText.contains("составлены из разных цифр")) {
            return true;
        }

        // По умолчанию: цифры могут повторяться
        return false;
    }

    /**
     * Определяет, должны ли соседние цифры быть разными
     */
    private boolean extractAdjacentDifferent(String text) {
        String lowerText = text.toLowerCase();

        // Если указано, что соседние цифры разные
        if (lowerText.contains("соседние цифры различны") ||
                lowerText.contains("никакие две соседние цифры не одинаковы") ||
                lowerText.contains("соседние цифры не совпадают") ||
                lowerText.contains("цифры в соседних разрядах различны")) {
            return true; // YES
        }

        // Если указано, что цифры идут подряд одинаковые
        if (lowerText.contains("одинаковые цифры рядом") ||
                lowerText.contains("цифры могут повторяться подряд") ||
                lowerText.contains("соседние цифры могут совпадать")) {
            return false; // NO
        }

        // По умолчанию: соседние цифры могут быть одинаковыми
        return false;
    }

    /**
     * Извлекает порядок цифр
     */
    private String extractOrder(String text) {
        String lowerText = text.toLowerCase();

        // 1. СТРОГО ВОЗРАСТАЮЩИЙ ПОРЯДОК
        if (lowerText.contains("возрастающ") &&
                (lowerText.contains("строг") ||
                        lowerText.contains("каждая следующая больше предыдущей") ||
                        lowerText.contains("цифры идут в порядке возрастания"))) {
            return "INCREASING";
        }

        // 2. НЕУБЫВАЮЩИЙ ПОРЯДОК
        if ((lowerText.contains("неубывающ") ||
                lowerText.contains("не убывающ")) &&
                lowerText.contains("порядок")) {
            return "NON_DECREASING";
        }

        // 3. СТРОГО УБЫВАЮЩИЙ ПОРЯДОК
        if (lowerText.contains("убывающ") &&
                (lowerText.contains("строг") ||
                        lowerText.contains("каждая следующая меньше предыдущей") ||
                        lowerText.contains("цифры идут в порядке убывания"))) {
            return "DECREASING";
        }

        // 4. НЕВОЗРАСТАЮЩИЙ ПОРЯДОК
        if ((lowerText.contains("невозрастающ") ||
                lowerText.contains("не возрастающ")) &&
                lowerText.contains("порядок")) {
            return "NON_INCREASING";
        }

        // 5. ПРОИЗВОЛЬНЫЙ ПОРЯДОК (явно указан)
        if (lowerText.contains("произвольный порядок") ||
                lowerText.contains("любой порядок") ||
                lowerText.contains("порядок произвольный")) {
            return ""; // Нет специального порядка
        }

        return null;
    }

    /**
     * Извлекает условия на состав числа
     */
    private List<String> extractCompositionConditions(String text) {
        List<String> conditions = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // 1. СУММА ЦИФР
        Pattern sumPattern = Pattern.compile("сумм[ауе]\\s+цифр\\s+(равна|больше|меньше)\\s+(\\d+)");
        Matcher sumMatcher = sumPattern.matcher(lowerText);
        if (sumMatcher.find()) {
            String operator = convertOperator(sumMatcher.group(1));
            String value = sumMatcher.group(2);
            conditions.add("SUM_DIGITS " + operator + " " + value);
        }

        // 2. ПРОИЗВЕДЕНИЕ ЦИФР
        Pattern productPattern = Pattern.compile("произведени[ея]\\s+цифр\\s+(равно|больше|меньше)\\s+(\\d+)");
        Matcher productMatcher = productPattern.matcher(lowerText);
        if (productMatcher.find()) {
            String operator = convertOperator(productMatcher.group(1));
            String value = productMatcher.group(2);
            conditions.add("PRODUCT_DIGITS " + operator + " " + value);
        }

        // 3. ЧЕТНОСТЬ/НЕЧЕТНОСТЬ
        if (lowerText.contains("четн") && !lowerText.contains("нечетн")) {
            conditions.add("EVEN");
        }
        if (lowerText.contains("нечетн")) {
            conditions.add("ODD");
        }

        // 4. ПЕРВАЯ ЦИФРА
        Pattern firstDigitPattern = Pattern.compile("перв[аяой]+\\s+цифр[аы]?\\s+(равна|больше|меньше)\\s+(\\d+)");
        Matcher firstDigitMatcher = firstDigitPattern.matcher(lowerText);
        if (firstDigitMatcher.find()) {
            String operator = convertOperator(firstDigitMatcher.group(1));
            String value = firstDigitMatcher.group(2);
            conditions.add("FIRST_DIGIT " + operator + " " + value);
        }

        // 5. ПОСЛЕДНЯЯ ЦИФРА
        Pattern lastDigitPattern = Pattern.compile("последн[яей]+\\s+цифр[аы]?\\s+(равна|больше|меньше)\\s+(\\d+)");
        Matcher lastDigitMatcher = lastDigitPattern.matcher(lowerText);
        if (lastDigitMatcher.find()) {
            String operator = convertOperator(lastDigitMatcher.group(1));
            String value = lastDigitMatcher.group(2);
            conditions.add("LAST_DIGIT " + operator + " " + value);
        }

        // 6. ЧЕТНЫЕ ЦИФРЫ БОЛЬШЕ ЧЕМ НЕЧЕТНЫЕ
        if (lowerText.contains("четных цифр больше чем нечетных")) {
            conditions.add("EVEN_DIGITS_MORE_THAN_ODD");
        }

        // 7. СОДЕРЖИТ ОПРЕДЕЛЕННУЮ ЦИФРУ
        Pattern containsDigitPattern = Pattern.compile("содержит цифру\\s+(\\d)");
        Matcher containsDigitMatcher = containsDigitPattern.matcher(lowerText);
        while (containsDigitMatcher.find()) {
            String digit = containsDigitMatcher.group(1);
            conditions.add("CONTAINS_DIGIT " + digit);
        }

        return conditions;
    }

    /**
     * Преобразует оператор из русского в английский
     */
    private String convertOperator(String russianOperator) {
        switch (russianOperator.toLowerCase()) {
            case "равна": case "равно": return "=";
            case "больше": return ">";
            case "меньше": return "<";
            default: return "=";
        }
    }

    /**
     * Извлекает условия делимости
     */
    private List<String> extractDivisibilityConditions(String text) {
        List<String> conditions = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // Паттерны для делимости
        Pattern[] patterns = {
                Pattern.compile("делит[ся]? на (\\d+)"),
                Pattern.compile("кратн[оы]? (\\d+)"),
                Pattern.compile("делится без остатка на (\\d+)"),
                Pattern.compile("остаток от деления на (\\d+) равен (\\d+)")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(lowerText);
            while (matcher.find()) {
                if (matcher.groupCount() >= 1) {
                    String divisor = matcher.group(1);
                    if (pattern.pattern().contains("остаток") && matcher.groupCount() >= 2) {
                        String remainder = matcher.group(2);
                        conditions.add("DIVISIBLE_BY " + divisor + " WITH_REMAINDER " + remainder);
                    } else {
                        conditions.add("DIVISIBLE_BY " + divisor);
                    }
                }
            }
        }

        return conditions;
    }

    /**
     * Извлекает сравнение чисел
     */
    private String extractComparison(String text) {
        String lowerText = text.toLowerCase();

        // Сравнение двух чисел
        if (lowerText.contains("больше чем") || lowerText.contains("меньше чем")) {
            Pattern comparisonPattern = Pattern.compile(
                    "число[а]?\\s+([^,\\.]+)\\s+(больше|меньше)\\s+чем\\s+([^,\\.]+)");
            Matcher comparisonMatcher = comparisonPattern.matcher(lowerText);

            if (comparisonMatcher.find()) {
                String left = comparisonMatcher.group(1).trim();
                String operator = comparisonMatcher.group(2);
                String right = comparisonMatcher.group(3).trim();

                // Преобразуем оператор
                String op = operator.equals("больше") ? ">" : "<";

                return "COMPARE " + left + " " + op + " " + right;
            }
        }

        return null;
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
            result += " (числовая задача)";
        }

        return result;
    }

    /**
     * Обогащает контекст данными из текста
     */
    private void enrichContextFromText(ProblemContext context, String text, String taskTitle) {
        if (context == null) return;

        // Устанавливаем тип задачи
        context.setProblemType(ProblemType.NUMBERS);
        context.setTaskName(taskTitle);

        // Устанавливаем параметры чисел
        int digits = extractDigits(text);
        context.setDigits(digits);

        boolean distinct = extractDistinctDigits(text);
        context.setDistinctDigits(distinct);

        boolean adjacentDifferent = extractAdjacentDifferent(text);
        context.setAdjacentDifferent(adjacentDifferent);

        // Устанавливаем порядок
        String order = extractOrder(text);
        if (order != null) {
            context.setOrder(order);
        }

        // Устанавливаем расчет
        context.setCalculationType("COMBINATIONS");

        // Добавляем условия
        List<String> compositionConditions = extractCompositionConditions(text);
        for (String condition : compositionConditions) {
            context.addGeneralCondition(condition);
        }

        List<String> divisibilityConditions = extractDivisibilityConditions(text);
        for (String condition : divisibilityConditions) {
            context.addDivisibilityCondition(condition);
        }

        // Обрабатываем сравнение
        String comparison = extractComparison(text);
        if (comparison != null) {
            // Парсим сравнение
            parseComparison(comparison, context);
        }

        // Сохраняем исходный текст
        context.setParameter("originalText", text);

        // Устанавливаем максимальную цифру (по умолчанию 9)
        context.setMaxDigit(9);

        // Первая цифра не ноль (по умолчанию для многозначных чисел)
        context.setFirstNotZero(digits > 1);
    }

    /**
     * Парсит сравнение и устанавливает в контекст
     */
    private void parseComparison(String comparison, ProblemContext context) {
        // Пример: "COMPARE сумма_цифр > 10"
        if (comparison.startsWith("COMPARE")) {
            String[] parts = comparison.split("\\s+", 4);
            if (parts.length >= 4) {
                String left = parts[1];
                String operator = parts[2];
                String right = parts[3];

                // Устанавливаем сравнение
                List<String> leftList = new ArrayList<>();
                leftList.add(left);
                context.setCompareLeft(leftList);

                List<String> rightList = new ArrayList<>();
                rightList.add(right);
                context.setCompareRight(rightList);

                context.setCompareOperator(operator);
            }
        }
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