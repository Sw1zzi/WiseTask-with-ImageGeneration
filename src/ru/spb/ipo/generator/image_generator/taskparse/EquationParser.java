//package ru.spb.ipo.generator.image_generator.taskparse;
//
//import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
//import ru.spb.ipo.generator.image_generator.cdsl.parser.ASTNode;
//import ru.spb.ipo.generator.image_generator.cdsl.parser.CDSLParser;
//import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.CDSLTokenizer;
//import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.Token;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Парсер для задач с уравнениями - работает аналогично CardParser
// */
//public class EquationParser {
//
//    private ProblemContext lastContext;
//
//    /**
//     * Основной метод парсинга задачи с уравнениями
//     */
//    public String parse(String taskTitle, String taskText) {
//        System.out.println("EquationParser: Parsing equation task: " + taskTitle);
//
//        // 1. Анализируем текст задачи и преобразуем в CDSL
//        String cdslCode = analyzeAndConvertToCDSL(taskTitle, taskText);
//
//        System.out.println("EquationParser: Generated CDSL:\n" + cdslCode);
//
//        // 2. Токенизация и парсинг CDSL кода
//        try {
//            List<Token> tokens = CDSLTokenizer.tokenize(cdslCode);
//
//            // 3. Парсинг в AST
//            CDSLParser parser = new CDSLParser(tokens);
//            ASTNode ast = parser.parse();
//
//            // 4. Интерпретация AST в ProblemContext (так же как в CardParser)
//            ProblemContext context = interpretAST(ast, taskTitle, taskText);
//            this.lastContext = context;
//
//            System.out.println("EquationParser: Context created successfully");
//
//        } catch (Exception e) {
//            System.err.println("EquationParser: Parse error - " + e.getMessage());
//            e.printStackTrace();
//            // Создаем минимальный контекст даже при ошибке
//            this.lastContext = createMinimalContext(taskTitle, taskText);
//        }
//
//        return cdslCode;
//    }
//
//    /**
//     * Анализирует текст задачи и преобразует в CDSL синтаксис
//     */
//    private String analyzeAndConvertToCDSL(String title, String text) {
//        StringBuilder cdsl = new StringBuilder();
//
//        // 1. TASK EQUATIONS "Название задачи" (обязательно)
//        cdsl.append("TASK EQUATIONS \"").append(escapeQuotes(title)).append("\"\n");
//
//        // 2. UNKNOWNS - количество неизвестных (обязательно)
//        int unknownsCount = extractUnknownsCount(text);
//        cdsl.append("UNKNOWNS ").append(unknownsCount).append("\n");
//
//        // 3. SUM - сумма переменных (если найдена)
//        Integer sumValue = extractSum(text);
//        if (sumValue != null) {
//            cdsl.append("SUM ").append(sumValue).append("\n");
//        }
//
//        // 4. DOMAIN - область определения переменных (обязательно)
//        String domain = extractDomain(text);
//        cdsl.append("DOMAIN \"").append(domain).append("\"\n");
//
//        // 5. CONSTRAINTS - дополнительные ограничения (если есть)
//        List<String> constraints = extractConstraints(text, unknownsCount);
//        if (!constraints.isEmpty()) {
//            cdsl.append("CONSTRAINTS [");
//            for (int i = 0; i < constraints.size(); i++) {
//                if (i > 0) cdsl.append(", ");
//                cdsl.append("\"").append(escapeQuotes(constraints.get(i))).append("\"");
//            }
//            cdsl.append("]\n");
//        }
//
//        // 6. COEFFICIENTS - коэффициенты уравнения (если явно указаны)
//        List<Integer> coefficients = extractCoefficients(text, unknownsCount);
//        if (!coefficients.isEmpty() && !allCoefficientsAreOne(coefficients)) {
//            cdsl.append("COEFFICIENTS [");
//            for (int i = 0; i < coefficients.size(); i++) {
//                if (i > 0) cdsl.append(", ");
//                cdsl.append(coefficients.get(i));
//            }
//            cdsl.append("]\n");
//        }
//
//        // 7. CALCULATE - тип расчета (обязательно)
//        String calculationType = extractCalculationType(text);
//        cdsl.append("CALCULATE ").append(calculationType);
//
//        return cdsl.toString();
//    }
//
//    /**
//     * Интерпретирует AST в ProblemContext (как в CardParser)
//     */
//    private ProblemContext interpretAST(ASTNode ast, String taskTitle, String originalText) {
//        // Создаем контекст задачи
//        ProblemContext context = new ProblemContext();
////        context.setTaskType("EQUATIONS");
////        context.setTaskName(taskTitle);
////        context.setOriginalText(originalText);
//
//        // Создаем карту параметров (как в CardParser)
//        Map<String, Object> parameters = new HashMap<>();
//
//        // Обходим AST и извлекаем информацию
//        for (ASTNode child : ast.getChildren()) {
//            switch (child.getType()) {
//                case "UNKNOWNS_DECLARATION":
//                    if (child.getChild(0) != null && child.getChild(0).getValue() != null) {
//                        int unknowns = (Integer) child.getChild(0).getValue();
//                        parameters.put("unknownsCount", unknowns);
//                        context.setParameter("unknownsCount", unknowns);
//                    }
//                    break;
//
//                case "SUM_DECLARATION":
//                    if (child.getChild(0) != null && child.getChild(0).getValue() != null) {
//                        int sum = (Integer) child.getChild(0).getValue();
//                        parameters.put("sum", sum);
//                        context.setParameter("sum", sum);
//                    }
//                    break;
//
//                case "DOMAIN_DECLARATION":
//                    if (child.getChild(0) != null && child.getChild(0).getValue() != null) {
//                        String domain = (String) child.getChild(0).getValue();
//                        parameters.put("domain", domain);
//                        context.setParameter("domain", domain);
//                    }
//                    break;
//
//                case "CONSTRAINTS_DECLARATION":
//                    List<String> constraints = new ArrayList<>();
//                    for (ASTNode constraintNode : child.getChildren()) {
//                        if ("CONSTRAINT".equals(constraintNode.getType()) && constraintNode.getValue() != null) {
//                            constraints.add(constraintNode.getValue().toString());
//                        }
//                    }
//                    if (!constraints.isEmpty()) {
//                        parameters.put("constraints", constraints);
//                        context.setParameter("constraints", constraints);
//                    }
//                    break;
//
//                case "COEFFICIENTS_DECLARATION":
//                    List<Integer> coefficients = new ArrayList<>();
//                    // Извлекаем коэффициенты из детей
//                    for (ASTNode coeffNode : child.getChildren()) {
//                        if (coeffNode.getValue() instanceof Integer) {
//                            coefficients.add((Integer) coeffNode.getValue());
//                        }
//                    }
//                    if (!coefficients.isEmpty()) {
//                        parameters.put("coefficients", coefficients);
//                        context.setParameter("coefficients", coefficients);
//                    }
//                    break;
//
//                case "CALCULATE":
//                    if (child.getChild(0) != null && child.getChild(0).getValue() != null) {
//                        String calcType = (String) child.getChild(0).getValue();
//                        parameters.put("calculationType", calcType);
//                        context.setParameter("calculationType", calcType);
//                    }
//                    break;
//            }
//        }
//
//        // Заполняем недостающие параметры значениями по умолчанию
//        if (!parameters.containsKey("unknownsCount")) {
//            int unknowns = extractUnknownsCount(originalText);
//            parameters.put("unknownsCount", unknowns);
//            context.setParameter("unknownsCount", unknowns);
//        }
//
//        if (!parameters.containsKey("domain")) {
//            String domain = extractDomain(originalText);
//            parameters.put("domain", domain);
//            context.setParameter("domain", domain);
//        }
//
//        if (!parameters.containsKey("calculationType")) {
//            String calcType = extractCalculationType(originalText);
//            parameters.put("calculationType", calcType);
//            context.setParameter("calculationType", calcType);
//        }
//
//        // Сохраняем все параметры в контекст
////        context.setParameters(parameters);
//
//        System.out.println("EquationParser: Context parameters: " + parameters);
//
//        return context;
//    }
//
//    /**
//     * Создает минимальный контекст при ошибке парсинга
//     */
//    private ProblemContext createMinimalContext(String taskTitle, String text) {
//        ProblemContext context = new ProblemContext();
//        context.setTaskType("EQUATIONS");
//        context.setTaskName(taskTitle);
//        context.setOriginalText(text);
//
//        Map<String, Object> parameters = new HashMap<>();
//
//        // Извлекаем базовые параметры напрямую из текста
//        int unknowns = extractUnknownsCount(text);
//        String domain = extractDomain(text);
//        String calcType = extractCalculationType(text);
//        Integer sum = extractSum(text);
//        List<String> constraints = extractConstraints(text, unknowns);
//
//        parameters.put("unknownsCount", unknowns);
//        parameters.put("domain", domain);
//        parameters.put("calculationType", calcType);
//
//        if (sum != null) {
//            parameters.put("sum", sum);
//        }
//
//        if (!constraints.isEmpty()) {
//            parameters.put("constraints", constraints);
//        }
//
//        context.setParameters(parameters);
//
//        return context;
//    }
//
//    // ===================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ (остаются без изменений) =====================
//
//    /**
//     * Извлекает количество неизвестных переменных
//     */
//    private int extractUnknownsCount(String text) {
//        String lowerText = text.toLowerCase();
//
//        // 1. Ищем явное указание количества
//        Pattern explicitPattern = Pattern.compile(
//                "(?:с|из|в)\\s+(\\d+)\\s+(?:неизвестными|неизвестных|переменными|переменных)"
//        );
//        Matcher matcher = explicitPattern.matcher(lowerText);
//        if (matcher.find()) {
//            try {
//                return Integer.parseInt(matcher.group(1));
//            } catch (NumberFormatException e) {
//                // ignore
//            }
//        }
//
//        // 2. Ищем по перечислению переменных: x1, x2, x3, ...
//        Pattern variablePattern = Pattern.compile("x(\\d+)");
//        matcher = variablePattern.matcher(text);
//        int maxIndex = 0;
//        while (matcher.find()) {
//            try {
//                int index = Integer.parseInt(matcher.group(1));
//                if (index > maxIndex) maxIndex = index;
//            } catch (NumberFormatException e) {
//                // ignore
//            }
//        }
//
//        if (maxIndex > 0) {
//            return maxIndex;
//        }
//
//        // 3. По умолчанию - 2 неизвестных
//        return 2;
//    }
//
//    /**
//     * Извлекает сумму (правую часть уравнения)
//     */
//    private Integer extractSum(String text) {
//        String lowerText = text.toLowerCase();
//
//        // 1. Ищем уравнение вида: ... = число
//        Pattern equationPattern = Pattern.compile("=\\s*(\\d+)");
//        Matcher matcher = equationPattern.matcher(text);
//        if (matcher.find()) {
//            try {
//                return Integer.parseInt(matcher.group(1));
//            } catch (NumberFormatException e) {
//                // ignore
//            }
//        }
//
//        // 2. Ищем фразы: "сумма равна", "всего", "равно"
//        String[] sumPhrases = {
//                "сумма\\s*равна\\s*(\\d+)",
//                "всего\\s*(\\d+)",
//                "равно\\s*(\\d+)",
//                "равна\\s*(\\d+)"
//        };
//
//        for (String phrase : sumPhrases) {
//            Pattern pattern = Pattern.compile(phrase, Pattern.CASE_INSENSITIVE);
//            matcher = pattern.matcher(lowerText);
//            if (matcher.find()) {
//                try {
//                    return Integer.parseInt(matcher.group(1));
//                } catch (NumberFormatException e) {
//                    // ignore
//                }
//            }
//        }
//
//        return null; // Сумма не указана явно
//    }
//
//    /**
//     * Извлекает область определения переменных
//     */
//    private String extractDomain(String text) {
//        String lowerText = text.toLowerCase();
//
//        // 1. Проверяем стандартные области
//        if (lowerText.contains("натуральн") && !lowerText.contains("ненулевые натуральн")) {
//            return "NATURAL";
//        } else if (lowerText.contains("целые") || lowerText.contains("целых")) {
//            return "INTEGER";
//        } else if (lowerText.contains("неотрицательные") || lowerText.contains("неотрицательных")) {
//            return "NON_NEGATIVE";
//        } else if (lowerText.contains("положительные") || lowerText.contains("положительных")) {
//            return "POSITIVE";
//        }
//
//        // 2. Ищем диапазон
//        Pattern rangePattern = Pattern.compile(
//                "от\\s*(\\d+)\\s*до\\s*(\\d+)",
//                Pattern.CASE_INSENSITIVE
//        );
//
//        Matcher matcher = rangePattern.matcher(lowerText);
//        if (matcher.find()) {
//            return matcher.group(1) + "-" + matcher.group(2);
//        }
//
//        // 3. По умолчанию - натуральные числа
//        return "NATURAL";
//    }
//
//    /**
//     * Извлекает дополнительные ограничения
//     */
//    private List<String> extractConstraints(String text, int unknownsCount) {
//        List<String> constraints = new ArrayList<>();
//        String lowerText = text.toLowerCase();
//
//        // 1. Ищем неравенства
//        Pattern inequalityPattern = Pattern.compile(
//                "x(\\d+)\\s*([<>]=?)\\s*x(\\d+)|" +
//                        "x(\\d+)\\s*([<>]=?)\\s*(\\d+)|" +
//                        "(\\d+)\\s*([<>]=?)\\s*x(\\d+)"
//        );
//
//        Matcher matcher = inequalityPattern.matcher(text);
//        while (matcher.find()) {
//            constraints.add(matcher.group().trim());
//        }
//
//        // 2. Текстовые ограничения
//        if (lowerText.contains("различн") || lowerText.contains("разные")) {
//            constraints.add("все x различны");
//        }
//
//        if (lowerText.contains("возрастающ") || lowerText.contains("x1 < x2")) {
//            StringBuilder order = new StringBuilder();
//            for (int i = 1; i <= unknownsCount; i++) {
//                if (i > 1) order.append(" < ");
//                order.append("x").append(i);
//            }
//            constraints.add(order.toString());
//        }
//
//        // 3. Условия четности
//        for (int i = 1; i <= unknownsCount; i++) {
//            String var = "x" + i;
//            if (lowerText.contains(var + " четн")) {
//                constraints.add(var + " четное");
//            }
//            if (lowerText.contains(var + " нечетн")) {
//                constraints.add(var + " нечетное");
//            }
//        }
//
//        return constraints;
//    }
//
//    /**
//     * Извлекает коэффициенты уравнения
//     */
//    private List<Integer> extractCoefficients(String text, int unknownsCount) {
//        List<Integer> coefficients = new ArrayList<>();
//
//        // Ищем коэффициенты: 2x1, 3x2, x3 (коэффициент 1)
//        Pattern coeffPattern = Pattern.compile("(\\d*)\\s*x\\d+\\b");
//
//        Matcher matcher = coeffPattern.matcher(text);
//        int found = 0;
//        while (matcher.find() && found < unknownsCount) {
//            String coeffStr = matcher.group(1);
//            if (coeffStr.isEmpty()) {
//                coefficients.add(1);
//            } else {
//                try {
//                    coefficients.add(Integer.parseInt(coeffStr));
//                } catch (NumberFormatException e) {
//                    coefficients.add(1);
//                }
//            }
//            found++;
//        }
//
//        // Дополняем единицами если нужно
//        while (coefficients.size() < unknownsCount) {
//            coefficients.add(1);
//        }
//
//        return coefficients;
//    }
//
//    /**
//     * Проверяет, все ли коэффициенты равны 1
//     */
//    private boolean allCoefficientsAreOne(List<Integer> coefficients) {
//        for (int coeff : coefficients) {
//            if (coeff != 1) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * Извлекает тип расчета
//     */
//    private String extractCalculationType(String text) {
//        String lowerText = text.toLowerCase();
//
//        if (lowerText.contains("вероятность")) {
//            return "PROBABILITY";
//        } else if (lowerText.contains("комбинации")) {
//            return "COMBINATIONS";
//        } else if (lowerText.contains("ожидание")) {
//            return "EXPECTATION";
//        }
//
//        return "SOLUTIONS_COUNT";
//    }
//
//    /**
//     * Экранирует кавычки
//     */
//    private String escapeQuotes(String str) {
//        return str.replace("\"", "'");
//    }
//
//    /**
//     * Геттер для последнего контекста
//     */
//    public ProblemContext getLastContext() {
//        return lastContext;
//    }
//}