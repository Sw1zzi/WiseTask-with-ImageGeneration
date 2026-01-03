package ru.spb.ipo.generator.image_generator.taskparse;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemInterpreter;
import ru.spb.ipo.generator.image_generator.cdsl.parser.ASTNode;
import ru.spb.ipo.generator.image_generator.cdsl.parser.CDSLParser;
import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.CDSLTokenizer;
import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Упрощенный парсер для задач со словами (WORDS)
 */
public class WordsParser {

    private ProblemContext lastContext;

    /**
     * Основной метод парсинга задачи со словами
     */
    public String parse(String taskTitle, String taskText) {
        try {
            String cdslCode = analyzeAndConvertToCDSL(taskText);

            List<Token> tokens = CDSLTokenizer.tokenize(cdslCode);

            CDSLParser parser = new CDSLParser(tokens);
            ASTNode ast = parser.parse();

            ProblemContext context = ProblemInterpreter.interpret(ast);

            enrichContextFromText(context, taskText);

            this.lastContext = context;
            return cdslCode;

        } catch (Exception e) {
            return createContextDirectly(taskText);
        }
    }

    /**
     * Анализирует текст задачи и преобразует в CDSL синтаксис
     */
    private String analyzeAndConvertToCDSL(String text) {
        StringBuilder cdsl = new StringBuilder();

        cdsl.append("TASK WORDS \"Words Task\"\n");

        String alphabet = extractAlphabet(text);
        cdsl.append("ALPHABET \"").append(alphabet).append("\"\n");

        int length = extractWordLength(text);
        cdsl.append("LENGTH ").append(length).append("\n");

        boolean uniqueLetters = extractUniqueLetters(text);
        cdsl.append("UNIQUE ").append(uniqueLetters ? "YES" : "NO").append("\n");

        cdsl.append("CALCULATE COMBINATIONS");

        return cdsl.toString();
    }

    /**
     * Извлекает алфавит из текста задачи
     */
    private String extractAlphabet(String text) {
        Pattern pattern = Pattern.compile("\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String letters = matcher.group(1).trim();

            if (letters.isEmpty()) {
                return "";
            }

            letters = letters.replaceAll("[,\\s]", "").trim();

            if (letters.matches("[А-Яа-яA-Z]+")) {
                return letters.toUpperCase();
            }
        }

        return "";
    }

    /**
     * Извлекает длину слова из текста задачи
     */
    private int extractWordLength(String text) {
        String lowerText = text.toLowerCase();

        Pattern pattern = Pattern.compile("слов[ао]?\\s+длины\\s+(\\d+)");
        Matcher matcher = pattern.matcher(lowerText);

        if (matcher.find()) {
            try {
                int length = Integer.parseInt(matcher.group(1));
                if (length > 0 && length <= 20) {
                    return length;
                }
            } catch (NumberFormatException e) {
            }
        }

        return 0;
    }

    /**
     * Определяет, могут ли буквы повторяться
     */
    private boolean extractUniqueLetters(String text) {
        String lowerText = text.toLowerCase();

        if (lowerText.contains("буквы в словах не могут повторяться")) {
            return true;
        }

        if (lowerText.contains("буквы в словах могут повторяться")) {
            return false;
        }

        return false;
    }

    /**
     * Обогащает контекст данными из текста
     */
    private void enrichContextFromText(ProblemContext context, String text) {
        if (context == null) return;

        context.setTaskName("Words Task");

        String alphabet = extractAlphabet(text);
        context.setAlphabet(alphabet);

        int wordLength = extractWordLength(text);
        context.setWordLength(wordLength);

        boolean uniqueLetters = extractUniqueLetters(text);
        context.setUniqueLetters(uniqueLetters);

        context.setCalculationType("COMBINATIONS");

        context.setParameter("originalText", text);
    }

    /**
     * Создает контекст напрямую (если CDSL парсинг не сработал)
     */
    private String createContextDirectly(String text) {
        try {
            ProblemContext context = new ProblemContext();

            enrichContextFromText(context, text);

            String cdslCode = analyzeAndConvertToCDSL(text);

            this.lastContext = context;
            return cdslCode;

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Возвращает контекст задачи после парсинга
     */
    public ProblemContext getLastContext() {
        return lastContext;
    }
}