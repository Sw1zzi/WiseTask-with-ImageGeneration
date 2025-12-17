package ru.spb.ipo.generator.image_generator.cdsl.interpreter;

import ru.spb.ipo.generator.image_generator.cdsl.model.Card;
import ru.spb.ipo.generator.image_generator.cdsl.model.ProblemType;
import ru.spb.ipo.generator.image_generator.cdsl.parser.ASTNode;

import java.util.*;

public class ProblemInterpreter {

    public static ProblemContext interpret(ASTNode ast) {
        ProblemContext context = new ProblemContext();
        System.out.println("ProblemInterpreter: Starting interpretation of AST");

        for (ASTNode node : ast.getChildren()) {
            System.out.println("ProblemInterpreter: Processing node: " + node.getType());

            switch (node.getType()) {
                case "TASK_DECLARATION":
                    interpretTaskDeclaration(node, context);
                    break;
                case "DECK_DECLARATION":
                    interpretDeckDeclaration(node, context);
                    break;
                case "ALPHABET_DECLARATION":
                    interpretAlphabetDeclaration(node, context);
                    break;
                case "LENGTH_DECLARATION":
                    interpretLengthDeclaration(node, context);
                    break;
                case "UNIQUE_DECLARATION":
                    interpretUniqueDeclaration(node, context);
                    break;
                case "TARGET_DECLARATION":
                    interpretTargetDeclaration(node, context);
                    break;
                case "DRAW_DECLARATION":
                    interpretDrawDeclaration(node, context);
                    break;
                case "CONDITION":
                    interpretGeneralCondition(node, context);
                    break;
                case "CALCULATE":
                    interpretCalculate(node, context);
                    break;
                case "UNKNOWNS_DECLARATION":
                    interpretUnknownsDeclaration(node, context);
                    break;
                case "SUM_DECLARATION":
                    interpretSumDeclaration(node, context);
                    break;
                case "DOMAIN_DECLARATION":
                    interpretDomainDeclaration(node, context);
                    break;
                case "CONSTRAINTS_DECLARATION":
                    interpretConstraintsDeclaration(node, context);
                    break;
                case "REMAINDERS_DECLARATION":
                    interpretRemaindersDeclaration(node, context);
                    break;
                case "DIVISIBILITY_DECLARATION":
                    interpretDivisibilityDeclaration(node, context);
                    break;
                case "BALLS_DECLARATION":
                    interpretBallsDeclaration(node, context);
                    break;
                case "NUMBERS_DECLARATION":
                    interpretNumbersDeclaration(node, context);
                    break;
                case "CHESS_DECLARATION":
                    interpretChessDeclaration(node, context);
                    break;
                default:
                    System.out.println("ProblemInterpreter: Unknown node type: " + node.getType());
                    break;
            }
        }

        System.out.println("ProblemInterpreter: Interpretation complete. Context: " + context);
        return context;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ОСНОВНЫХ ДЕКЛАРАЦИЙ
    // ============================================================

    private static void interpretTaskDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting TASK_DECLARATION");

        for (ASTNode child : node.getChildren()) {
            switch (child.getType()) {
                case "TASK_TYPE":
                    String taskType = (String) child.getValue();
                    System.out.println("ProblemInterpreter: Task type: " + taskType);
                    try {
                        ProblemType problemType = ProblemType.fromCdslName(taskType);
                        context.setProblemType(problemType);
                        System.out.println("ProblemInterpreter: Set problem type to: " + problemType);
                    } catch (IllegalArgumentException e) {
                        System.err.println("ProblemInterpreter: Unknown task type: " + taskType + ", defaulting to CARDS");
                        context.setProblemType(ProblemType.CARDS);
                    }
                    break;
                case "TASK_NAME":
                    String taskName = (String) child.getValue();
                    context.setTaskName(taskName);
                    System.out.println("ProblemInterpreter: Task name: " + taskName);
                    break;
            }
        }
    }

    private static void interpretDeckDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting DECK_DECLARATION");

        for (ASTNode child : node.getChildren()) {
            switch (child.getType()) {
                case "DECK_TYPE":
                    String deckType = (String) child.getValue();
                    context.setDeckType(deckType);
                    System.out.println("ProblemInterpreter: Deck type: " + deckType);
                    break;
                case "DECK_SIZE":
                    int deckSize = (Integer) child.getValue();
                    context.setDeckSize(deckSize);
                    System.out.println("ProblemInterpreter: Deck size: " + deckSize);
                    break;
            }
        }
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ЗАДАЧ СО СЛОВАМИ (WORDS)
    // ============================================================

    private static void interpretAlphabetDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting ALPHABET_DECLARATION");

        for (ASTNode child : node.getChildren()) {
            if ("ALPHABET".equals(child.getType())) {
                String alphabet = (String) child.getValue();
                context.setAlphabet(alphabet);
                context.setParameter("alphabet", alphabet);
                System.out.println("ProblemInterpreter: Alphabet set to: " + alphabet);
            }
        }
    }

    private static void interpretLengthDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting LENGTH_DECLARATION");

        for (ASTNode child : node.getChildren()) {
            if ("LENGTH".equals(child.getType())) {
                int wordLength = (Integer) child.getValue();
                context.setWordLength(wordLength);
                context.setParameter("wordLength", wordLength);
                System.out.println("ProblemInterpreter: Word length set to: " + wordLength);
            }
        }
    }

    private static void interpretUniqueDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting UNIQUE_DECLARATION");

        for (ASTNode child : node.getChildren()) {
            if ("UNIQUE".equals(child.getType())) {
                boolean uniqueLetters = (Boolean) child.getValue();
                context.setUniqueLetters(uniqueLetters);
                context.setParameter("uniqueLetters", uniqueLetters);
                System.out.println("ProblemInterpreter: Unique letters: " + uniqueLetters);
            }
        }
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ УСЛОВИЙ (включая условия для слов)
    // ============================================================

    private static void interpretGeneralCondition(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting CONDITION");

        for (ASTNode child : node.getChildren()) {
            if ("CONDITION_EXPR".equals(child.getType())) {
                String condition = (String) child.getValue();
                context.addGeneralCondition(condition);
                System.out.println("ProblemInterpreter: Added condition: " + condition);

                // Для задач со словами также добавляем как условие для слов
                if (context.getProblemType() == ProblemType.WORDS) {
                    context.addWordCondition(condition);
                    System.out.println("ProblemInterpreter: Added word condition: " + condition);
                }
            }
        }
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ЦЕЛЕВЫХ ОБЪЕКТОВ И КАРТ
    // ============================================================

    private static void interpretTargetDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting TARGET_DECLARATION");

        if (node.getChildren().isEmpty()) {
            System.out.println("ProblemInterpreter: Empty target declaration");
            return;
        }

        ASTNode targetNode = node.getChildren().get(0);
        if (targetNode == null) {
            System.out.println("ProblemInterpreter: Null target node");
            return;
        }

        if ("TARGET_LIST".equals(targetNode.getType())) {
            System.out.println("ProblemInterpreter: Processing target list");
            for (ASTNode itemNode : targetNode.getChildren()) {
                interpretTargetItem(itemNode, context);
            }
        } else {
            System.out.println("ProblemInterpreter: Processing single target");
            interpretTargetItem(targetNode, context);
        }
    }

    private static void interpretTargetItem(ASTNode itemNode, ProblemContext context) {
        if (itemNode == null) {
            System.out.println("ProblemInterpreter: Null target item");
            return;
        }

        System.out.println("ProblemInterpreter: Processing target item type: " + itemNode.getType());

        switch (itemNode.getType()) {
            case "CARD":
                interpretCard(itemNode, context);
                break;
            case "COUNT_CONDITION":
                interpretCountCondition(itemNode, context);
                break;
            case "CONDITION":
                interpretWordCondition(itemNode, context);
                break;
            default:
                System.out.println("ProblemInterpreter: Unknown target item type: " + itemNode.getType());
                break;
        }
    }

    private static void interpretCountCondition(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting COUNT_CONDITION");

        String countType = null;
        String countValue = null;
        String operator = null;
        Integer targetValue = null;

        for (ASTNode child : node.getChildren()) {
            switch (child.getType()) {
                case "COUNT_TYPE":
                    countType = (String) child.getValue();
                    System.out.println("ProblemInterpreter: Count type: " + countType);
                    break;
                case "COUNT_VALUE":
                    countValue = (String) child.getValue();
                    System.out.println("ProblemInterpreter: Count value: " + countValue);
                    break;
                case "OPERATOR":
                    operator = (String) child.getValue();
                    System.out.println("ProblemInterpreter: Operator: " + operator);
                    break;
                case "TARGET_VALUE":
                    targetValue = (Integer) child.getValue();
                    System.out.println("ProblemInterpreter: Target value: " + targetValue);
                    break;
            }
        }

        if (countType != null && countValue != null && operator != null && targetValue != null) {
            ProblemContext.CountCondition condition = new ProblemContext.CountCondition();
            condition.setCountType(countType);
            condition.setCountValue(countValue);
            condition.setOperator(operator);
            condition.setTargetValue(targetValue);
            context.addCountCondition(condition);
            System.out.println("ProblemInterpreter: Added count condition: " + condition);
        } else {
            System.err.println("ProblemInterpreter: Incomplete count condition");
        }
    }

    private static void interpretCard(ASTNode cardNode, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting CARD");

        String rank = null;
        String suit = null;

        for (ASTNode child : cardNode.getChildren()) {
            switch (child.getType()) {
                case "RANK":
                    rank = (String) child.getValue();
                    System.out.println("ProblemInterpreter: Card rank: " + rank);
                    break;
                case "SUIT":
                    suit = (String) child.getValue();
                    System.out.println("ProblemInterpreter: Card suit: " + suit);
                    break;
            }
        }

        if (rank != null && suit != null) {
            Card card = new Card(rank, suit);
            context.getTargetCards().add(card);
            System.out.println("ProblemInterpreter: Added target card: " + card);
        } else {
            System.err.println("ProblemInterpreter: Incomplete card definition");
        }
    }

    private static void interpretWordCondition(ASTNode conditionNode, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting WORD_CONDITION");

        for (ASTNode child : conditionNode.getChildren()) {
            if ("CONDITION_TYPE".equals(child.getType())) {
                String condition = (String) child.getValue();
                context.addWordCondition(condition);
                context.addGeneralCondition(condition);
                System.out.println("ProblemInterpreter: Added word condition: " + condition);
            }
        }
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ВЫТЯГИВАНИЯ И РАСЧЕТОВ
    // ============================================================

    private static void interpretDrawDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting DRAW_DECLARATION");

        for (ASTNode child : node.getChildren()) {
            switch (child.getType()) {
                case "DRAW_COUNT":
                    int drawCount = (Integer) child.getValue();
                    context.setDrawCount(drawCount);
                    System.out.println("ProblemInterpreter: Draw count: " + drawCount);
                    break;
                case "REPLACEMENT":
                    String replacement = (String) child.getValue();
                    boolean withReplacement = "REPLACEMENT".equals(replacement);
                    context.setWithReplacement(withReplacement);
                    System.out.println("ProblemInterpreter: With replacement: " + withReplacement);
                    break;
            }
        }
    }

    private static void interpretCalculate(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting CALCULATE");

        for (ASTNode child : node.getChildren()) {
            if ("CALCULATION_TYPE".equals(child.getType())) {
                String calculationType = (String) child.getValue();
                context.setCalculationType(calculationType);
                System.out.println("ProblemInterpreter: Calculation type: " + calculationType);
            }
        }
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ УРАВНЕНИЙ
    // ============================================================

    private static void interpretUnknownsDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting UNKNOWNS_DECLARATION");

        for (ASTNode child : node.getChildren()) {
            if ("UNKNOWNS_COUNT".equals(child.getType())) {
                int unknowns = (Integer) child.getValue();
                context.setParameter("unknowns", unknowns);
                context.setUnknowns(unknowns);
                System.out.println("ProblemInterpreter: Unknowns: " + unknowns);
            }
        }
    }

    private static void interpretSumDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting SUM_DECLARATION");

        for (ASTNode child : node.getChildren()) {
            if ("SUM_VALUE".equals(child.getType())) {
                int sum = (Integer) child.getValue();
                context.setParameter("sum", sum);
                context.setSum(sum);
                System.out.println("ProblemInterpreter: Sum: " + sum);
            }
        }
    }

    private static void interpretDomainDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting DOMAIN_DECLARATION");

        for (ASTNode child : node.getChildren()) {
            if ("DOMAIN".equals(child.getType())) {
                String domain = (String) child.getValue();
                context.setParameter("domain", domain);
                context.setDomain(domain);
                System.out.println("ProblemInterpreter: Domain: " + domain);
            }
        }
    }

    private static void interpretConstraintsDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting CONSTRAINTS_DECLARATION");

        for (ASTNode child : node.getChildren()) {
            if ("CONSTRAINT".equals(child.getType())) {
                String constraint = (String) child.getValue();
                context.addConstraint(constraint);
                System.out.println("ProblemInterpreter: Added constraint: " + constraint);
            }
        }
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ОСТАТКОВ
    // ============================================================

    private static void interpretRemaindersDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting REMAINDERS_DECLARATION");

        for (ASTNode child : node.getChildren()) {
            switch (child.getType()) {
                case "DIVIDEND":
                    String dividend = (String) child.getValue();
                    context.setDividend(dividend);
                    context.setParameter("dividend", dividend);
                    System.out.println("ProblemInterpreter: Dividend: " + dividend);
                    break;
                case "DIVISOR":
                    int divisor = (Integer) child.getValue();
                    context.setDivisor(divisor);
                    context.setParameter("divisor", divisor);
                    System.out.println("ProblemInterpreter: Divisor: " + divisor);
                    break;
                case "REMAINDER":
                    int remainder = (Integer) child.getValue();
                    context.setRemainder(remainder);
                    context.setParameter("remainder", remainder);
                    System.out.println("ProblemInterpreter: Remainder: " + remainder);
                    break;
            }
        }

        // Устанавливаем тип задачи
        context.setProblemType(ProblemType.REMAINDERS);
        System.out.println("ProblemInterpreter: Set problem type to REMAINDERS");
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ДЕЛИМОСТИ
    // ============================================================

    private static void interpretDivisibilityDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting DIVISIBILITY_DECLARATION");

        int digits = 2;
        String rule = "цифры переставляются";
        String transformation = "увеличивается в целое число раз";
        int factor = 2;
        String operationType = "INCREASE";
        List<String> conditions = new ArrayList<>();
        List<String> digitPositions = new ArrayList<>();
        String transformationSequence = null;

        for (ASTNode child : node.getChildren()) {
            System.out.println("ProblemInterpreter: Processing child: " + child.getType() + " = " + child.getValue());

            switch (child.getType()) {
                case "DIGITS":
                    digits = (Integer) child.getValue();
                    context.setDigits(digits);
                    context.setParameter("digits", digits);
                    System.out.println("ProblemInterpreter: Set digits to: " + digits);
                    break;

                case "RULE":
                    rule = (String) child.getValue();
                    context.setParameter("rule", rule);
                    System.out.println("ProblemInterpreter: Set rule to: " + rule);
                    break;

                case "TRANSFORMATION":
                case "TRANSFORMATION_SEQUENCE":
                    transformationSequence = (String) child.getValue();
                    context.setParameter("transformationSequence", transformationSequence);
                    System.out.println("ProblemInterpreter: Set transformation sequence to: " + transformationSequence);
                    break;

                case "FACTOR":
                    factor = (Integer) child.getValue();
                    context.setParameter("factor", factor);
                    System.out.println("ProblemInterpreter: Set factor to: " + factor);
                    break;

                case "OPERATION_TYPE":
                    operationType = (String) child.getValue();
                    context.setParameter("operationType", operationType);
                    System.out.println("ProblemInterpreter: Set operationType to: " + operationType);
                    break;

                case "CONDITION":
                    String condition = (String) child.getValue();
                    conditions.add(condition);
                    context.addGeneralCondition(condition);
                    System.out.println("ProblemInterpreter: Added condition: " + condition);
                    break;

                case "DIGIT_POSITION":
                    String digitPos = (String) child.getValue();
                    digitPositions.add(digitPos);
                    System.out.println("ProblemInterpreter: Added digit position: " + digitPos);
                    break;
            }
        }

        // Сохраняем digitPositions
        if (!digitPositions.isEmpty()) {
            context.setParameter("digitPositions", new ArrayList<>(digitPositions));
            context.setParameter("digitPositionsStr", String.join(", ", digitPositions));
        }

        // Сохраняем transformationSequence (если есть)
        if (transformationSequence != null && !transformationSequence.isEmpty()) {
            context.setParameter("transformationSequence", transformationSequence);
        }

        // Сохраняем собранные данные
        context.setParameter("digits", digits);
        context.setParameter("rule", rule);
        context.setParameter("transformation", transformation);
        context.setParameter("factor", factor);
        context.setParameter("operationType", operationType);

        if (!conditions.isEmpty()) {
            context.setParameter("conditions", String.join("; ", conditions));
        }

        // Формируем описание задачи
        StringBuilder description = new StringBuilder();
        description.append("Найти все ").append(digits).append("-значные натуральные числа");

        if (transformationSequence != null && !transformationSequence.isEmpty()) {
            description.append(", которые при преобразовании ");
            description.append(transformationSequence);
            description.append(" ").append(transformation);
        }
        else if (!digitPositions.isEmpty()) {
            description.append(", которые при изменении цифр по позициям: ");
            description.append(String.join(" ", digitPositions));
            description.append(" ").append(transformation);
        }
        else if (rule != null && !rule.isEmpty()) {
            description.append(", которые при преобразовании по правилу: ");
            description.append(rule);
            description.append(" ").append(transformation);
        }
        else {
            description.append(", которые ").append(transformation);
        }

        if (!conditions.isEmpty()) {
            description.append(" (условия: ").append(String.join(", ", conditions)).append(")");
        }

        context.setParameter("description", description.toString());
        context.setProblemType(ProblemType.DIVISIBILITY);

        System.out.println("ProblemInterpreter: Divisibility interpretation complete. Description: " + description);
        System.out.println("ProblemInterpreter: Transformation sequence: " + transformationSequence);
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ШАРОВ И УРН
    // ============================================================

    private static void interpretBallsDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting BALLS_DECLARATION");

        Map<String, Integer> urnContents = new HashMap<>();
        Map<String, Integer> drawBalls = new HashMap<>();
        boolean sequential = true;
        int drawCount = 1;

        for (ASTNode child : node.getChildren()) {
            switch (child.getType()) {
                case "URN_CONTENTS":
                    if (child.getValue() instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Integer> contents = (Map<String, Integer>) child.getValue();
                        urnContents.putAll(contents);

                        StringBuilder contentsStr = new StringBuilder();
                        for (Map.Entry<String, Integer> entry : contents.entrySet()) {
                            if (!contentsStr.isEmpty()) contentsStr.append(", ");
                            contentsStr.append(entry.getKey()).append(" ").append(entry.getValue());
                            context.setParameter("ball_" + entry.getKey().toLowerCase(), entry.getValue());
                        }
                        context.setParameter("contents", contentsStr.toString());
                        System.out.println("ProblemInterpreter: Urn contents: " + contentsStr);

                        int totalBalls = contents.values().stream().mapToInt(Integer::intValue).sum();
                        context.setParameter("totalBalls", totalBalls);
                        System.out.println("ProblemInterpreter: Total balls: " + totalBalls);
                    }
                    break;

                case "DRAW_BALLS":
                    if (child.getValue() instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Integer> draw = (Map<String, Integer>) child.getValue();
                        drawBalls.putAll(draw);

                        StringBuilder drawStr = new StringBuilder();
                        for (Map.Entry<String, Integer> entry : draw.entrySet()) {
                            if (!drawStr.isEmpty()) drawStr.append(", ");
                            drawStr.append(entry.getKey()).append(" ").append(entry.getValue());
                            context.setParameter("draw_" + entry.getKey().toLowerCase(), entry.getValue());
                        }
                        context.setParameter("draw_balls", drawStr.toString());
                        System.out.println("ProblemInterpreter: Draw balls: " + drawStr);

                        int totalDraw = draw.values().stream().mapToInt(Integer::intValue).sum();
                        drawCount = totalDraw;
                        context.setParameter("drawCount", totalDraw);
                        System.out.println("ProblemInterpreter: Draw count: " + totalDraw);
                    }
                    break;

                case "DRAW_TYPE":
                    sequential = (Boolean) child.getValue();
                    context.setParameter("drawType", sequential ? "SEQUENTIAL" : "SIMULTANEOUS");
                    System.out.println("ProblemInterpreter: Draw type sequential: " + sequential);
                    break;

                case "DRAW_COUNT":
                    drawCount = (Integer) child.getValue();
                    context.setParameter("drawCount", drawCount);
                    System.out.println("ProblemInterpreter: Draw count: " + drawCount);
                    break;
            }
        }

        if (drawBalls.isEmpty() && drawCount > 0) {
            drawBalls.put("RED", Math.min(1, drawCount));
            if (drawCount > 1) drawBalls.put("BLUE", 1);
            if (drawCount > 2) drawBalls.put("GREEN", 1);

            StringBuilder drawStr = new StringBuilder();
            for (Map.Entry<String, Integer> entry : drawBalls.entrySet()) {
                if (!drawStr.isEmpty()) drawStr.append(", ");
                drawStr.append(entry.getKey()).append(" ").append(entry.getValue());
            }
            context.setParameter("draw_balls", drawStr.toString());
            System.out.println("ProblemInterpreter: Default draw balls: " + drawStr);
        }

        if (urnContents.isEmpty()) {
            urnContents.put("RED", 3);
            urnContents.put("BLUE", 5);
            urnContents.put("GREEN", 2);
            urnContents.put("WHITE", 1);
            urnContents.put("BLACK", 4);

            StringBuilder contentsStr = new StringBuilder();
            for (Map.Entry<String, Integer> entry : urnContents.entrySet()) {
                if (!contentsStr.isEmpty()) contentsStr.append(", ");
                contentsStr.append(entry.getKey()).append(" ").append(entry.getValue());
                context.setParameter("ball_" + entry.getKey().toLowerCase(), entry.getValue());
            }
            context.setParameter("contents", contentsStr.toString());
            System.out.println("ProblemInterpreter: Default urn contents: " + contentsStr);
        }

        context.setProblemType(ProblemType.BALLS_AND_URNS);
        System.out.println("ProblemInterpreter: Set problem type to BALLS_AND_URNS");
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ЧИСЕЛ (NUMBERS)
    // ============================================================

    private static void interpretNumbersDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting NUMBERS_DECLARATION");

        // Значения по умолчанию
        int digits = 3;
        int maxDigit = 9;
        boolean firstNotZero = false;
        boolean distinct = false;
        boolean adjacentDifferent = false;
        String order = null;
        List<String> compareLeft = new ArrayList<>();
        List<String> compareRight = new ArrayList<>();
        String compareOperator = null;

        for (ASTNode child : node.getChildren()) {
            System.out.println("ProblemInterpreter: Processing NUMBERS child: " +
                    child.getType() + " = " + child.getValue());

            switch (child.getType()) {
                case "DIGITS":
                    digits = (Integer) child.getValue();
                    context.setDigits(digits);
                    context.setParameter("digits", digits);
                    System.out.println("ProblemInterpreter: Set digits to: " + digits);
                    break;

                case "MAX_DIGIT":
                    maxDigit = (Integer) child.getValue();
                    context.setParameter("maxDigit", maxDigit);
                    System.out.println("ProblemInterpreter: Set maxDigit to: " + maxDigit);
                    break;

                case "FIRST_NOT_ZERO":
                    firstNotZero = true;
                    context.setParameter("firstNotZero", true);
                    System.out.println("ProblemInterpreter: Set firstNotZero to true");
                    break;

                case "DISTINCT":
                    distinct = true;
                    context.setParameter("distinct", true);
                    System.out.println("ProblemInterpreter: Set distinct to true");
                    break;

                case "ADJACENT_DIFFERENT":
                    adjacentDifferent = true;
                    context.setParameter("adjacentDifferent", true);
                    System.out.println("ProblemInterpreter: Set adjacentDifferent to true");
                    break;

                case "ORDER":
                    order = (String) child.getValue();
                    context.setParameter("order", order);
                    System.out.println("ProblemInterpreter: Set order to: " + order);
                    break;

                case "COMPARE_LEFT":
                    compareLeft = (List<String>) child.getValue();
                    context.setParameter("compareLeft", new ArrayList<>(compareLeft));
                    System.out.println("ProblemInterpreter: Set compareLeft: " + compareLeft);
                    break;

                case "COMPARE_OPERATOR":
                    compareOperator = (String) child.getValue();
                    context.setParameter("compareOperator", compareOperator);
                    System.out.println("ProblemInterpreter: Set compareOperator: " + compareOperator);
                    break;

                case "COMPARE_RIGHT":
                    compareRight = (List<String>) child.getValue();
                    context.setParameter("compareRight", new ArrayList<>(compareRight));
                    System.out.println("ProblemInterpreter: Set compareRight: " + compareRight);
                    break;

                case "SUM_LEFT":
                    compareLeft = (List<String>) child.getValue();
                    context.setParameter("compareLeft", new ArrayList<>(compareLeft));
                    System.out.println("ProblemInterpreter: Set SUM left: " + compareLeft);
                    break;

                case "SUM_OPERATOR":
                    compareOperator = "=";
                    context.setParameter("compareOperator", "=");
                    System.out.println("ProblemInterpreter: Set SUM operator to =");
                    break;

                case "SUM_RIGHT":
                    compareRight = (List<String>) child.getValue();
                    context.setParameter("compareRight", new ArrayList<>(compareRight));
                    System.out.println("ProblemInterpreter: Set SUM right: " + compareRight);
                    break;
            }
        }

        // Формируем описание задачи
        StringBuilder description = new StringBuilder();
        description.append("Найти все наборы из ").append(digits).append(" цифр");
        description.append(" (цифры от 0 до ").append(maxDigit).append(")");

        if (firstNotZero) {
            description.append(", первая цифра ≠ 0");
        }

        if (distinct) {
            description.append(", все цифры различны");
        }

        if (adjacentDifferent) {
            description.append(", соседние цифры различны");
        }

        if (order != null) {
            String orderText = getOrderText(order);
            description.append(", цифры в ").append(orderText).append(" порядке");
        }

        if (!compareLeft.isEmpty() && !compareRight.isEmpty() && compareOperator != null) {
            description.append(", где сумма ").append(formatPositions(compareLeft))
                    .append(" ").append(compareOperator).append(" сумма ")
                    .append(formatPositions(compareRight));
        }

        context.setParameter("description", description.toString());
        context.setProblemType(ProblemType.NUMBERS);

        System.out.println("ProblemInterpreter: NUMBERS interpretation complete: " + description);
    }

    // Вспомогательный метод для форматирования позиций
    private static String formatPositions(List<String> positions) {
        return String.join("", positions);
    }

    // Вспомогательный метод для перевода порядка
    private static String getOrderText(String order) {
        return switch (order.toUpperCase()) {
            case "ASCENDING", "ASC" -> "возрастающем";
            case "DESCENDING", "DESC" -> "убывающем";
            case "NON_DECREASING" -> "неубывающем";
            case "NON_INCREASING" -> "невозрастающем";
            default -> order;
        };
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ШАХМАТ
    // ============================================================

    private static void interpretChessDeclaration(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting CHESS_DECLARATION");

        int height = 8;
        int width = 8;

        for (ASTNode child : node.getChildren()) {
            switch (child.getType()) {
                case "BOARD_HEIGHT":
                    height = (Integer) child.getValue();
                    context.setBoardHeight(height);
                    context.setParameter("boardHeight", height);
                    System.out.println("ProblemInterpreter: Board height: " + height);
                    break;
                case "BOARD_WIDTH":
                    width = (Integer) child.getValue();
                    context.setBoardWidth(width);
                    context.setParameter("boardWidth", width);
                    System.out.println("ProblemInterpreter: Board width: " + width);
                    break;
                case "PIECES":
                    if (child.getValue() instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Integer> piecesMap = (Map<String, Integer>) child.getValue();
                        context.getPieces().putAll(piecesMap);

                        StringBuilder piecesStr = new StringBuilder();
                        for (Map.Entry<String, Integer> entry : piecesMap.entrySet()) {
                            if (!piecesStr.isEmpty()) piecesStr.append(", ");
                            piecesStr.append(entry.getKey()).append(": ").append(entry.getValue());
                        }
                        context.setParameter("pieces", piecesStr.toString());
                        System.out.println("ProblemInterpreter: Pieces: " + piecesStr);

                        int totalPieces = piecesMap.values().stream().mapToInt(Integer::intValue).sum();
                        context.setParameter("totalPieces", totalPieces);
                        System.out.println("ProblemInterpreter: Total pieces: " + totalPieces);
                    }
                    break;
                case "ATTACKING":
                    boolean attacking = (Boolean) child.getValue();
                    context.setAttacking(attacking);
                    context.setParameter("attacking", attacking);
                    System.out.println("ProblemInterpreter: Attacking: " + attacking);
                    break;
            }
        }

        context.setProblemType(ProblemType.CHESS);
        context.setBoardHeight(height);
        context.setBoardWidth(width);
        context.setParameter("boardHeight", height);
        context.setParameter("boardWidth", width);

        System.out.println("ProblemInterpreter: Chess interpretation complete. Board: " + width + "x" + height);
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private static void interpretNumbersCondition(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting NUMBERS_CONDITION");

        for (ASTNode child : node.getChildren()) {
            switch (child.getType()) {
                case "INCREASING":
                    context.addGeneralCondition("INCREASING");
                    System.out.println("ProblemInterpreter: Added INCREASING condition");
                    break;
                case "DECREASING":
                    context.addGeneralCondition("DECREASING");
                    System.out.println("ProblemInterpreter: Added DECREASING condition");
                    break;
                case "PALINDROME":
                    context.addGeneralCondition("PALINDROME");
                    System.out.println("ProblemInterpreter: Added PALINDROME condition");
                    break;
            }
        }
    }

    private static void interpretDivisibilityCondition(ASTNode node, ProblemContext context) {
        System.out.println("ProblemInterpreter: Interpreting DIVISIBILITY_CONDITION");

        StringBuilder condition = new StringBuilder();

        for (ASTNode child : node.getChildren()) {
            if (child.getValue() != null) {
                condition.append(child.getValue()).append(" ");
            }
        }

        if (condition.length() > 0) {
            String conditionStr = condition.toString().trim();
            context.addGeneralCondition(conditionStr);
            System.out.println("ProblemInterpreter: Added divisibility condition: " + conditionStr);
        }
    }
}