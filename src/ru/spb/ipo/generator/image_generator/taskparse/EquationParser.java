package ru.spb.ipo.generator.image_generator.taskparse;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemInterpreter;
import ru.spb.ipo.generator.image_generator.cdsl.parser.ASTNode;
import ru.spb.ipo.generator.image_generator.cdsl.parser.CDSLParser;
import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.CDSLTokenizer;
import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсер для задач с уравнениями - работает аналогично CardParser
 */
public class EquationParser {

    private ProblemContext lastContext;

    /**
     * Основной метод парсинга задачи с уравнениями
     */
    public String parse(String taskTitle, String taskText) {
        System.out.println("EquationParser: Parsing equation task: " + taskTitle);

        // 1. Анализируем текст задачи и преобразуем в CDSL
        String cdslCode = analyzeAndConvertToCDSL(taskTitle, taskText);

        System.out.println("EquationParser: Generated CDSL:\n" + cdslCode);

        // 2. Токенизация и парсинг CDSL кода
        try {
            List<Token> tokens = CDSLTokenizer.tokenize(cdslCode);

            System.out.println("EquationParser: Tokenization complete. Tokens count: " + tokens.size());

            // 3. Парсинг в AST
            CDSLParser parser = new CDSLParser(tokens);
            ASTNode ast = parser.parse();

            // 4. Интерпретация AST в ProblemContext
            ProblemContext context = ProblemInterpreter.interpret(ast);

            // 5. Дополняем контекст данными, которые не были распознаны парсером
            enrichContext(context, taskTitle, taskText);

            this.lastContext = context;

            System.out.println("EquationParser: Context created successfully");
            System.out.println("Context: " + context);

        } catch (Exception e) {
            System.err.println("EquationParser: Parse error - " + e.getMessage());
            e.printStackTrace();
            // Создаем минимальный контекст даже при ошибке
            this.lastContext = createMinimalContext(taskTitle, taskText);
        }

        return cdslCode;
    }

    /**
     * Анализирует текст задачи и преобразует в CDSL синтаксис
     */
    private String analyzeAndConvertToCDSL(String title, String text) {
        StringBuilder cdsl = new StringBuilder();

        // 1. TASK EQUATIONS "Название задачи"
        cdsl.append("TASK EQUATIONS \"").append(escapeQuotes(title)).append("\"\n");

        // 2. UNKNOWNS - количество неизвестных
        int unknownsCount = extractUnknownsCount(text);
        cdsl.append("UNKNOWNS ").append(unknownsCount).append("\n");

        // 3. SUM - сумма переменных
        Integer sumValue = extractSum(text);
        if (sumValue != null) {
            cdsl.append("SUM ").append(sumValue).append("\n");
        } else {
            // По умолчанию сумма 10
            cdsl.append("SUM 10\n");
        }

        // 4. DOMAIN - область определения переменных
        String domain = extractDomain(text);
        cdsl.append("DOMAIN \"").append(domain).append("\"\n");

        // 5. CONSTRAINTS - дополнительные ограничения
        List<String> constraints = extractConstraints(text, unknownsCount);
        if (!constraints.isEmpty()) {
            cdsl.append("CONSTRAINTS [");
            for (int i = 0; i < constraints.size(); i++) {
                if (i > 0) cdsl.append(", ");
                cdsl.append("\"").append(escapeQuotes(constraints.get(i))).append("\"");
            }
            cdsl.append("]\n");
        }

        // 6. CALCULATE - тип расчета
        String calculationType = extractCalculationType(text);
        cdsl.append("CALCULATE ").append(calculationType);

        return cdsl.toString();
    }

    /**
     * Дополняет контекст данными
     */
    private void enrichContext(ProblemContext context, String taskTitle, String originalText) {
        // Устанавливаем название задачи
        context.setTaskName(taskTitle);

        // Извлекаем параметры из текста
        int unknownsCount = extractUnknownsCount(originalText);
        String domain = extractDomain(originalText);
        Integer sum = extractSum(originalText);
        List<String> constraints = extractConstraints(originalText, unknownsCount);
        List<Integer> coefficients = extractCoefficients(originalText, unknownsCount);

        // Сохраняем параметры в контекст
        context.setParameter("unknownsCount", unknownsCount);
        context.setParameter("domain", domain);
        context.setParameter("unknowns", unknownsCount);

        if (sum != null) {
            context.setParameter("sum", sum);
            context.setSum(sum);
        } else {
            context.setParameter("sum", 10);
            context.setSum(10);
        }

        if (!constraints.isEmpty()) {
            context.setParameter("constraints", constraints);
            context.setParameter("constraintsList", constraints);
        }

        if (!coefficients.isEmpty() && !allCoefficientsAreOne(coefficients)) {
            context.setParameter("coefficients", coefficients);
        }

        String calcType = extractCalculationType(originalText);
        context.setParameter("calculationType", calcType);
        context.setCalculationType(calcType);

        // Устанавливаем тип задачи
        try {
            context.setProblemType(ru.spb.ipo.generator.image_generator.cdsl.model.ProblemType.EQUATIONS);
        } catch (Exception e) {
            // Игнорируем если не удалось установить тип
        }
    }

    /**
     * Создает минимальный контекст при ошибке парсинга
     */
    private ProblemContext createMinimalContext(String taskTitle, String text) {
        ProblemContext context = new ProblemContext();

        // Базовые параметры
        context.setTaskName(taskTitle);

        int unknowns = extractUnknownsCount(text);
        String domain = extractDomain(text);
        String calcType = extractCalculationType(text);
        Integer sum = extractSum(text);
        List<String> constraints = extractConstraints(text, unknowns);

        context.setParameter("unknownsCount", unknowns);
        context.setParameter("domain", domain);
        context.setParameter("calculationType", calcType);
        context.setParameter("unknowns", unknowns);

        if (sum != null) {
            context.setParameter("sum", sum);
            context.setSum(sum);
        } else {
            context.setParameter("sum", 10);
            context.setSum(10);
        }

        if (!constraints.isEmpty()) {
            context.setParameter("constraints", constraints);
            context.setParameter("constraintsList", constraints);
        }

        // Тип задачи
        try {
            context.setProblemType(ru.spb.ipo.generator.image_generator.cdsl.model.ProblemType.EQUATIONS);
        } catch (Exception e) {
            // Игнорируем
        }

        context.setCalculationType(calcType);

        return context;
    }

    /**
     * Извлекает количество неизвестных переменных
     */
    private int extractUnknownsCount(String text) {
        String lowerText = text.toLowerCase();

        // 1. Ищем явное указание количества
        Pattern explicitPattern = Pattern.compile(
                "(?:с|из|в)\\s+(\\d+)\\s+(?:неизвестными|неизвестных|переменными|переменных)"
        );
        Matcher matcher = explicitPattern.matcher(lowerText);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        // 2. Ищем по перечислению переменных: x1, x2, x3, ...
        Pattern variablePattern = Pattern.compile("x(\\d+)");
        matcher = variablePattern.matcher(text);
        int maxIndex = 0;
        while (matcher.find()) {
            try {
                int index = Integer.parseInt(matcher.group(1));
                if (index > maxIndex) maxIndex = index;
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (maxIndex > 0) {
            return maxIndex;
        }

        // 3. Ищем "x и y" - 2 переменные
        if (lowerText.contains(" x ") && lowerText.contains(" y ")) {
            return 2;
        }

        // 4. По умолчанию - 2 неизвестных
        return 2;
    }

    /**
     * Извлекает сумму (правую часть уравнения)
     */
    private Integer extractSum(String text) {
        String lowerText = text.toLowerCase();

        // 1. Ищем уравнение вида: ... = число
        Pattern equationPattern = Pattern.compile("=\\s*(\\d+)");
        Matcher matcher = equationPattern.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        // 2. Ищем фразы: "сумма равна", "всего", "равно"
        String[] sumPhrases = {
                "сумма\\s*равна\\s*(\\d+)",
                "всего\\s*(\\d+)",
                "равно\\s*(\\d+)",
                "равна\\s*(\\d+)",
                "составляет\\s*(\\d+)"
        };

        for (String phrase : sumPhrases) {
            Pattern pattern = Pattern.compile(phrase, Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        // 3. Ищем сумму в начале: "x1 + x2 + ... = число"
        Pattern sumEquationPattern = Pattern.compile(
                "(?:x\\d+\\s*\\+\\s*)+x?\\d*\\s*=\\s*(\\d+)"
        );
        matcher = sumEquationPattern.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        return null; // Сумма не указана явно
    }

    /**
     * Извлекает область определения переменных
     */
    private String extractDomain(String text) {
        String lowerText = text.toLowerCase();

        // 1. Проверяем стандартные области
        if (lowerText.contains("натуральн") || lowerText.contains("натур")) {
            return "NATURAL";
        } else if (lowerText.contains("целые") || lowerText.contains("целых") ||
                lowerText.contains("целое") || lowerText.contains("целым")) {
            return "INTEGER";
        } else if (lowerText.contains("неотрицательные") || lowerText.contains("неотрицательных") ||
                lowerText.contains("неотрицательн") || lowerText.contains("больше или равно 0")) {
            return "NON_NEGATIVE";
        } else if (lowerText.contains("положительные") || lowerText.contains("положительных") ||
                lowerText.contains("положительн") || lowerText.contains("больше 0")) {
            return "POSITIVE";
        } else if (lowerText.contains("ненулевые натуральн")) {
            return "POSITIVE_NATURAL";
        }

        // 2. Ищем диапазон
        Pattern rangePattern = Pattern.compile(
                "от\\s*(\\d+)\\s*до\\s*(\\d+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = rangePattern.matcher(lowerText);
        if (matcher.find()) {
            return "RANGE_" + matcher.group(1) + "_" + matcher.group(2);
        }

        // 3. По умолчанию - натуральные числа
        return "NATURAL";
    }

    /**
     * Извлекает дополнительные ограничения
     */
    private List<String> extractConstraints(String text, int unknownsCount) {
        List<String> constraints = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // 1. Ищем неравенства
        Pattern inequalityPattern = Pattern.compile(
                "x(\\d+)\\s*([<>]=?)\\s*x(\\d+)|" +
                        "x(\\d+)\\s*([<>]=?)\\s*(\\d+)|" +
                        "(\\d+)\\s*([<>]=?)\\s*x(\\d+)"
        );

        Matcher matcher = inequalityPattern.matcher(text);
        while (matcher.find()) {
            String constraint = matcher.group().trim();
            constraints.add(constraint);
            System.out.println("Found inequality constraint: " + constraint);
        }

        // 2. Текстовые ограничения
        if (lowerText.contains("различн") || lowerText.contains("разные") ||
                lowerText.contains("не равны друг другу")) {
            constraints.add("all different");
            System.out.println("Added 'all different' constraint");
        }

        if (lowerText.contains("возрастающ") || lowerText.contains("x1 < x2") ||
                lowerText.contains("x1 меньше x2") || lowerText.contains("упорядочен")) {
            StringBuilder order = new StringBuilder();
            for (int i = 1; i <= unknownsCount; i++) {
                if (i > 1) order.append(" < ");
                order.append("x").append(i);
            }
            constraints.add(order.toString());
            System.out.println("Added ordering constraint: " + order);
        }

        // 3. Условия четности
        for (int i = 1; i <= unknownsCount; i++) {
            String var = "x" + i;
            if (lowerText.contains(var + " четн") || lowerText.contains("четное " + var)) {
                constraints.add(var + " even");
                System.out.println("Added even constraint for " + var);
            }
            if (lowerText.contains(var + " нечетн") || lowerText.contains("нечетное " + var)) {
                constraints.add(var + " odd");
                System.out.println("Added odd constraint for " + var);
            }
        }

        // 4. Условия взаимной простоты
        if (lowerText.contains("взаимно прост") || lowerText.contains("попарно прост")) {
            constraints.add("pairwise coprime");
            System.out.println("Added coprime constraint");
        }

        return constraints;
    }

    /**
     * Извлекает коэффициенты уравнения
     */
    private List<Integer> extractCoefficients(String text, int unknownsCount) {
        List<Integer> coefficients = new ArrayList<>();

        // Ищем коэффициенты: 2x1, 3x2, x3 (коэффициент 1)
        Pattern coeffPattern = Pattern.compile("(\\d*)\\s*x\\d+\\b");

        Matcher matcher = coeffPattern.matcher(text);
        int found = 0;
        while (matcher.find() && found < unknownsCount) {
            String coeffStr = matcher.group(1);
            if (coeffStr.isEmpty()) {
                coefficients.add(1);
            } else {
                try {
                    coefficients.add(Integer.parseInt(coeffStr));
                } catch (NumberFormatException e) {
                    coefficients.add(1);
                }
            }
            found++;
        }

        // Дополняем единицами если нужно
        while (coefficients.size() < unknownsCount) {
            coefficients.add(1);
        }

        System.out.println("Extracted coefficients: " + coefficients);

        return coefficients;
    }

    /**
     * Проверяет, все ли коэффициенты равны 1
     */
    private boolean allCoefficientsAreOne(List<Integer> coefficients) {
        for (int coeff : coefficients) {
            if (coeff != 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Извлекает тип расчета
     */
    private String extractCalculationType(String text) {
        String lowerText = text.toLowerCase();

        if (lowerText.contains("вероятность") || lowerText.contains("вероятност")) {
            return "PROBABILITY";
        } else if (lowerText.contains("комбинации") || lowerText.contains("количество решений") ||
                lowerText.contains("сколько решений") || lowerText.contains("найти все решения")) {
            return "COMBINATIONS";
        } else if (lowerText.contains("ожидание") || lowerText.contains("матожидание")) {
            return "EXPECTATION";
        }

        // Для уравнений по умолчанию ищем количество решений
        return "COMBINATIONS";
    }

    /**
     * Экранирует кавычки
     */
    private String escapeQuotes(String str) {
        if (str == null) return "";
        return str.replace("\"", "'");
    }

    /**
     * Геттер для последнего контекста
     */
    public ProblemContext getLastContext() {
        return lastContext;
    }
}