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
 * Упрощенный парсер для карточных задач
 */
public class CardParser {

    private ProblemContext lastContext;

    /**
     * Основной метод парсинга карточной задачи
     */
    public String parse(String taskTitle, String taskText) {
        String cdslCode = analyzeAndConvertToCDSL(taskText);

        try {
            List<Token> tokens = CDSLTokenizer.tokenize(cdslCode);

            CDSLParser parser = new CDSLParser(tokens);
            ASTNode ast = parser.parse();

            ProblemContext context = ProblemInterpreter.interpret(ast);
            this.lastContext = context;

        } catch (Exception e) {
        }

        return cdslCode;
    }

    /**
     * Анализирует текст задачи и преобразует в CDSL синтаксис
     */
    private String analyzeAndConvertToCDSL(String text) {
        StringBuilder cdsl = new StringBuilder();

        cdsl.append("TASK CARDS \"").append(extractTaskName(text)).append("\"\n");

        int deckSize = extractDeckSize(text);
        cdsl.append("DECK STANDARD ").append(deckSize).append("\n");

        int drawCount = extractDrawCount(text);
        cdsl.append("DRAW ").append(drawCount).append(" ");

        List<CardInfo> targetCards = extractTargetCards(text);
        if (!targetCards.isEmpty()) {
            cdsl.append("TARGET [");
            for (int i = 0; i < targetCards.size(); i++) {
                if (i > 0) cdsl.append(", ");
                CardInfo card = targetCards.get(i);
                cdsl.append(card.rank).append(" ").append(card.suit);
            }
            cdsl.append("]\n");
        }

        cdsl.append("CALCULATE PROBABILITY");

        return cdsl.toString();
    }

    private List<CardInfo> extractTargetCards(String text) {
        List<CardInfo> cards = new ArrayList<>();
        String lowerText = text.toLowerCase();

        String[][] allCards = {
                {"туз червей", "туз черв", "туз ♥", "ACE", "HEARTS"},
                {"туз бубен", "туз бубей", "туз буб", "туз ♦", "ACE", "DIAMONDS"},
                {"туз пик", "туз пик", "туз ♠", "ACE", "SPADES"},
                {"туз треф", "туз треф", "туз ♣", "ACE", "CLUBS"},

                {"король червей", "король черв", "король ♥", "KING", "HEARTS"},
                {"король бубен", "король бубей", "король буб", "король ♦", "KING", "DIAMONDS"},
                {"король пик", "король пик", "король ♠", "KING", "SPADES"},
                {"король треф", "король треф", "король ♣", "KING", "CLUBS"},

                {"дама червей", "дама черв", "дама ♥", "QUEEN", "HEARTS"},
                {"дама бубен", "дама бубей", "дама буб", "дама ♦", "QUEEN", "DIAMONDS"},
                {"дама пик", "дама пик", "дама ♠", "QUEEN", "SPADES"},
                {"дама треф", "дама треф", "дама ♣", "QUEEN", "CLUBS"},

                {"валет червей", "валет черв", "валет ♥", "JACK", "HEARTS"},
                {"валет бубен", "валет бубей", "валет буб", "валет ♦", "JACK", "DIAMONDS"},
                {"валет пик", "валет пик", "валет ♠", "JACK", "SPADES"},
                {"валет треф", "валет треф", "валет ♣", "JACK", "CLUBS"},

                {"десятка червей", "10 червей", "десятка черв", "10 черв", "десятка ♥", "10 ♥", "10", "HEARTS"},
                {"десятка бубен", "10 бубен", "десятка бубей", "10 бубей", "десятка буб", "10 буб", "десятка ♦", "10 ♦", "10", "DIAMONDS"},
                {"десятка пик", "10 пик", "десятка пик", "10 пик", "десятка ♠", "10 ♠", "10", "SPADES"},
                {"десятка треф", "10 треф", "десятка треф", "10 треф", "десятка ♣", "10 ♣", "10", "CLUBS"},

                {"девятка червей", "9 червей", "девятка черв", "9 черв", "девятка ♥", "9 ♥", "9", "HEARTS"},
                {"девятка бубен", "9 бубен", "девятка бубей", "9 бубей", "девятка буб", "9 буб", "девятка ♦", "9 ♦", "9", "DIAMONDS"},
                {"девятка пик", "9 пик", "девятка пик", "9 пик", "девятка ♠", "9 ♠", "9", "SPADES"},
                {"девятка треф", "9 треф", "девятка треф", "9 треф", "девятка ♣", "9 ♣", "9", "CLUBS"},

                {"восьмерка червей", "8 червей", "восьмерка черв", "8 черв", "восьмерка ♥", "8 ♥", "8", "HEARTS"},
                {"восьмерка бубен", "8 бубен", "восьмерка бубей", "8 бубей", "восьмерка буб", "8 буб", "восьмерка ♦", "8 ♦", "8", "DIAMONDS"},
                {"восьмерка пик", "8 пик", "восьмерка пик", "8 пик", "восьмерка ♠", "8 ♠", "8", "SPADES"},
                {"восьмерка треф", "8 треф", "восьмерка треф", "8 треф", "восьмерка ♣", "8 ♣", "8", "CLUBS"},

                {"семерка червей", "7 червей", "семерка черв", "7 черв", "семерка ♥", "7 ♥", "7", "HEARTS"},
                {"семерка бубен", "7 бубен", "семерка бубей", "7 бубей", "семерка буб", "7 буб", "семерка ♦", "7 ♦", "7", "DIAMONDS"},
                {"семерка пик", "7 пик", "семерка пик", "7 пик", "семерка ♠", "7 ♠", "7", "SPADES"},
                {"семерка треф", "7 треф", "семерка треф", "7 треф", "семерка ♣", "7 ♣", "7", "CLUBS"},

                {"шестерка червей", "6 червей", "шестерка черв", "6 черв", "шестерка ♥", "6 ♥", "6", "HEARTS"},
                {"шестерка бубен", "6 бубен", "шестерка бубей", "6 бубей", "шестерка буб", "6 буб", "шестерка ♦", "6 ♦", "6", "DIAMONDS"},
                {"шестерка пик", "6 пик", "шестерка пик", "6 пик", "шестерка ♠", "6 ♠", "6", "SPADES"},
                {"шестерка треф", "6 треф", "шестерка треф", "6 треф", "шестерка ♣", "6 ♣", "6", "CLUBS"},

                {"пятерка червей", "5 червей", "пятерка черв", "5 черв", "пятерка ♥", "5 ♥", "5", "HEARTS"},
                {"пятерка бубен", "5 бубен", "пятерка бубей", "5 бубей", "пятерка буб", "5 буб", "пятерка ♦", "5 ♦", "5", "DIAMONDS"},
                {"пятерка пик", "5 пик", "пятерка пик", "5 пик", "пятерка ♠", "5 ♠", "5", "SPADES"},
                {"пятерка треф", "5 треф", "пятерка треф", "5 треф", "пятерка ♣", "5 ♣", "5", "CLUBS"},

                {"четверка червей", "4 червей", "четверка черв", "4 черв", "четверка ♥", "4 ♥", "4", "HEARTS"},
                {"четверка бубен", "4 бубен", "четверка бубей", "4 бубей", "четверка буб", "4 буб", "четверка ♦", "4 ♦", "4", "DIAMONDS"},
                {"четверка пик", "4 пик", "четверка пик", "4 пик", "четверка ♠", "4 ♠", "4", "SPADES"},
                {"четверка треф", "4 треф", "четверка треф", "4 треф", "четверка ♣", "4 ♣", "4", "CLUBS"},

                {"тройка червей", "3 червей", "тройка черв", "3 черв", "тройка ♥", "3 ♥", "3", "HEARTS"},
                {"тройка бубен", "3 бубен", "тройка бубей", "3 бубей", "тройка буб", "3 буб", "тройка ♦", "3 ♦", "3", "DIAMONDS"},
                {"тройка пик", "3 пик", "тройка пик", "3 пик", "тройка ♠", "3 ♠", "3", "SPADES"},
                {"тройка треф", "3 треф", "тройка треф", "3 треф", "тройка ♣", "3 ♣", "3", "CLUBS"},

                {"двойка червей", "2 червей", "двойка черв", "2 черв", "двойка ♥", "2 ♥", "2", "HEARTS"},
                {"двойка бубен", "2 бубен", "двойка бубей", "2 бубей", "двойка буб", "2 буб", "двойка ♦", "2 ♦", "2", "DIAMONDS"},
                {"двойка пик", "2 пик", "двойка пик", "2 пик", "двойка ♠", "2 ♠", "2", "SPADES"},
                {"двойка треф", "2 треф", "двойка треф", "2 треф", "двойка ♣", "2 ♣", "2", "CLUBS"}
        };

        for (String[] cardInfo : allCards) {
            for (int i = 0; i < cardInfo.length - 2; i++) {
                if (lowerText.contains(cardInfo[i])) {
                    String rank = cardInfo[cardInfo.length - 2];
                    String suit = cardInfo[cardInfo.length - 1];
                    cards.add(new CardInfo(rank, suit));
                    break;
                }
            }
        }

        if (cards.isEmpty()) {
            if (lowerText.contains("туз") && !lowerText.contains("не туз")) {
                cards.add(new CardInfo("ACE", getRandomSuit()));
            }
            if (lowerText.contains("корол") && !lowerText.contains("не корол")) {
                cards.add(new CardInfo("KING", getRandomSuit()));
            }
            if (lowerText.contains("дам") && !lowerText.contains("не дам")) {
                cards.add(new CardInfo("QUEEN", getRandomSuit()));
            }
            if (lowerText.contains("валет") && !lowerText.contains("не валет")) {
                cards.add(new CardInfo("JACK", getRandomSuit()));
            }
            if (lowerText.contains("десятк") && !lowerText.contains("не десятк")) {
                cards.add(new CardInfo("10", getRandomSuit()));
            }
            if (lowerText.contains("девятк") && !lowerText.contains("не девятк")) {
                cards.add(new CardInfo("9", getRandomSuit()));
            }
            if (lowerText.contains("восьмерк") && !lowerText.contains("не восьмерк")) {
                cards.add(new CardInfo("8", getRandomSuit()));
            }
            if (lowerText.contains("семерк") && !lowerText.contains("не семерк")) {
                cards.add(new CardInfo("7", getRandomSuit()));
            }
        }

        int drawCount = extractDrawCount(text);
        if (cards.size() > drawCount) {
            cards = cards.subList(0, drawCount);
        }

        return cards;
    }

    /**
     * Возвращает случайную масть
     */
    private String getRandomSuit() {
        String[] suits = {"HEARTS", "DIAMONDS", "SPADES", "CLUBS"};
        return suits[new Random().nextInt(suits.length)];
    }

    /**
     * Вспомогательный класс для хранения информации о карте
     */
    private static class CardInfo {
        String rank;
        String suit;

        CardInfo(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }
    }


    /**
     * Извлекает размер колоды
     */
    private int extractDeckSize(String text) {
        Pattern pattern = Pattern.compile("(\\d+)\\s*карт");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String number = matcher.group(1);
            if (text.contains("колод") || text.contains("в " + number + " карт")) {
                try {
                    return Integer.parseInt(number);
                } catch (NumberFormatException e) {
                }
            }
        }

        if (text.contains("36 карт")) return 36;
        if (text.contains("52 карт")) return 52;
        if (text.contains("54 карт")) return 54;

        return 36;
    }

    /**
     * Извлекает количество вытягиваемых карт (размер набора)
     */
    private int extractDrawCount(String text) {
        String lowerText = text.toLowerCase();

        Pattern pattern = Pattern.compile("случайным образом (\\d+) карт");
        Matcher matcher = pattern.matcher(lowerText);

        if (matcher.find()) {
            String numberStr = matcher.group(1);
            try {
                return Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
            }
        }

        return 5;
    }

    /**
     * Извлекает название задачи
     */
    private String extractTaskName(String text) {
        return "Card task";
    }

    /**
     *  Печатает AST (закомментировано)
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

}