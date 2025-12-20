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
 * Парсер для задач на делимость (DIVISIBILITY)
 */
public class DivisibilityParser {

    private ProblemContext lastContext;

    /**
     * Основной метод парсинга задачи на делимость
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

        // 1. TASK DIVISIBILITY "Название"
        cdsl.append("TASK DIVISIBILITY \"").append(extractTaskName(text)).append("\"\n");

        // 2. NUMBER_LENGTH <n>
        int numberLength = extractNumberLength(text);
        cdsl.append("NUMBER_LENGTH ").append(numberLength).append("\n");

        // 3. TRANSFORMATION - только если есть в тексте
        List<String> transformations = extractTransformations(text);
        if (!transformations.isEmpty()) {
            cdsl.append("TRANSFORMATION [");
            for (int i = 0; i < transformations.size(); i++) {
                if (i > 0) cdsl.append(", ");
                cdsl.append("\"").append(transformations.get(i)).append("\"");
            }
            cdsl.append("]\n");
        }

        // 4. Правило изменения - только если есть в тексте
        String changeRule = extractChangeRule(text);
        if (changeRule != null && !changeRule.isEmpty()) {
            cdsl.append(changeRule).append("\n");
        }

        // 5. Делимость - только если есть в тексте
        List<String> divisibilityConditions = extractDivisibilityConditions(text);
        if (!divisibilityConditions.isEmpty()) {
            cdsl.append("DIVISIBILITY_RULES [");
            for (int i = 0; i < divisibilityConditions.size(); i++) {
                if (i > 0) cdsl.append(", ");
                cdsl.append(divisibilityConditions.get(i));
            }
            cdsl.append("]\n");
        }

        // 6. Позиции цифр - только если есть в тексте
        List<String> digitPositions = extractDigitPositions(text);
        if (!digitPositions.isEmpty()) {
            cdsl.append("DIGIT_POSITIONS [");
            for (int i = 0; i < digitPositions.size(); i++) {
                if (i > 0) cdsl.append(", ");
                cdsl.append(digitPositions.get(i));
            }
            cdsl.append("]\n");
        }

        // 7. Ограничения на цифры - только если есть в тексте
        List<String> digitConstraints = extractDigitConstraints(text);
        if (!digitConstraints.isEmpty()) {
            cdsl.append("DIGIT_CONSTRAINTS [");
            for (int i = 0; i < digitConstraints.size(); i++) {
                if (i > 0) cdsl.append(", ");
                cdsl.append(digitConstraints.get(i));
            }
            cdsl.append("]\n");
        }

        // 8. CALCULATE COMBINATIONS
        cdsl.append("CALCULATE COMBINATIONS");

        return cdsl.toString();
    }

    /**
     * Извлекает длину числа
     */
    private int extractNumberLength(String text) {
        String lowerText = text.toLowerCase();

        // Паттерны для поиска длины числа
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

                // "n-значное число"
                Pattern.compile("(\\d+)-значн[а-я]+\\s+числ"),

                // Словесные обозначения
                Pattern.compile("(двух?|трех?|четырех?|пяти|шести|семи|восьми|девяти|десяти)-?значн[а-я]+")
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

        // По умолчанию: 3-значные числа
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
     * Извлекает преобразования (перестановки цифр)
     */
    private List<String> extractTransformations(String text) {
        List<String> transformations = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // 1. ПЕРЕСТАНОВКА ЦИФР
        if (lowerText.contains("перестановк") ||
                lowerText.contains("перестав") ||
                lowerText.contains("изменение порядка цифр")) {

            // Ищем конкретные примеры перестановок
            Pattern permutationPattern = Pattern.compile("\"([^\"]+)\"");
            Matcher permutationMatcher = permutationPattern.matcher(text);

            while (permutationMatcher.find()) {
                transformations.add(permutationMatcher.group(1));
            }

            // Если не нашли конкретные, добавляем общие
            if (transformations.isEmpty()) {
                if (lowerText.contains("обратном порядке") || lowerText.contains("наоборот")) {
                    transformations.add("REVERSE");
                }
                if (lowerText.contains("циклический сдвиг")) {
                    transformations.add("CYCLIC_SHIFT");
                }
                if (lowerText.contains("перестановка первой и последней цифры")) {
                    transformations.add("SWAP_FIRST_LAST");
                }
            }
        }

        // 2. ЗАМЕНА ЦИФР - только если явно указано
        if (lowerText.contains("замен") || lowerText.contains("изменение цифр")) {
            Pattern replacementPattern = Pattern.compile("замен[аеу]\\s+([\\d\\sи,]+)\\s+на\\s+([\\d\\sи,]+)");
            Matcher replacementMatcher = replacementPattern.matcher(lowerText);

            if (replacementMatcher.find()) {
                String from = replacementMatcher.group(1).replaceAll("[^\\d]", "");
                String to = replacementMatcher.group(2).replaceAll("[^\\d]", "");
                transformations.add("REPLACE_" + from + "_WITH_" + to);
            }
        }

        // 3. УДАЛЕНИЕ/ДОБАВЛЕНИЕ ЦИФР - только если явно указано
        if (lowerText.contains("удален") || lowerText.contains("убрать")) {
            transformations.add("REMOVE_DIGITS");
        }
        if (lowerText.contains("добавлен") || lowerText.contains("вставить")) {
            transformations.add("ADD_DIGITS");
        }

        return transformations;
    }

    /**
     * Извлекает правило изменения числа
     */
    private String extractChangeRule(String text) {
        String lowerText = text.toLowerCase();

        // 1. УВЕЛИЧЕНИЕ В ЦЕЛОЕ ЧИСЛО РАЗ
        Pattern increaseFactorPattern = Pattern.compile(
                "увелич[а-я]+\\s+в\\s+(\\d+)\\s+раз[а]?",
                Pattern.CASE_INSENSITIVE);
        Matcher increaseFactorMatcher = increaseFactorPattern.matcher(text);

        if (increaseFactorMatcher.find()) {
            int factor = Integer.parseInt(increaseFactorMatcher.group(1));
            return "INCREASES_BY_FACTOR " + factor;
        }

        // 2. УМЕНЬШЕНИЕ В ЦЕЛОЕ ЧИСЛО РАЗ
        Pattern decreaseFactorPattern = Pattern.compile(
                "уменьш[а-я]+\\s+в\\s+(\\d+)\\s+раз[а]?",
                Pattern.CASE_INSENSITIVE);
        Matcher decreaseFactorMatcher = decreaseFactorPattern.matcher(text);

        if (decreaseFactorMatcher.find()) {
            int factor = Integer.parseInt(decreaseFactorMatcher.group(1));
            return "DECREASES_BY_FACTOR " + factor;
        }

        // 3. УВЕЛИЧЕНИЕ НА КОНКРЕТНОЕ ЧИСЛО
        Pattern increaseByPattern = Pattern.compile(
                "увелич[а-я]+\\s+на\\s+(\\d+)",
                Pattern.CASE_INSENSITIVE);
        Matcher increaseByMatcher = increaseByPattern.matcher(text);

        if (increaseByMatcher.find()) {
            int amount = Integer.parseInt(increaseByMatcher.group(1));
            return "INCREASES_BY " + amount;
        }

        // 4. УМЕНЬШЕНИЕ НА КОНКРЕТНОЕ ЧИСЛО
        Pattern decreaseByPattern = Pattern.compile(
                "уменьш[а-я]+\\s+на\\s+(\\d+)",
                Pattern.CASE_INSENSITIVE);
        Matcher decreaseByMatcher = decreaseByPattern.matcher(text);

        if (decreaseByMatcher.find()) {
            int amount = Integer.parseInt(decreaseByMatcher.group(1));
            return "DECREASES_BY " + amount;
        }

        // 5. НЕ ИЗМЕНЯЕТСЯ
        if (lowerText.contains("не изменяется") ||
                lowerText.contains("остается прежним") ||
                lowerText.contains("остается тем же")) {
            return "UNCHANGED";
        }

        // 6. СТАНОВИТСЯ В КВАДРАТ
        if (lowerText.contains("квадрат") || lowerText.contains("во второй степени")) {
            return "INCREASES_BY_FACTOR (SQUARE)";
        }

        // 7. УДВАИВАЕТСЯ/УТРАИВАЕТСЯ И Т.Д.
        if (lowerText.contains("удваивается") || lowerText.contains("в два раза больше")) {
            return "INCREASES_BY_FACTOR 2";
        }
        if (lowerText.contains("утраивается") || lowerText.contains("в три раза больше")) {
            return "INCREASES_BY_FACTOR 3";
        }
        if (lowerText.contains("увеличивается в 10 раз")) {
            return "INCREASES_BY_FACTOR 10";
        }

        return null;
    }

    /**
     * Извлекает условия делимости
     */
    private List<String> extractDivisibilityConditions(String text) {
        List<String> conditions = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // Паттерны для делимости
        Pattern[] patterns = {
                // "делится на n"
                Pattern.compile("делит[ся]? на (\\d+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("кратн[оы]? (\\d+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("делится без остатка на (\\d+)", Pattern.CASE_INSENSITIVE),

                // "остаток от деления на n равен m"
                Pattern.compile("остаток от деления на (\\d+) равен (\\d+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("при делении на (\\d+) дает остаток (\\d+)", Pattern.CASE_INSENSITIVE),

                // "делится на n после преобразования"
                Pattern.compile("после [^\\d]*(\\d+)[^\\d]*делится на (\\d+)", Pattern.CASE_INSENSITIVE),

                // Признаки делимости
                Pattern.compile("делится на\\s+(\\d+)\\s+по признаку", Pattern.CASE_INSENSITIVE),
                Pattern.compile("признак делимости на (\\d+)", Pattern.CASE_INSENSITIVE)
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                if (pattern.pattern().contains("остаток") && matcher.groupCount() >= 2) {
                    String divisor = matcher.group(1);
                    String remainder = matcher.group(2);
                    conditions.add("REMAINDER " + divisor + " = " + remainder);
                } else if (matcher.groupCount() >= 1) {
                    String divisor = matcher.group(1);
                    conditions.add("DIVISIBLE_BY " + divisor);
                }
            }
        }

        // Убраны условия по умолчанию
        return conditions;
    }

    /**
     * Извлекает позиции цифр для анализа
     */
    private List<String> extractDigitPositions(String text) {
        List<String> positions = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // Позиции цифр, которые важны для делимости
        if (lowerText.contains("первая цифра") || lowerText.contains("первой цифры")) {
            positions.add("FIRST_DIGIT");
        }
        if (lowerText.contains("последняя цифра") || lowerText.contains("последней цифры")) {
            positions.add("LAST_DIGIT");
        }
        if (lowerText.contains("последние две цифры")) {
            positions.add("LAST_TWO_DIGITS");
        }
        if (lowerText.contains("последние три цифры")) {
            positions.add("LAST_THREE_DIGITS");
        }
        if (lowerText.contains("четные позиции") || lowerText.contains("цифры на четных местах")) {
            positions.add("EVEN_POSITIONS");
        }
        if (lowerText.contains("нечетные позиции") || lowerText.contains("цифры на нечетных местах")) {
            positions.add("ODD_POSITIONS");
        }
        if (lowerText.contains("сумма цифр")) {
            positions.add("SUM_OF_DIGITS");
        }
        if (lowerText.contains("произведение цифр")) {
            positions.add("PRODUCT_OF_DIGITS");
        }
        if (lowerText.contains("знакочередующаяся сумма")) {
            positions.add("ALTERNATING_SUM");
        }

        return positions;
    }

    /**
     * Извлекает ограничения на цифры
     */
    private List<String> extractDigitConstraints(String text) {
        List<String> constraints = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // 1. ЦИФРЫ НЕ ПОВТОРЯЮТСЯ
        if (lowerText.contains("различные цифры") ||
                lowerText.contains("цифры не повторяются") ||
                lowerText.contains("все цифры различны")) {
            constraints.add("DISTINCT_DIGITS");
        }

        // 2. ЦИФРЫ ИЗ ОПРЕДЕЛЕННОГО НАБОРА
        Pattern digitsPattern = Pattern.compile("цифр[аы]?\\s+([\\d\\sи,]+)");
        Matcher digitsMatcher = digitsPattern.matcher(lowerText);
        if (digitsMatcher.find()) {
            String digits = digitsMatcher.group(1).replaceAll("[^\\d]", "");
            if (!digits.isEmpty()) {
                constraints.add("ALLOWED_DIGITS " + digits);
            }
        }

        // 3. ПЕРВАЯ ЦИФРА НЕ НУЛЬ
        if (lowerText.contains("первая цифра не нуль") ||
                lowerText.contains("не начинается с нуля") ||
                lowerText.contains("не ноль в начале")) {
            constraints.add("FIRST_DIGIT_NOT_ZERO");
        }

        // 4. ЧЕТНЫЕ/НЕЧЕТНЫЕ ЦИФРЫ
        if (lowerText.contains("только четные цифры")) {
            constraints.add("ONLY_EVEN_DIGITS");
        }
        if (lowerText.contains("только нечетные цифры")) {
            constraints.add("ONLY_ODD_DIGITS");
        }

        // 5. ОГРАНИЧЕНИЯ НА КОНКРЕТНЫЕ ЦИФРЫ
        Pattern containsPattern = Pattern.compile("содержит цифру (\\d)");
        Matcher containsMatcher = containsPattern.matcher(lowerText);
        while (containsMatcher.find()) {
            constraints.add("CONTAINS_DIGIT " + containsMatcher.group(1));
        }

        Pattern notContainsPattern = Pattern.compile("не содержит цифру (\\d)");
        Matcher notContainsMatcher = notContainsPattern.matcher(lowerText);
        while (notContainsMatcher.find()) {
            constraints.add("NOT_CONTAINS_DIGIT " + notContainsMatcher.group(1));
        }

        return constraints;
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
     * Обогащает контекст данными из текста
     */
    private void enrichContextFromText(ProblemContext context, String text, String taskTitle) {
        if (context == null) return;

        // Устанавливаем тип задачи
        context.setProblemType(ProblemType.DIVISIBILITY);
        context.setTaskName(taskTitle);

        // Устанавливаем параметры
        int numberLength = extractNumberLength(text);
        context.setDigits(numberLength);

        // Устанавливаем преобразование только если оно есть
        List<String> transformations = extractTransformations(text);
        if (!transformations.isEmpty()) {
            String transformation = String.join(" и ", transformations);
            context.setTransformation(transformation);
        }

        // Устанавливаем правило изменения только если оно есть
        String changeRule = extractChangeRule(text);
        if (changeRule != null) {
            parseChangeRule(changeRule, context);
        }

        // Устанавливаем расчет
        context.setCalculationType("COMBINATIONS");

        // Добавляем условия делимости только если они есть
        List<String> divisibilityConditions = extractDivisibilityConditions(text);
        for (String condition : divisibilityConditions) {
            context.addDivisibilityCondition(condition);
        }

        // Добавляем позиции цифр только если они есть
        List<String> digitPositions = extractDigitPositions(text);
        context.setDigitPositions(digitPositions);

        // Добавляем ограничения на цифры только если они есть
        List<String> digitConstraints = extractDigitConstraints(text);
        for (String constraint : digitConstraints) {
            context.addGeneralCondition(constraint);
        }

        // Сохраняем исходный текст
        context.setParameter("originalText", text);
    }

    /**
     * Парсит правило изменения и устанавливает в контекст
     */
    private void parseChangeRule(String changeRule, ProblemContext context) {
        String[] parts = changeRule.split("\\s+");

        if (parts.length >= 2) {
            String ruleType = parts[0];

            switch (ruleType) {
                case "INCREASES_BY_FACTOR":
                    if (parts.length >= 2) {
                        try {
                            int factor = Integer.parseInt(parts[1]);
                            context.setFactor(factor);
                            context.setOperationType("INCREASE_BY_FACTOR");
                        } catch (NumberFormatException e) {
                            // Оставляем пустым
                        }
                    }
                    break;

                case "DECREASES_BY_FACTOR":
                    if (parts.length >= 2) {
                        try {
                            int factor = Integer.parseInt(parts[1]);
                            context.setFactor(factor);
                            context.setOperationType("DECREASE_BY_FACTOR");
                        } catch (NumberFormatException e) {
                            // Оставляем пустым
                        }
                    }
                    break;

                case "INCREASES_BY":
                    if (parts.length >= 2) {
                        try {
                            int amount = Integer.parseInt(parts[1]);
                            context.setFactor(amount);
                            context.setOperationType("INCREASE_BY");
                        } catch (NumberFormatException e) {
                            context.setOperationType("INCREASE");
                        }
                    }
                    break;

                case "DECREASES_BY":
                    if (parts.length >= 2) {
                        try {
                            int amount = Integer.parseInt(parts[1]);
                            context.setFactor(amount);
                            context.setOperationType("DECREASE_BY");
                        } catch (NumberFormatException e) {
                            context.setOperationType("DECREASE");
                        }
                    }
                    break;

                case "UNCHANGED":
                    context.setOperationType("UNCHANGED");
                    break;
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