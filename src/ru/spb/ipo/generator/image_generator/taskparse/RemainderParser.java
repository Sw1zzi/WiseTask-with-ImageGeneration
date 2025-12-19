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
 * Парсер для задач на остатки от деления
 */
public class RemainderParser {

    private ProblemContext lastContext;

    /**
     * Основной метод парсинга задачи на остатки
     */
    public String parse(String taskTitle, String taskText) {
        // 1. Анализируем текст задачи и преобразуем в CDSL
        String cdslCode = analyzeAndConvertToCDSL(taskText);

        // 2. Токенизация и парсинг CDSL кода
        try {
            List<Token> tokens = CDSLTokenizer.tokenize(cdslCode);

            // 3. Парсинг в AST
            CDSLParser parser = new CDSLParser(tokens);
            ASTNode ast = parser.parse();

            /*
            System.out.println("\nАбстрактное синтаксическое дерево (AST):");
            printAST(ast, 0);
            */

            // 4. Интерпретация AST
            ProblemContext context = ProblemInterpreter.interpret(ast);
            this.lastContext = context;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cdslCode;
    }

    /**
     * Анализирует текст задачи и преобразует в CDSL синтаксис
     */
    private String analyzeAndConvertToCDSL(String text) {
        StringBuilder cdsl = new StringBuilder();

        // 1. TASK REMAINDER "Название"
        cdsl.append("TASK REMAINDERS \"").append(extractTaskName(text)).append("\"\n");

        // 2. DIVISOR <делитель>
        int divisor = extractDivisor(text);
        cdsl.append("DIVISOR ").append(divisor).append("\n");

        // 3. NUMBER_SET <диапазон>
        String numberSet = extractNumberSet(text);
        cdsl.append("NUMBER_SET ").append(numberSet).append("\n");

        // 4. OPERATION <операция>
        String operation = extractOperation(text);
        cdsl.append("OPERATION ").append(operation).append("\n");

        // 5. REQUIRED_REMAINDER <остаток>
        int requiredRemainder = extractRequiredRemainder(text);
        cdsl.append("REQUIRED_REMAINDER ").append(requiredRemainder).append("\n");

        // 6. CALCULATE COUNT
        cdsl.append("CALCULATE COUNT");

        return cdsl.toString();
    }

    /**
     * Извлекает делитель из текста задачи
     */
    private int extractDivisor(String text) {
        String lowerText = text.toLowerCase();

        // Паттерны для поиска делителя
        Pattern[] patterns = {
                Pattern.compile("на\\s+(\\d+)\\s*\\b"),
                Pattern.compile("делит(?:ся|ь)\\s+на\\s+(\\d+)\\s*\\b"),
                Pattern.compile("остаток\\s+от\\s+деления\\s+на\\s+(\\d+)\\s*\\b"),
                Pattern.compile("(\\d+)\\s*-\\s*значны[ех]"),
                Pattern.compile("(\\d+)\\s*разрядны[ех]")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    // continue
                }
            }
        }

        // Проверяем типичные делители в задачах
        if (lowerText.contains("делит на 2") || lowerText.contains("остаток от деления на 2")) return 2;
        if (lowerText.contains("делит на 3") || lowerText.contains("остаток от деления на 3")) return 3;
        if (lowerText.contains("делит на 4") || lowerText.contains("остаток от деления на 4")) return 4;
        if (lowerText.contains("делит на 5") || lowerText.contains("остаток от деления на 5")) return 5;
        if (lowerText.contains("делит на 6") || lowerText.contains("остаток от деления на 6")) return 6;
        if (lowerText.contains("делит на 7") || lowerText.contains("остаток от деления на 7")) return 7;
        if (lowerText.contains("делит на 8") || lowerText.contains("остаток от деления на 8")) return 8;
        if (lowerText.contains("делит на 9") || lowerText.contains("остаток от деления на 9")) return 9;
        if (lowerText.contains("делит на 10") || lowerText.contains("остаток от деления на 10")) return 10;

        // Проверяем числительные
        if (lowerText.contains("двух")) return 2;
        if (lowerText.contains("трех") || lowerText.contains("трёх")) return 3;
        if (lowerText.contains("четырех") || lowerText.contains("четырёх")) return 4;
        if (lowerText.contains("пяти")) return 5;
        if (lowerText.contains("шести")) return 6;
        if (lowerText.contains("семи")) return 7;
        if (lowerText.contains("восьми")) return 8;
        if (lowerText.contains("девяти")) return 9;
        if (lowerText.contains("десяти")) return 10;

        return 3; // По умолчанию
    }

    /**
     * Извлекает диапазон чисел из текста задачи
     */
    private String extractNumberSet(String text) {
        String lowerText = text.toLowerCase();

        // Паттерны для поиска диапазона чисел
        Pattern[] patterns = {
                // "все двузначные числа" или "все трехзначные числа"
                Pattern.compile("(\\d+)-значны[ех]\\s+чис"),
                Pattern.compile("(\\d+)-разрядны[ех]\\s+чис"),
                Pattern.compile("числа\\s+из\\s+(\\d+)\\s+цифр"),

                // Диапазон "от A до B"
                Pattern.compile("от\\s+(\\d+)\\s+до\\s+(\\d+)"),
                Pattern.compile("в\\s+диапазоне\\s+(\\d+)\\s*-\\s*(\\d+)"),

                // Конкретные числа
                Pattern.compile("числа\\s+(\\d+)\\s*,\\s*(\\d+)\\s*и\\s*(\\d+)"),
                Pattern.compile("числа\\s+(\\d+)\\s*и\\s*(\\d+)")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                if (pattern.pattern().contains("-значны") || pattern.pattern().contains("-разрядны")) {
                    int digits = Integer.parseInt(matcher.group(1));
                    int min = (int) Math.pow(10, digits - 1);
                    int max = (int) Math.pow(10, digits) - 1;
                    return min + "-" + max;
                } else if (pattern.pattern().contains("от") || pattern.pattern().contains("диапазон")) {
                    return matcher.group(1) + "-" + matcher.group(2);
                } else {
                    // Для конкретных чисел возвращаем список
                    StringBuilder numbers = new StringBuilder();
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        if (matcher.group(i) != null) {
                            if (numbers.length() > 0) numbers.append(",");
                            numbers.append(matcher.group(i));
                        }
                    }
                    return "[" + numbers.toString() + "]";
                }
            }
        }

        // Если нашли просто "двузначные", "трехзначные" и т.д.
        if (lowerText.contains("двузначн") || lowerText.contains("2-значн")) return "10-99";
        if (lowerText.contains("трехзначн") || lowerText.contains("3-значн") || lowerText.contains("трёхзначн")) return "100-999";
        if (lowerText.contains("четырехзначн") || lowerText.contains("4-значн") || lowerText.contains("четырёхзначн")) return "1000-9999";
        if (lowerText.contains("пятизначн") || lowerText.contains("5-значн")) return "10000-99999";
        if (lowerText.contains("шестизначн") || lowerText.contains("6-значн")) return "100000-999999";

        // По умолчанию: все двузначные числа
        return "10-99";
    }

    /**
     * Извлекает операцию из текста задачи
     */
    private String extractOperation(String text) {
        String lowerText = text.toLowerCase();

        // Определяем операцию по ключевым словам
        if (lowerText.contains("сумм") || lowerText.contains("складыва")) {
            return "SUM";
        } else if (lowerText.contains("произведен") || lowerText.contains("умножа")) {
            return "PRODUCT";
        } else if (lowerText.contains("разност") || lowerText.contains("вычита")) {
            return "DIFFERENCE";
        } else if (lowerText.contains("цифр") && lowerText.contains("сумм")) {
            return "DIGIT_SUM";
        } else if (lowerText.contains("цифр") && lowerText.contains("произведен")) {
            return "DIGIT_PRODUCT";
        } else if (lowerText.contains("перв") && lowerText.contains("послед")) {
            return "FIRST_LAST_DIFFERENCE";
        } else if (lowerText.contains("четн") && lowerText.contains("нечетн")) {
            return "EVEN_ODD_COUNT";
        }

        return "SELF"; // По умолчанию: само число
    }

    /**
     * Извлекает требуемый остаток из текста задачи
     */
    private int extractRequiredRemainder(String text) {
        String lowerText = text.toLowerCase();

        // Паттерны для поиска остатка
        Pattern[] patterns = {
                Pattern.compile("остаток\\s+(\\d+)\\s*\\b"),
                Pattern.compile("остатком\\s+(\\d+)\\s*\\b"),
                Pattern.compile("равен\\s+(\\d+)\\s*\\b"),
                Pattern.compile("дает\\s+остаток\\s+(\\d+)\\s*\\b"),
                Pattern.compile("остатку\\s+(\\d+)\\s*\\b"),
                Pattern.compile("(\\d+)\\s*\\s*в\\s+остатке")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(lowerText);
            while (matcher.find()) {
                try {
                    int remainder = Integer.parseInt(matcher.group(1));
                    // Проверяем, что это действительно остаток (меньше делителя обычно)
                    int divisor = extractDivisor(text);
                    if (remainder >= 0 && remainder < divisor) {
                        return remainder;
                    }
                } catch (NumberFormatException e) {
                    // continue
                }
            }
        }

        // Проверяем словесные описания остатков
        if (lowerText.contains("кратн") || lowerText.contains("делится без остатка") ||
                lowerText.contains("остаток 0")) {
            return 0;
        }
        if (lowerText.contains("остаток 1") || lowerText.contains("остатком 1")) return 1;
        if (lowerText.contains("остаток 2") || lowerText.contains("остатком 2")) return 2;
        if (lowerText.contains("остаток 3") || lowerText.contains("остатком 3")) return 3;
        if (lowerText.contains("остаток 4") || lowerText.contains("остатком 4")) return 4;
        if (lowerText.contains("остаток 5") || lowerText.contains("остатком 5")) return 5;

        // Проверяем четность/нечетность
        if (lowerText.contains("четн") || lowerText.contains("делится на 2") ||
                lowerText.contains("кратно 2")) {
            return 0; // четное => остаток 0 при делении на 2
        }
        if (lowerText.contains("нечетн") || lowerText.contains("не делится на 2")) {
            return 1; // нечетное => остаток 1 при делении на 2
        }

        return 0; // По умолчанию
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

        // Добавляем тематику если название слишком короткое
        if (result.split("\\s+").length < 3) {
            result += " (остатки от деления)";
        }

        return result;
    }

    /**
     * Вспомогательный класс для представления диапазона чисел
     */
    private static class NumberRange {
        int min;
        int max;

        NumberRange(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public String toString() {
            return min + "-" + max;
        }
    }

    /**
     * Печатает AST (закомментировано)
     */
    /*
    private void printAST(ASTNode node, int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }

        System.out.println(indent + node.getType());

        // Выводим значение, если оно не null
        if (node.getValue() != null) {
            System.out.println(indent + "  VALUE: " + node.getValue());
        }

        for (ASTNode child : node.getChildren()) {
            printAST(child, depth + 1);
        }
    }
    */

    /**
     * Возвращает контекст задачи после парсинга
     */
    public ProblemContext getLastContext() {
        return lastContext;
    }
}