package ru.spb.ipo.generator.image_generator.cdsl.parser;

import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.Token;
import ru.spb.ipo.generator.image_generator.cdsl.tokenizer.TokenType;

import java.util.*;

public class CDSLParser {
    private final List<Token> tokens;
    private int current = 0;

    // В начале метода parse() добавь:

    public CDSLParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parse() {
        ASTNode program = new ASTNode("PROGRAM");

        System.out.println("CDSLParser: Starting parse. Tokens:");
        for (Token token : tokens) {
            System.out.println("  " + token.getType() + ": '" + token.getValue() + "'");
        }

        while (!isAtEnd()) {
            try {
                if (match(TokenType.TASK)) {
                    program.addChild(parseTaskDeclaration());
                } else if (match(TokenType.DECK)) {
                    program.addChild(parseDeckDeclaration());
                }
                // ЗАДАЧИ СО СЛОВАМИ - основные параметры
                else if (match(TokenType.ALPHABET)) {
                    program.addChild(parseAlphabetDeclaration());
                }
                else if (match(TokenType.LENGTH)) {
                    program.addChild(parseLengthDeclaration());
                } else if (match(TokenType.UNIQUE)) {
                    program.addChild(parseUniqueDeclaration());
                }
                // ALLOW_DUPLICATES тоже парсим как UNIQUE
                else if (match(TokenType.ALLOW_DUPLICATES)) {
                    current--; // Возвращаем токен
                    program.addChild(parseUniqueDeclaration());
                }
                // ОБЩИЕ КОМАНДЫ
                else if (match(TokenType.TARGET)) {
                    program.addChild(parseTargetDeclaration());
                } else if (match(TokenType.DRAW)) {
                    program.addChild(parseDrawDeclaration());
                } else if (match(TokenType.CONDITION)) {
                    program.addChild(parseCondition());
                } else if (match(TokenType.CALCULATE)) {
                    program.addChild(parseCalculate());
                }
                // ЗАДАЧИ С УРАВНЕНИЯМИ
                else if (match(TokenType.UNKNOWNS)) {
                    program.addChild(parseUnknownsDeclaration());
                } else if (match(TokenType.SUM)) {
                    program.addChild(parseSumDeclaration());
                } else if (match(TokenType.DOMAIN)) {
                    program.addChild(parseDomainDeclaration());
                } else if (match(TokenType.CONSTRAINTS)) {
                    program.addChild(parseConstraintsDeclaration());
                }
                // ЗАДАЧИ С ЧИСЛАМИ (NUMBERS)
                else if (match(TokenType.NUMBERS) ||
                        (check(TokenType.DIGITS) && checkNext(TokenType.INTEGER) && !checkNextNext(TokenType.DIVIDES_BY))) {
                    program.addChild(parseNumbersDeclaration());
                }
                // ЗАДАЧИ НА ОСТАТКИ
                else if (match(TokenType.DIVIDEND) || match(TokenType.DIVISOR) || match(TokenType.REMAINDER)) {
                    current--;
                    program.addChild(parseRemaindersDeclaration());
                }
                // ЗАДАЧИ НА ДЕЛИМОСТЬ
                else if (match(TokenType.DIVISIBILITY) ||
                        (check(TokenType.DIGITS) && checkNext(TokenType.INTEGER) &&
                                (checkNextNext(TokenType.DIVIDES_BY) || checkNextNext(TokenType.RULE)))) {
                    program.addChild(parseDivisibilityDeclaration());
                }
                // ШАРЫ И УРНЫ
                else if (match(TokenType.URN) || match(TokenType.BALLS)) {
                    program.addChild(parseBallsDeclaration());
                }
                // ШАХМАТНЫЕ ЗАДАЧИ
                else if (match(TokenType.CHESS) || match(TokenType.PIECES) || match(TokenType.BOARD_HEIGHT)) {
                    current--;
                    program.addChild(parseChessDeclaration());
                }
                // АВТОМАТИЧЕСКОЕ ОПРЕДЕЛЕНИЕ ТИПА ЗАДАЧ
                else if (checkUrnContentsStart()) {
                    program.addChild(parseBallsDeclaration());
                } else if (checkColorToken()) {
                    program.addChild(parseSimpleBallsDeclaration());
                }
                // ПРОПУСК НЕИЗВЕСТНЫХ ТОКЕНОВ
                else {
                    Token unknown = advance();
                    System.out.println("CDSLParser: Skipping unknown token: " + unknown.getValue() +
                            " at line " + unknown.getLine());
                }
            } catch (Exception e) {
                System.err.println("CDSLParser: Parse error: " + e.getMessage());
                if (!isAtEnd()) {
                    advance();
                }
            }
        }

        return program;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ОСНОВНЫХ ЗАДАЧ
    // ============================================================

    private ASTNode parseTaskDeclaration() {
        ASTNode node = new ASTNode("TASK_DECLARATION");
        System.out.println("CDSLParser: Parsing TASK declaration");

        if (match(TokenType.CARDS, TokenType.WORDS, TokenType.NUMBERS, TokenType.EQUATIONS,
                TokenType.BALLS, TokenType.DIVISIBILITY, TokenType.REMAINDERS, TokenType.CHESS)) {
            String taskType = previous().getValue();
            node.addChild(new ASTNode("TASK_TYPE", taskType));
            System.out.println("CDSLParser: Task type: " + taskType);
        } else {
            System.err.println("CDSLParser: Unknown task type: " + (isAtEnd() ? "EOF" : peek().getValue()));
            return node;
        }

        if (match(TokenType.STRING)) {
            String taskName = previous().getValue().replaceAll("\"", "");
            node.addChild(new ASTNode("TASK_NAME", taskName));
            System.out.println("CDSLParser: Task name: " + taskName);
        } else {
            node.addChild(new ASTNode("TASK_NAME", ""));
        }

        return node;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ЗАДАЧ СО СЛОВАМИ (WORDS)
    // ============================================================

    private ASTNode parseAlphabetDeclaration() {
        ASTNode node = new ASTNode("ALPHABET_DECLARATION");
        System.out.println("CDSLParser: Parsing ALPHABET declaration");

        if (match(TokenType.STRING)) {
            String alphabet = previous().getValue().replaceAll("\"", "");
            node.addChild(new ASTNode("ALPHABET", alphabet));
            System.out.println("CDSLParser: Alphabet set to: " + alphabet);
        } else {
            System.err.println("CDSLParser: Expected string after ALPHABET");
        }

        return node;
    }

    private ASTNode parseLengthDeclaration() {
        ASTNode node = new ASTNode("LENGTH_DECLARATION");
        System.out.println("CDSLParser: Parsing LENGTH declaration");

        if (match(TokenType.INTEGER)) {
            int length = Integer.parseInt(previous().getValue());
            node.addChild(new ASTNode("LENGTH", length));
            System.out.println("CDSLParser: Word length set to: " + length);
        } else {
            System.err.println("CDSLParser: Expected integer after LENGTH");
        }

        return node;
    }

    private ASTNode parseUniqueDeclaration() {
        ASTNode node = new ASTNode("UNIQUE_DECLARATION");
        System.out.println("CDSLParser: Parsing UNIQUE declaration");

        if (match(TokenType.BOOLEAN)) {
            String boolValue = previous().getValue().toUpperCase();
            boolean isUnique = "YES".equals(boolValue) || "TRUE".equals(boolValue);
            node.addChild(new ASTNode("UNIQUE", isUnique));
            System.out.println("CDSLParser: Unique letters: " + isUnique);
        } else if (match(TokenType.ALLOW_DUPLICATES)) {
            // ALLOW_DUPLICATES означает НЕ уникальные буквы
            node.addChild(new ASTNode("UNIQUE", false));
            System.out.println("CDSLParser: Allow duplicates -> Unique: false");
        } else {
            // По умолчанию - буквы могут повторяться
            node.addChild(new ASTNode("UNIQUE", false));
            System.out.println("CDSLParser: Default Unique: false");
        }

        return node;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ УСЛОВИЙ (включая условия для слов)
    // ============================================================

    private ASTNode parseCondition() {
        ASTNode node = new ASTNode("CONDITION");
        System.out.println("CDSLParser: Parsing CONDITION");

        // Только строки в кавычках
        if (match(TokenType.STRING)) {
            String conditionStr = previous().getValue().replaceAll("\"", "");
            node.addChild(new ASTNode("CONDITION_EXPR", conditionStr));
            System.out.println("CDSLParser: Condition: " + conditionStr);
        }
        // Или если условие написано без кавычек (как одно слово)
        else if (!isAtEnd() && !isNextCommand() && !check(TokenType.SEMICOLON)) {
            StringBuilder conditionBuilder = new StringBuilder();

            // Собираем все токены до конца команды
            while (!isAtEnd() && !isNextCommand() && !check(TokenType.SEMICOLON)) {
                Token token = advance();
                conditionBuilder.append(token.getValue()).append(" ");
            }

            if (conditionBuilder.length() > 0) {
                String condition = conditionBuilder.toString().trim();
                node.addChild(new ASTNode("CONDITION_EXPR", condition));
                System.out.println("CDSLParser: Condition without quotes: " + condition);
            }
        } else {
            System.err.println("CDSLParser: Expected condition expression");
        }

        return node;
    }

    private boolean isKnownWordCondition(String condition) {
        return condition.equals("ПАЛИНДРОМ") ||
                condition.equals("ЧЕРЕДУЮТСЯ ГЛАСНЫЕ И СОГЛАСНЫЕ") ||
                condition.equals("СОГЛАСНАЯ ПЕРЕД ГЛАСНОЙ") ||
                condition.equals("ГЛАСНАЯ ПЕРЕД СОГЛАСНОЙ") ||
                condition.equals("ГЛАСНЫХ БОЛЬШЕ ЧЕМ СОГЛАСНЫХ") ||
                condition.equals("СОГЛАСНЫХ БОЛЬШЕ ЧЕМ ГЛАСНЫХ") ||
                condition.equals("ГЛАСНЫХ СТОЛЬКО ЖЕ СКОЛЬКО СОГЛАСНЫХ") ||
                condition.equals("PALINDROME") ||
                condition.equals("ALTERNATING_VOWELS_CONSONANTS") ||
                condition.equals("CONSONANT_FOLLOWED_BY_VOWEL") ||
                condition.equals("VOWEL_FOLLOWED_BY_CONSONANT") ||
                condition.equals("MORE_VOWELS_THAN_CONSONANTS") ||
                condition.equals("MORE_CONSONANTS_THAN_VOWELS") ||
                condition.equals("EQUAL_VOWELS_CONSONANTS");
    }
    // ============================================================
    // МЕТОДЫ ДЛЯ NUMBERS
    // ============================================================

    private ASTNode parseNumbersDeclaration() {
        ASTNode node = new ASTNode("NUMBERS_DECLARATION");
        System.out.println("CDSLParser: Starting NUMBERS declaration parsing");

        boolean parsing = true;

        while (parsing && !isAtEnd()) {
            System.out.println("CDSLParser: Current token: " + peek().getValue() +
                    " type: " + peek().getType());

            // 1. DIGITS - размер набора
            if (match(TokenType.DIGITS) || match(TokenType.NUMBER_LENGTH)) {
                System.out.println("CDSLParser: Found DIGITS");
                if (match(TokenType.INTEGER)) {
                    int digits = Integer.parseInt(previous().getValue());
                    node.addChild(new ASTNode("DIGITS", digits));
                    System.out.println("CDSLParser: Set DIGITS to: " + digits);
                }
            }

            // 2. MAX_DIGIT - максимальная цифра
            else if (match(TokenType.MAX_DIGIT)) {
                System.out.println("CDSLParser: Found MAX_DIGIT");
                if (match(TokenType.INTEGER)) {
                    int maxDigit = Integer.parseInt(previous().getValue());
                    node.addChild(new ASTNode("MAX_DIGIT", maxDigit));
                    System.out.println("CDSLParser: Set MAX_DIGIT to: " + maxDigit);
                }
            }

            // 3. FIRST_NOT_ZERO - первая цифра не 0
            else if (match(TokenType.FIRST_NOT_ZERO)) {
                System.out.println("CDSLParser: Found FIRST_NOT_ZERO");
                boolean firstNotZeroValue = true; // по умолчанию YES
                if (match(TokenType.BOOLEAN)) {
                    String boolStr = previous().getValue().toUpperCase();
                    firstNotZeroValue = "YES".equals(boolStr) || "TRUE".equals(boolStr);
                }
                node.addChild(new ASTNode("FIRST_NOT_ZERO", firstNotZeroValue));
                System.out.println("CDSLParser: Set FIRST_NOT_ZERO to: " + firstNotZeroValue);
            }

            // 4. DISTINCT - все цифры различны
            else if (match(TokenType.DISTINCT)) {
                System.out.println("CDSLParser: Found DISTINCT");
                boolean distinctValue = true; // по умолчанию YES
                if (match(TokenType.BOOLEAN)) {
                    String boolStr = previous().getValue().toUpperCase();
                    distinctValue = "YES".equals(boolStr) || "TRUE".equals(boolStr);
                }
                node.addChild(new ASTNode("DISTINCT", distinctValue));
                System.out.println("CDSLParser: Set DISTINCT to: " + distinctValue);
            }

            // 5. ADJACENT_DIFFERENT - соседние цифры различны
            else if (match(TokenType.ADJACENT_DIFFERENT)) {
                System.out.println("CDSLParser: Found ADJACENT_DIFFERENT");
                boolean adjacentValue = true; // по умолчанию YES
                if (match(TokenType.BOOLEAN)) {
                    String boolStr = previous().getValue().toUpperCase();
                    adjacentValue = "YES".equals(boolStr) || "TRUE".equals(boolStr);
                }
                node.addChild(new ASTNode("ADJACENT_DIFFERENT", adjacentValue));
                System.out.println("CDSLParser: Set ADJACENT_DIFFERENT to: " + adjacentValue);
            }

            // 6. ORDER - порядок цифр
            else if (match(TokenType.ORDER)) {
                System.out.println("CDSLParser: Found ORDER");
                if (match(TokenType.ASCENDING, TokenType.DESCENDING,
                        TokenType.NON_DECREASING, TokenType.NON_INCREASING)) {
                    String orderType = previous().getValue();
                    node.addChild(new ASTNode("ORDER", orderType));
                    System.out.println("CDSLParser: Set ORDER to: " + orderType);
                }
            }

            // 7. COMPARE - сравнение сумм комбинаций
            else if (match(TokenType.COMPARE)) {
                System.out.println("CDSLParser: Found COMPARE");

                // Читаем левую часть (первую комбинацию позиций)
                List<String> leftPositions = parsePositionList();
                if (!leftPositions.isEmpty()) {
                    node.addChild(new ASTNode("COMPARE_LEFT", leftPositions));
                    System.out.println("CDSLParser: Compare left: " + leftPositions);
                }

                // Читаем оператор
                if (match(TokenType.LESS, TokenType.GREATER, TokenType.EQUALS,
                        TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL, TokenType.NOT_EQUALS)) {
                    String operator = previous().getValue();
                    node.addChild(new ASTNode("COMPARE_OPERATOR", operator));
                    System.out.println("CDSLParser: Compare operator: " + operator);
                }

                // Читаем правую часть (вторую комбинацию позиций)
                List<String> rightPositions = parsePositionList();
                if (!rightPositions.isEmpty()) {
                    node.addChild(new ASTNode("COMPARE_RIGHT", rightPositions));
                    System.out.println("CDSLParser: Compare right: " + rightPositions);
                }
            }

            // 8. TOTAL - равенство сумм
            else if (match(TokenType.TOTAL)) {
                System.out.println("CDSLParser: Found TOTAL");

                List<String> leftPositions = parsePositionList();
                if (!leftPositions.isEmpty()) {
                    node.addChild(new ASTNode("COMPARE_LEFT", leftPositions));
                    System.out.println("CDSLParser: Total left: " + leftPositions);
                }

                if (match(TokenType.EQUALS)) {
                    node.addChild(new ASTNode("COMPARE_OPERATOR", "="));
                    System.out.println("CDSLParser: Total operator: =");
                }

                List<String> rightPositions = parsePositionList();
                if (!rightPositions.isEmpty()) {
                    node.addChild(new ASTNode("COMPARE_RIGHT", rightPositions));
                    System.out.println("CDSLParser: Total right: " + rightPositions);
                }
            }

            // 9. Конец команды или следующая команда
            else if (match(TokenType.SEMICOLON)) {
                System.out.println("CDSLParser: Found semicolon, ending NUMBERS parsing");
                break;
            }
            else if (isNextCommand()) {
                System.out.println("CDSLParser: Next command found: " +
                        (isAtEnd() ? "EOF" : peek().getType()) + ", stopping NUMBERS parsing");
                parsing = false;
                current--; // Возвращаемся к началу следующей команды
            } else {
                System.out.println("CDSLParser: Skipping token in NUMBERS: " +
                        peek().getValue() + " type: " + peek().getType());
                advance();
            }
        }

        System.out.println("CDSLParser: Finished NUMBERS declaration");
        return node;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ДЕЛИМОСТИ
    // ============================================================

    private ASTNode parseDivisibilityDeclaration() {
        ASTNode node = new ASTNode("DIVISIBILITY_DECLARATION");
        System.out.println("CDSLParser: Starting DIVISIBILITY declaration parsing");

        boolean parsing = true;

        while (parsing && !isAtEnd()) {
            System.out.println("CDSLParser: Current token: " + peek().getValue() +
                    " type: " + peek().getType());

            if (match(TokenType.DIGITS) || match(TokenType.NUMBER_LENGTH)) {
                System.out.println("CDSLParser: Found DIGITS");
                if (match(TokenType.INTEGER)) {
                    int digits = Integer.parseInt(previous().getValue());
                    node.addChild(new ASTNode("DIGITS", digits));
                    System.out.println("CDSLParser: Digits set to: " + digits);
                }
            }
            else if (match(TokenType.RULE) || match(TokenType.FORMATION_RULE)) {
                System.out.println("CDSLParser: Found RULE");
                StringBuilder ruleBuilder = new StringBuilder();
                while (!isAtEnd() && !isNextCommand() && !check(TokenType.SEMICOLON)) {
                    Token token = advance();
                    ruleBuilder.append(token.getValue()).append(" ");
                }
                if (ruleBuilder.length() > 0) {
                    String rule = ruleBuilder.toString().trim();
                    node.addChild(new ASTNode("RULE", rule));
                    System.out.println("CDSLParser: Rule set to: " + rule);
                }
            }
            else if (match(TokenType.DIVIDES_BY)) {
                System.out.println("CDSLParser: Found DIVIDES_BY");
                if (match(TokenType.INTEGER)) {
                    int divisor = Integer.parseInt(previous().getValue());
                    node.addChild(new ASTNode("DIVISOR", divisor));
                    System.out.println("CDSLParser: Divisor set to: " + divisor);
                }
            }
            else if (match(TokenType.SEMICOLON)) {
                System.out.println("CDSLParser: Found semicolon, ending divisibility parsing");
                break;
            }
            else if (isNextCommand()) {
                System.out.println("CDSLParser: Next command found, stopping divisibility parsing");
                parsing = false;
                current--;
            } else {
                System.out.println("CDSLParser: Skipping token in divisibility: " + peek().getValue());
                advance();
            }
        }

        System.out.println("CDSLParser: Finished divisibility declaration");
        return node;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ КАРТ
    // ============================================================

    private ASTNode parseDeckDeclaration() {
        ASTNode node = new ASTNode("DECK_DECLARATION");
        System.out.println("CDSLParser: Parsing DECK declaration");

        if (match(TokenType.STANDARD, TokenType.FRENCH, TokenType.SPANISH, TokenType.CUSTOM)) {
            node.addChild(new ASTNode("DECK_TYPE", previous().getValue()));
            System.out.println("CDSLParser: Deck type: " + previous().getValue());
        } else {
            node.addChild(new ASTNode("DECK_TYPE", "STANDARD"));
            System.out.println("CDSLParser: Default deck type: STANDARD");
        }

        if (match(TokenType.INTEGER)) {
            node.addChild(new ASTNode("DECK_SIZE", Integer.parseInt(previous().getValue())));
            System.out.println("CDSLParser: Deck size: " + previous().getValue());
        } else {
            node.addChild(new ASTNode("DECK_SIZE", 52));
            System.out.println("CDSLParser: Default deck size: 52");
        }

        return node;
    }

    private ASTNode parseTargetDeclaration() {
        ASTNode node = new ASTNode("TARGET_DECLARATION");
        System.out.println("CDSLParser: Parsing TARGET declaration");

        if (match(TokenType.LBRACKET)) {
            node.addChild(parseTargetList());
        } else {
            try {
                if (checkCardComponents()) {
                    node.addChild(parseSingleCard());
                } else if (checkCountCondition()) {
                    node.addChild(parseCountCondition());
                } else {
                    node.addChild(parseSingleCondition());
                }
            } catch (Exception e) {
                System.err.println("CDSLParser: Error parsing target: " + e.getMessage());
                while (!isAtEnd() && !isNextCommand()) {
                    advance();
                }
            }
        }

        return node;
    }

    private ASTNode parseTargetList() {
        ASTNode node = new ASTNode("TARGET_LIST");
        System.out.println("CDSLParser: Parsing target list");

        while (!isAtEnd() && !check(TokenType.RBRACKET)) {
            try {
                if (checkCardComponents()) {
                    node.addChild(parseSingleCard());
                } else if (checkCountCondition()) {
                    node.addChild(parseCountCondition());
                } else {
                    node.addChild(parseSingleCondition());
                }

                if (match(TokenType.COMMA)) {
                    System.out.println("CDSLParser: Found comma in list");
                    continue;
                }
            } catch (Exception e) {
                System.err.println("CDSLParser: Error parsing target in list: " + e.getMessage());
                break;
            }
        }

        if (!match(TokenType.RBRACKET)) {
            System.err.println("CDSLParser: Expected ']' but found: " + (isAtEnd() ? "EOF" : peek().getValue()));
        } else {
            System.out.println("CDSLParser: Closed target list");
        }

        return node;
    }

    private ASTNode parseCountCondition() {
        ASTNode node = new ASTNode("COUNT_CONDITION");
        System.out.println("CDSLParser: Parsing COUNT condition");

        if (!match(TokenType.COUNT) || !match(TokenType.LPAREN)) {
            throw new RuntimeException("CDSLParser: Expected 'COUNT('");
        }

        if (match(TokenType.SUIT, TokenType.COLOR, TokenType.RANK_TYPE, TokenType.RANK, TokenType.RANK_RANGE)) {
            String conditionType = previous().getValue();
            node.addChild(new ASTNode("COUNT_TYPE", conditionType));
            System.out.println("CDSLParser: Count type: " + conditionType);

            if (conditionType.equals("SUIT")) {
                if (match(TokenType.HEARTS, TokenType.DIAMONDS, TokenType.CLUBS, TokenType.SPADES)) {
                    node.addChild(new ASTNode("COUNT_VALUE", previous().getValue()));
                    System.out.println("CDSLParser: Suit value: " + previous().getValue());
                } else {
                    throw new RuntimeException("CDSLParser: Expected suit name after SUIT");
                }
            } else if (conditionType.equals("COLOR")) {
                if (match(TokenType.RED, TokenType.BLACK)) {
                    node.addChild(new ASTNode("COUNT_VALUE", previous().getValue()));
                    System.out.println("CDSLParser: Color value: " + previous().getValue());
                } else {
                    throw new RuntimeException("CDSLParser: Expected RED or BLACK after COLOR");
                }
            } else if (conditionType.equals("RANK_TYPE")) {
                if (match(TokenType.ACE, TokenType.KING, TokenType.QUEEN, TokenType.JACK,
                        TokenType.NUMBER, TokenType.FACE, TokenType.ROYAL, TokenType.LOW,
                        TokenType.HIGH, TokenType.EVEN, TokenType.ODD)) {
                    node.addChild(new ASTNode("COUNT_VALUE", previous().getValue()));
                    System.out.println("CDSLParser: Rank type value: " + previous().getValue());
                } else {
                    throw new RuntimeException("CDSLParser: Expected rank type after RANK_TYPE");
                }
            } else if (conditionType.equals("RANK")) {
                if (match(TokenType.INTEGER) || match(TokenType.ACE, TokenType.KING, TokenType.QUEEN, TokenType.JACK)) {
                    node.addChild(new ASTNode("COUNT_VALUE", previous().getValue()));
                    System.out.println("CDSLParser: Rank value: " + previous().getValue());
                } else {
                    throw new RuntimeException("CDSLParser: Expected rank value after RANK");
                }
            } else if (conditionType.equals("RANK_RANGE")) {
                if (match(TokenType.INTEGER)) {
                    String rangeStart = previous().getValue();
                    if (match(TokenType.INTEGER)) {
                        String rangeEnd = previous().getValue();
                        node.addChild(new ASTNode("COUNT_VALUE", rangeStart + "-" + rangeEnd));
                        System.out.println("CDSLParser: Rank range: " + rangeStart + "-" + rangeEnd);
                    } else {
                        throw new RuntimeException("CDSLParser: Expected range end after RANK_RANGE start");
                    }
                } else {
                    throw new RuntimeException("CDSLParser: Expected range start after RANK_RANGE");
                }
            }
        } else {
            throw new RuntimeException("CDSLParser: Expected condition type (SUIT, COLOR, RANK_TYPE, RANK, RANK_RANGE)");
        }

        if (!match(TokenType.RPAREN)) {
            throw new RuntimeException("CDSLParser: Expected ')' after COUNT condition");
        }

        if (match(TokenType.EQUALS, TokenType.NOT_EQUALS, TokenType.GREATER,
                TokenType.LESS, TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL)) {
            node.addChild(new ASTNode("OPERATOR", previous().getValue()));
            System.out.println("CDSLParser: Operator: " + previous().getValue());
        } else {
            throw new RuntimeException("CDSLParser: Expected comparison operator after COUNT condition");
        }

        if (match(TokenType.INTEGER)) {
            node.addChild(new ASTNode("TARGET_VALUE", Integer.parseInt(previous().getValue())));
            System.out.println("CDSLParser: Target value: " + previous().getValue());
        } else {
            throw new RuntimeException("CDSLParser: Expected integer value after operator");
        }

        return node;
    }

    private ASTNode parseSingleCard() {
        ASTNode node = new ASTNode("CARD");
        System.out.println("CDSLParser: Parsing single card");

        String rank = null;
        if (match(TokenType.RANK, TokenType.ACE, TokenType.KING, TokenType.QUEEN, TokenType.JACK)) {
            rank = normalizeRank(previous().getValue());
            System.out.println("CDSLParser: Card rank: " + rank);
        } else if (match(TokenType.INTEGER)) {
            rank = previous().getValue();
            System.out.println("CDSLParser: Card rank (number): " + rank);
        } else {
            throw new RuntimeException("CDSLParser: Expected card rank, found: " + (isAtEnd() ? "EOF" : peek().getValue()));
        }
        node.addChild(new ASTNode("RANK", rank));

        String suit = null;
        if (match(TokenType.HEARTS, TokenType.DIAMONDS, TokenType.CLUBS, TokenType.SPADES)) {
            suit = normalizeSuit(previous().getValue());
            System.out.println("CDSLParser: Card suit: " + suit);
        } else {
            throw new RuntimeException("CDSLParser: Expected card suit, found: " + (isAtEnd() ? "EOF" : peek().getValue()));
        }
        node.addChild(new ASTNode("SUIT", suit));

        return node;
    }

    private ASTNode parseSingleCondition() {
        ASTNode node = new ASTNode("CONDITION");
        System.out.println("CDSLParser: Parsing single condition");

        if (match(TokenType.PALINDROME, TokenType.ALTERNATING,
                TokenType.CONSONANT_FOLLOWED_BY_VOWEL, TokenType.VOWEL_FOLLOWED_BY_CONSONANT,
                TokenType.MORE_VOWELS_THAN_CONSONANTS, TokenType.MORE_CONSONANTS_THAN_VOWELS,
                TokenType.EQUAL_VOWELS_CONSONANTS)) {
            String conditionType = previous().getValue();
            node.addChild(new ASTNode("CONDITION_TYPE", conditionType));
            System.out.println("CDSLParser: Condition type: " + conditionType);
        } else {
            if (match(TokenType.STRING)) {
                String condition = previous().getValue().replaceAll("\"", "");
                node.addChild(new ASTNode("CONDITION_TYPE", condition));
                System.out.println("CDSLParser: String condition: " + condition);
            } else if (!isAtEnd() && !check(TokenType.RBRACKET) && !check(TokenType.COMMA)) {
                Token token = advance();
                node.addChild(new ASTNode("CONDITION_TYPE", token.getValue()));
                System.out.println("CDSLParser: Token condition: " + token.getValue());
            } else {
                throw new RuntimeException("CDSLParser: Expected condition type");
            }
        }

        return node;
    }

    private ASTNode parseDrawDeclaration() {
        ASTNode node = new ASTNode("DRAW_DECLARATION");
        System.out.println("CDSLParser: Parsing DRAW declaration");

        if (match(TokenType.INTEGER)) {
            node.addChild(new ASTNode("DRAW_COUNT", Integer.parseInt(previous().getValue())));
            System.out.println("CDSLParser: Draw count: " + previous().getValue());
        } else {
            node.addChild(new ASTNode("DRAW_COUNT", 1));
            System.out.println("CDSLParser: Default draw count: 1");
        }

        if (match(TokenType.REPLACEMENT, TokenType.NO_REPLACEMENT)) {
            node.addChild(new ASTNode("REPLACEMENT", previous().getValue()));
            System.out.println("CDSLParser: Replacement: " + previous().getValue());
        } else {
            node.addChild(new ASTNode("REPLACEMENT", "NO_REPLACEMENT"));
            System.out.println("CDSLParser: Default replacement: NO_REPLACEMENT");
        }

        return node;
    }

    private ASTNode parseCalculate() {
        ASTNode node = new ASTNode("CALCULATE");
        System.out.println("CDSLParser: Parsing CALCULATE");

        if (match(TokenType.PROBABILITY, TokenType.COMBINATIONS, TokenType.EXPECTATION)) {
            node.addChild(new ASTNode("CALCULATION_TYPE", previous().getValue()));
            System.out.println("CDSLParser: Calculation type: " + previous().getValue());
        } else {
            node.addChild(new ASTNode("CALCULATION_TYPE", "PROBABILITY"));
            System.out.println("CDSLParser: Default calculation type: PROBABILITY");
        }

        return node;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ УРАВНЕНИЙ
    // ============================================================

    private ASTNode parseUnknownsDeclaration() {
        ASTNode node = new ASTNode("UNKNOWNS_DECLARATION");
        System.out.println("CDSLParser: Parsing UNKNOWNS declaration");

        if (match(TokenType.INTEGER)) {
            int unknownsCount = Integer.parseInt(previous().getValue());
            node.addChild(new ASTNode("UNKNOWNS_COUNT", unknownsCount));
            System.out.println("CDSLParser: Unknowns count: " + unknownsCount);
        } else {
            System.err.println("CDSLParser: Expected integer after UNKNOWNS, found: " + (isAtEnd() ? "EOF" : peek().getValue()));
        }

        return node;
    }

    private ASTNode parseSumDeclaration() {
        ASTNode node = new ASTNode("SUM_DECLARATION");
        System.out.println("CDSLParser: Parsing SUM declaration");

        if (match(TokenType.INTEGER)) {
            int sumValue = Integer.parseInt(previous().getValue());
            node.addChild(new ASTNode("SUM_VALUE", sumValue));
            System.out.println("CDSLParser: Sum value: " + sumValue);
        } else {
            System.err.println("CDSLParser: Expected integer after SUM, found: " + (isAtEnd() ? "EOF" : peek().getValue()));
        }

        return node;
    }

    private ASTNode parseDomainDeclaration() {
        ASTNode node = new ASTNode("DOMAIN_DECLARATION");
        System.out.println("CDSLParser: Parsing DOMAIN declaration");

        if (match(TokenType.STRING)) {
            String domain = previous().getValue().replaceAll("\"", "");
            node.addChild(new ASTNode("DOMAIN", domain));
            System.out.println("CDSLParser: Domain: " + domain);
        } else {
            System.err.println("CDSLParser: Expected string after DOMAIN, found: " + (isAtEnd() ? "EOF" : peek().getValue()));
        }

        return node;
    }

    private ASTNode parseConstraintsDeclaration() {
        ASTNode node = new ASTNode("CONSTRAINTS_DECLARATION");
        System.out.println("CDSLParser: Parsing CONSTRAINTS declaration");

        if (match(TokenType.LBRACKET)) {
            while (!isAtEnd() && !check(TokenType.RBRACKET)) {
                if (match(TokenType.STRING)) {
                    String constraint = previous().getValue().replaceAll("\"", "");
                    ASTNode constraintNode = new ASTNode("CONSTRAINT", constraint);
                    node.addChild(constraintNode);
                    System.out.println("CDSLParser: Constraint: " + constraint);
                }

                if (match(TokenType.COMMA)) {
                    System.out.println("CDSLParser: Found comma in constraints");
                    continue;
                } else if (check(TokenType.RBRACKET)) {
                    break;
                } else if (!isAtEnd()) {
                    advance();
                }
            }

            if (!match(TokenType.RBRACKET)) {
                System.err.println("CDSLParser: Expected ']' after constraints list");
            } else {
                System.out.println("CDSLParser: Closed constraints list");
            }
        } else {
            if (match(TokenType.STRING)) {
                String constraint = previous().getValue().replaceAll("\"", "");
                ASTNode constraintNode = new ASTNode("CONSTRAINT", constraint);
                node.addChild(constraintNode);
                System.out.println("CDSLParser: Single constraint: " + constraint);
            }
        }

        return node;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ШАХМАТ
    // ============================================================

    private ASTNode parseChessDeclaration() {
        ASTNode node = new ASTNode("CHESS_DECLARATION");
        System.out.println("CDSLParser: Starting chess declaration parsing");

        boolean parsing = true;

        while (parsing && !isAtEnd()) {
            System.out.println("CDSLParser: Current token: " + peek().getValue() +
                    " type: " + peek().getType());

            if (match(TokenType.BOARD_HEIGHT)) {
                System.out.println("CDSLParser: Found BOARD_HEIGHT");
                if (match(TokenType.INTEGER)) {
                    int height = Integer.parseInt(previous().getValue());
                    node.addChild(new ASTNode("BOARD_HEIGHT", height));
                    System.out.println("CDSLParser: Board height set to: " + height);
                } else {
                    node.addChild(new ASTNode("BOARD_HEIGHT", 8));
                    System.out.println("CDSLParser: Default board height: 8");
                }
            } else if (match(TokenType.BOARD_WIDTH)) {
                System.out.println("CDSLParser: Found BOARD_WIDTH");
                if (match(TokenType.INTEGER)) {
                    int width = Integer.parseInt(previous().getValue());
                    node.addChild(new ASTNode("BOARD_WIDTH", width));
                    System.out.println("CDSLParser: Board width set to: " + width);
                } else {
                    node.addChild(new ASTNode("BOARD_WIDTH", 8));
                    System.out.println("CDSLParser: Default board width: 8");
                }
            } else if (match(TokenType.PIECES)) {
                System.out.println("CDSLParser: Found PIECES");
                Map<String, Integer> pieces = new HashMap<>();

                if (match(TokenType.LBRACKET)) {
                    System.out.println("CDSLParser: Parsing pieces list");
                    while (!isAtEnd() && !check(TokenType.RBRACKET)) {
                        if (match(TokenType.CHESS_ROOK) || match(TokenType.CHESS_KNIGHT) ||
                                match(TokenType.CHESS_BISHOP) || match(TokenType.CHESS_QUEEN) ||
                                match(TokenType.CHESS_KING) || match(TokenType.CHESS_PAWN) ||
                                match(TokenType.STRING)) {

                            String pieceType = previous().getValue().replaceAll("\"", "");
                            System.out.println("CDSLParser: Found piece type: " + pieceType);

                            // Дополнительная проверка для фигур без префикса
                            if (!pieceType.startsWith("CHESS_") &&
                                    (pieceType.equals("ROOK") || pieceType.equals("KNIGHT") ||
                                            pieceType.equals("BISHOP") || pieceType.equals("QUEEN") ||
                                            pieceType.equals("KING") || pieceType.equals("PAWN"))) {
                                pieceType = "CHESS_" + pieceType;
                            }

                            if (pieceType.startsWith("CHESS_")) {
                                pieceType = pieceType.substring(6);
                            }
                            int count = 1;
                            if (check(TokenType.INTEGER)) {
                                Token countToken = advance();
                                count = Integer.parseInt(countToken.getValue());
                                System.out.println("CDSLParser: Piece count: " + count);
                            }

                            pieces.put(pieceType, pieces.getOrDefault(pieceType, 0) + count);
                            System.out.println("CDSLParser: Added " + pieceType + " x" + count);

                            if (match(TokenType.COMMA)) {
                                System.out.println("CDSLParser: Found comma, continuing...");
                                continue;
                            } else if (check(TokenType.RBRACKET)) {
                                System.out.println("CDSLParser: Found closing bracket, breaking");
                                break;
                            }
                        } else {
                            System.out.println("CDSLParser: Skipping unknown piece token: " + peek().getValue());
                            advance();
                        }
                    }

                    if (match(TokenType.RBRACKET)) {
                        System.out.println("CDSLParser: Pieces map: " + pieces);
                        node.addChild(new ASTNode("PIECES", pieces));
                    } else {
                        System.err.println("CDSLParser: Expected ']' after pieces list");
                    }
                }
            } else if (match(TokenType.ATTACKING, TokenType.NON_ATTACKING)) {
                System.out.println("CDSLParser: Found ATTACKING/NON_ATTACKING");
                boolean attacking = previous().getType() == TokenType.ATTACKING;
                node.addChild(new ASTNode("ATTACKING", attacking));
                System.out.println("CDSLParser: Attacking set to: " + attacking);
            } else if (isNextCommand()) {
                System.out.println("CDSLParser: Next command found, stopping chess parsing");
                parsing = false;
            } else {
                System.out.println("CDSLParser: Skipping unknown chess token: " + peek().getValue());
                advance();
            }
        }

        System.out.println("CDSLParser: Finished chess declaration");
        return node;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ШАРОВ И УРН
    // ============================================================

    private ASTNode parseBallsDeclaration() {
        ASTNode node = new ASTNode("BALLS_DECLARATION");
        System.out.println("CDSLParser: Parsing BALLS declaration");

        if (match(TokenType.URN) || check(TokenType.LBRACKET)) {
            if (previous().getType() != TokenType.URN) {
                current--;
            }

            if (match(TokenType.LBRACKET)) {
                Map<String, Integer> urnContents = parseBallsMap();
                if (!urnContents.isEmpty()) {
                    node.addChild(new ASTNode("URN_CONTENTS", urnContents));
                    System.out.println("CDSLParser: Urn contents: " + urnContents);
                }

                if (!match(TokenType.RBRACKET)) {
                    System.err.println("CDSLParser: Expected ']' after urn contents");
                } else {
                    System.out.println("CDSLParser: Closed urn contents");
                }
            }
        }

        if (match(TokenType.DRAW) || check(TokenType.LBRACKET)) {
            if (previous().getType() != TokenType.DRAW && check(TokenType.LBRACKET)) {
                current--;
            }

            if (match(TokenType.LBRACKET)) {
                Map<String, Integer> drawBalls = parseBallsMap();
                if (!drawBalls.isEmpty()) {
                    node.addChild(new ASTNode("DRAW_BALLS", drawBalls));
                    System.out.println("CDSLParser: Draw balls: " + drawBalls);
                }

                if (!match(TokenType.RBRACKET)) {
                    System.err.println("CDSLParser: Expected ']' after draw balls");
                } else {
                    System.out.println("CDSLParser: Closed draw balls");
                }
            } else if (checkColorToken() || check(TokenType.INTEGER)) {
                Map<String, Integer> drawBalls = parseSimpleBallsList();
                if (!drawBalls.isEmpty()) {
                    node.addChild(new ASTNode("DRAW_BALLS", drawBalls));
                    System.out.println("CDSLParser: Simple draw balls: " + drawBalls);
                }
            }
        }

        if (match(TokenType.SEQUENTIAL, TokenType.SIMULTANEOUS)) {
            boolean sequential = previous().getType() == TokenType.SEQUENTIAL;
            node.addChild(new ASTNode("DRAW_TYPE", sequential));
            System.out.println("CDSLParser: Draw type sequential: " + sequential);
        }

        if (match(TokenType.INTEGER) && !isNextCommand()) {
            int drawCount = Integer.parseInt(previous().getValue());
            node.addChild(new ASTNode("DRAW_COUNT", drawCount));
            System.out.println("CDSLParser: Draw count: " + drawCount);
        }

        return node;
    }

    private ASTNode parseSimpleBallsDeclaration() {
        ASTNode node = new ASTNode("BALLS_DECLARATION");
        System.out.println("CDSLParser: Parsing simple BALLS declaration");

        Map<String, Integer> urnContents = parseSimpleBallsList();
        if (!urnContents.isEmpty()) {
            node.addChild(new ASTNode("URN_CONTENTS", urnContents));
            System.out.println("CDSLParser: Simple urn contents: " + urnContents);
        }

        if (match(TokenType.DRAW)) {
            Map<String, Integer> drawBalls = parseSimpleBallsList();
            if (!drawBalls.isEmpty()) {
                node.addChild(new ASTNode("DRAW_BALLS", drawBalls));
                System.out.println("CDSLParser: Simple draw balls: " + drawBalls);
            }
        }

        if (match(TokenType.SEQUENTIAL, TokenType.SIMULTANEOUS)) {
            boolean sequential = previous().getType() == TokenType.SEQUENTIAL;
            node.addChild(new ASTNode("DRAW_TYPE", sequential));
            System.out.println("CDSLParser: Simple draw type sequential: " + sequential);
        }

        return node;
    }

    private Map<String, Integer> parseBallsMap() {
        Map<String, Integer> ballsMap = new HashMap<>();
        System.out.println("CDSLParser: Parsing balls map");

        while (!isAtEnd() && !check(TokenType.RBRACKET)) {
            if (checkColorToken()) {
                Token colorToken = advance();
                String color = colorToken.getValue();
                System.out.println("CDSLParser: Ball color: " + color);

                int count = 1;
                if (check(TokenType.INTEGER)) {
                    Token countToken = advance();
                    count = Integer.parseInt(countToken.getValue());
                    System.out.println("CDSLParser: Ball count: " + count);
                }

                ballsMap.put(color, ballsMap.getOrDefault(color, 0) + count);
                System.out.println("CDSLParser: Added " + color + " x" + count);

                if (match(TokenType.COMMA)) {
                    System.out.println("CDSLParser: Found comma, continuing...");
                    continue;
                } else if (check(TokenType.RBRACKET)) {
                    System.out.println("CDSLParser: Found closing bracket, breaking");
                    break;
                }
            } else {
                System.out.println("CDSLParser: Skipping non-color token: " + peek().getValue());
                advance();
            }
        }

        return ballsMap;
    }

    private Map<String, Integer> parseSimpleBallsList() {
        Map<String, Integer> ballsMap = new HashMap<>();
        System.out.println("CDSLParser: Parsing simple balls list");

        while (!isAtEnd() && (checkColorToken() || check(TokenType.INTEGER))) {
            if (checkColorToken()) {
                Token colorToken = advance();
                String color = colorToken.getValue();
                System.out.println("CDSLParser: Simple ball color: " + color);

                int count = 1;
                if (check(TokenType.INTEGER)) {
                    Token countToken = advance();
                    count = Integer.parseInt(countToken.getValue());
                    System.out.println("CDSLParser: Simple ball count: " + count);
                }

                ballsMap.put(color, ballsMap.getOrDefault(color, 0) + count);
                System.out.println("CDSLParser: Added simple " + color + " x" + count);
            } else {
                System.out.println("CDSLParser: Breaking simple balls list");
                break;
            }
        }

        return ballsMap;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ОСТАТКОВ
    // ============================================================

    private ASTNode parseRemaindersDeclaration() {
        ASTNode node = new ASTNode("REMAINDERS_DECLARATION");
        System.out.println("CDSLParser: Starting remainders declaration parsing");

        boolean parsing = true;

        while (parsing && !isAtEnd()) {
            System.out.println("CDSLParser: Current token: " + peek().getValue() +
                    " type: " + peek().getType());

            if (match(TokenType.DIVIDEND)) {
                System.out.println("CDSLParser: Found DIVIDEND token");
                if (match(TokenType.STRING)) {
                    String dividend = previous().getValue().replaceAll("\"", "");
                    node.addChild(new ASTNode("DIVIDEND", dividend));
                    System.out.println("CDSLParser: Added DIVIDEND: " + dividend);
                } else if (match(TokenType.INTEGER) || match(TokenType.VARIABLE)) {
                    String dividend = previous().getValue();
                    node.addChild(new ASTNode("DIVIDEND", dividend));
                    System.out.println("CDSLParser: Added DIVIDEND: " + dividend);
                } else {
                    node.addChild(new ASTNode("DIVIDEND", "X"));
                    System.out.println("CDSLParser: Added default DIVIDEND: X");
                }
            }
            else if (match(TokenType.DIVISOR)) {
                System.out.println("CDSLParser: Found DIVISOR token");
                if (match(TokenType.INTEGER)) {
                    int divisor = Integer.parseInt(previous().getValue());
                    node.addChild(new ASTNode("DIVISOR", divisor));
                    System.out.println("CDSLParser: Added DIVISOR: " + divisor);
                } else {
                    node.addChild(new ASTNode("DIVISOR", 1));
                    System.out.println("CDSLParser: Added default DIVISOR: 1");
                }
            }
            else if (match(TokenType.REMAINDER)) {
                System.out.println("CDSLParser: Found REMAINDER token");
                if (match(TokenType.INTEGER)) {
                    int remainder = Integer.parseInt(previous().getValue());
                    node.addChild(new ASTNode("REMAINDER", remainder));
                    System.out.println("CDSLParser: Added REMAINDER: " + remainder);
                } else {
                    node.addChild(new ASTNode("REMAINDER", 0));
                    System.out.println("CDSLParser: Added default REMAINDER: 0");
                }
            }
            else if (isNextCommand()) {
                System.out.println("CDSLParser: Next command found, stopping remainders parsing");
                parsing = false;
                current--;
            }
            else {
                System.out.println("CDSLParser: Skipping token in remainders: " + peek().getValue());
                advance();
            }
        }

        System.out.println("CDSLParser: Finished remainders declaration");
        return node;
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private List<String> parsePositionList() {
        List<String> positions = new ArrayList<>();
        System.out.println("CDSLParser: Parsing position list");

        while (!isAtEnd() && check(TokenType.DIGIT_POSITION)) {
            Token posToken = advance();
            positions.add(posToken.getValue()); // например "[1]"
            System.out.println("CDSLParser: Added position: " + posToken.getValue());
        }

        return positions;
    }

    private boolean checkUrnContentsStart() {
        int savePos = current;
        try {
            return check(TokenType.LBRACKET) && checkNextColorToken();
        } finally {
            current = savePos;
        }
    }

    private boolean checkColorToken() {
        return check(TokenType.RED) || check(TokenType.BLUE) || check(TokenType.GREEN) ||
                check(TokenType.WHITE) || check(TokenType.BLACK);
    }

    private boolean checkNextColorToken() {
        if (current + 1 >= tokens.size()) return false;
        TokenType nextType = tokens.get(current + 1).getType();
        return nextType == TokenType.RED || nextType == TokenType.BLUE ||
                nextType == TokenType.GREEN || nextType == TokenType.WHITE ||
                nextType == TokenType.BLACK;
    }

    private boolean checkCardComponents() {
        int savePos = current;
        try {
            return (check(TokenType.RANK) || check(TokenType.ACE) || check(TokenType.KING) ||
                    check(TokenType.QUEEN) || check(TokenType.JACK) || check(TokenType.INTEGER)) &&
                    (checkNext(TokenType.HEARTS) || checkNext(TokenType.DIAMONDS) ||
                            checkNext(TokenType.CLUBS) || checkNext(TokenType.SPADES));
        } finally {
            current = savePos;
        }
    }

    private boolean checkCountCondition() {
        int savePos = current;
        try {
            return check(TokenType.COUNT) && checkNext(TokenType.LPAREN);
        } finally {
            current = savePos;
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).getType() == type;
    }

    private boolean checkNextNext(TokenType type) {
        if (current + 2 >= tokens.size()) return false;
        return tokens.get(current + 2).getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isNextCommand() {
        return check(TokenType.TASK) || check(TokenType.DECK) || check(TokenType.ALPHABET) ||
                check(TokenType.LENGTH) || check(TokenType.UNIQUE) || check(TokenType.TARGET) ||
                check(TokenType.DRAW) || check(TokenType.CONDITION) || check(TokenType.CALCULATE) ||
                check(TokenType.UNKNOWNS) || check(TokenType.SUM) || check(TokenType.DOMAIN) ||
                check(TokenType.CONSTRAINTS) ||

                check(TokenType.DIVISIBILITY) || check(TokenType.URN) ||
                check(TokenType.BALLS) || check(TokenType.NUMBERS) || check(TokenType.CHESS) ||
                check(TokenType.PIECES) || check(TokenType.BOARD_HEIGHT) || check(TokenType.BOARD_WIDTH);
    }

    private String normalizeRank(String rank) {
        return switch (rank.toUpperCase()) {
            case "A" -> "ACE";
            case "K" -> "KING";
            case "Q" -> "QUEEN";
            case "J" -> "JACK";
            default -> rank;
        };
    }

    private String normalizeSuit(String suit) {
        return switch (suit.toUpperCase()) {
            case "H" -> "HEARTS";
            case "D" -> "DIAMONDS";
            case "C" -> "CLUBS";
            case "S" -> "SPADES";
            default -> suit;
        };
    }
}