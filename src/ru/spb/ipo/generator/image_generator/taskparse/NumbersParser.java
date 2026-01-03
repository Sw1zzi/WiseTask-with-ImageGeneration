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

        // 3. MAX_DIGIT <n>
        int maxDigit = extractMaxDigit(text);
        cdsl.append("MAX_DIGIT ").append(maxDigit).append("\n");

        // 4. FIRST_NOT_ZERO YES|NO
        boolean firstNotZero = extractFirstNotZero(text);
        cdsl.append("FIRST_NOT_ZERO ").append(firstNotZero ? "YES" : "NO").append("\n");

        // 5. DISTINCT YES|NO
        boolean distinct = extractDistinctDigits(text);
        cdsl.append("DISTINCT ").append(distinct ? "YES" : "NO").append("\n");

        // 6. CALCULATE COMBINATIONS
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

        // По умолчанию: 3-значные числа
        return 3;
    }

    /**
     * Извлекает максимальную цифру (от 0 до X)
     */
    private int extractMaxDigit(String text) {
        String lowerText = text.toLowerCase();

        // Паттерн для поиска "цифр от 0 до X" или "цифр 0-9"
        Pattern[] patterns = {
                Pattern.compile("цифр\\s+от\\s+\\d+\\s+до\\s+(\\d+)"),
                Pattern.compile("цифр\\s+0\\s*-\\s*(\\d+)"),
                Pattern.compile("цифр\\s+(\\d)\\s*-\\s*(\\d+)"),
                Pattern.compile("цифр[ы]?\\s+(?:от\\s+)?0\\s*до\\s*(\\d+)"),
                Pattern.compile("используются цифры\\s+(?:от\\s+)?0\\s*до\\s*(\\d+)"),
                Pattern.compile("цифр[аы]?\\s+из\\s+диапазона\\s+0\\s*-\\s*(\\d+)")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                try {
                    // Для паттерна с двумя группами (цифр X-Y)
                    if (pattern.pattern().contains("(\\d)\\s*-\\s*(\\d+)")) {
                        if (matcher.groupCount() >= 2) {
                            String maxDigitStr = matcher.group(2);
                            return Integer.parseInt(maxDigitStr);
                        }
                    }
                    // Для других паттернов
                    else if (matcher.groupCount() >= 1) {
                        String maxDigitStr = matcher.group(1);
                        return Integer.parseInt(maxDigitStr);
                    }
                } catch (NumberFormatException e) {
                    // continue
                }
            }
        }

        // Если явно указано "цифр от 0 до 9"
        if (lowerText.contains("цифр от 0 до 9")) {
            return 9;
        }

        // Если указано "цифр 0-9"
        if (lowerText.contains("цифр 0-9")) {
            return 9;
        }

        // Если указано просто "цифр" без диапазона
        if (lowerText.contains("цифр") && !lowerText.contains("от") && !lowerText.contains("до")) {
            // По умолчанию 9 для стандартных десятичных цифр
            return 9;
        }

        // Проверяем другие варианты
        Pattern singleDigitPattern = Pattern.compile("цифр[аы]?\\s+(\\d)");
        Matcher singleMatcher = singleDigitPattern.matcher(lowerText);
        if (singleMatcher.find()) {
            try {
                return Integer.parseInt(singleMatcher.group(1));
            } catch (NumberFormatException e) {
                // continue
            }
        }

        // По умолчанию возвращаем 9
        return 9;
    }


    private boolean extractFirstNotZero(String text) {
        String lowerText = text.toLowerCase();

        // Правило: если говорится о "наборах чисел" или "наборах цифр" -
        // тогда первая цифра МОЖЕТ быть 0
        // Иначе (обычные "числа" или "цифры") - первая цифра НЕ может быть 0

        boolean isNumberSet = lowerText.contains("набор") &&
                (lowerText.contains("чисел") || lowerText.contains("цифр"));

        // Если это "наборы чисел/цифр" - первая цифра МОЖЕТ быть 0
        if (isNumberSet) {
            return false; // NO - первая может быть 0
        }

        // Проверяем явные указания в тексте
        if (lowerText.contains("первая цифра не может быть 0") ||
                lowerText.contains("первая цифра не 0") ||
                lowerText.contains("число не может начинаться с 0") ||
                lowerText.contains("не начинается с 0")) {
            return true; // YES - первая не может быть 0
        }

        if (lowerText.contains("первая цифра может быть 0") ||
                lowerText.contains("может начинаться с 0") ||
                lowerText.contains("начинается с 0")) {
            return false; // NO - первая может быть 0
        }

        // Для однозначных чисел всегда может быть 0
        int digits = extractDigits(text);
        if (digits == 1) {
            return false; // NO - для 1-значных 0 допустим
        }

        // По умолчанию: для многозначных чисел первая цифра НЕ 0
        return digits > 1;
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

        // По умолчанию: цифры могут повторяться
        return false;
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

        int maxDigit = extractMaxDigit(text);
        context.setMaxDigit(maxDigit);

        // Определяем, может ли первая цифра быть 0
        boolean firstNotZero = extractFirstNotZero(text);
        context.setFirstNotZero(firstNotZero);

        boolean distinct = extractDistinctDigits(text);
        context.setDistinctDigits(distinct);

        // Устанавливаем расчет
        context.setCalculationType("COMBINATIONS");

        // Сохраняем исходный текст
        context.setParameter("originalText", text);
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