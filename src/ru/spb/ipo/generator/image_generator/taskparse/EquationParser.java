package ru.spb.ipo.generator.image_generator.taskparse;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemInterpreter;
import ru.spb.ipo.generator.image_generator.cdsl.parser.ASTNode;
import ru.spb.ipo.generator.image_generator.cdsl.parser.CDSLParser;
import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.CDSLTokenizer;
import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.Token;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Упрощенный парсер для задач с уравнениями
 */
public class EquationParser {

    private ProblemContext lastContext;

    /**
     * Основной метод парсинга задачи с уравнениями
     */
    public String parse(String taskTitle, String taskText) {
        // 1. Анализируем текст задачи и преобразуем в CDSL
        String cdslCode = analyzeAndConvertToCDSL(taskTitle, taskText);

        try {
            List<Token> tokens = CDSLTokenizer.tokenize(cdslCode);
            CDSLParser parser = new CDSLParser(tokens);
            ASTNode ast = parser.parse();
            ProblemContext context = ProblemInterpreter.interpret(ast);

            enrichContext(context, taskText);
            this.lastContext = context;

        } catch (Exception e) {
            this.lastContext = createMinimalContext(taskText);
        }

        return cdslCode;
    }

    /**
     * Анализирует текст задачи и преобразует в CDSL синтаксис
     */
    private String analyzeAndConvertToCDSL(String title, String text) {
        StringBuilder cdsl = new StringBuilder();

        cdsl.append("TASK EQUATIONS \"Equation Task\"\n");

        int unknownsCount = extractUnknownsCount(text);
        cdsl.append("UNKNOWNS ").append(unknownsCount).append("\n");

        Integer sumValue = extractSum(text);
        if (sumValue != null) {
            cdsl.append("SUM ").append(sumValue).append("\n");
        } else {
            cdsl.append("SUM 10\n");
        }

        cdsl.append("CALCULATE COMBINATIONS");

        return cdsl.toString();
    }

    /**
     * Дополняет контекст данными
     */
    private void enrichContext(ProblemContext context, String originalText) {
        int unknownsCount = extractUnknownsCount(originalText);
        Integer sum = extractSum(originalText);

        context.setTaskName("Equation Task");
        context.setParameter("unknowns", unknownsCount);
        context.setParameter("sum", sum != null ? sum : 10);

        if (sum != null) {
            context.setSum(sum);
        } else {
            context.setSum(10);
        }
    }

    /**
     * Создает минимальный контекст при ошибке парсинга
     */
    private ProblemContext createMinimalContext(String text) {
        ProblemContext context = new ProblemContext();

        int unknowns = extractUnknownsCount(text);
        Integer sum = extractSum(text);

        context.setTaskName("Equation Task");
        context.setParameter("unknowns", unknowns);
        context.setParameter("sum", sum != null ? sum : 10);

        if (sum != null) {
            context.setSum(sum);
        } else {
            context.setSum(10);
        }

        return context;
    }

    /**
     * Извлекает количество неизвестных переменных
     */
    private int extractUnknownsCount(String text) {
        Pattern variablePattern = Pattern.compile("x(\\d+)\\b");
        Matcher matcher = variablePattern.matcher(text);

        int maxIndex = 0;
        while (matcher.find()) {
            try {
                int index = Integer.parseInt(matcher.group(1));
                if (index > maxIndex) {
                    maxIndex = index;
                }
            } catch (NumberFormatException e) {
            }
        }

        if (maxIndex > 0) {
            return maxIndex;
        }

        return 2;
    }

    /**
     * Извлекает сумму (правую часть уравнения)
     */
    private Integer extractSum(String text) {
        String lowerText = text.toLowerCase();

        Pattern equationPattern = Pattern.compile("=\\s*(\\d+)");
        Matcher matcher = equationPattern.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
            }
        }

        return null;
    }

    /**
     * Геттер для последнего контекста
     */
    public ProblemContext getLastContext() {
        return lastContext;
    }
}