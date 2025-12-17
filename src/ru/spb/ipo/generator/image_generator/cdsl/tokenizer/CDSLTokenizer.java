package ru.spb.ipo.generator.image_generator.cdsl.tokenizer;

import java.util.*;
import java.util.regex.*;

/**
 * Лексический анализатор (токенизатор) для языка CDSL.
 *
 * Преобразует исходный текст на языке CDSL в последовательность токенов (лексем).
 * Каждый токен представляет собой минимальную значимую единицу языка.
 *
 * Основные функции:
 * 1. Разбиение текста на строки
 * 2. Распознавание лексем с помощью регулярных выражений
 * 3. Определение типа каждой лексемы
 * 4. Учет позиции лексемы в исходном тексте
 *
 * Работает по принципу конечного автомата, анализируя текст последовательно,
 * символ за символом, и группируя символы в токены на основе паттернов.
 */
public class CDSLTokenizer {

    /**
     * Основной метод токенизации. Преобразует входной текст в список токенов.
     *
     * @param input Исходный текст на языке CDSL
     * @return Список объектов Token, представляющих лексемы текста
     */
    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();

        // Проверка на пустой вход
        if (input == null || input.trim().isEmpty()) {
            return tokens;
        }

        // Разбиваем текст на строки для учета номеров строк
        String[] lines = input.split("\n");
        int lineNumber = 1; // Нумерация строк начинается с 1

        // Обрабатываем каждую строку отдельно
        for (String line : lines) {
            // Токенизируем каждую строку и добавляем токены в общий список
            tokens.addAll(tokenizeLine(line.trim(), lineNumber));
            lineNumber++; // Переходим к следующей строке
        }

        // Добавляем специальный токен конца файла (EOF) в конец списка
        if (!tokens.isEmpty()) {
            tokens.add(new Token(TokenType.EOF, "", lineNumber, 1));
        }

        return tokens;
    }

    /**
     * Токенизирует одну строку текста.
     *
     * @param line Строка для токенизации (без начальных и конечных пробелов)
     * @param lineNumber Номер текущей строки в исходном файле
     * @return Список токенов, найденных в строке
     */
    private static List<Token> tokenizeLine(String line, int lineNumber) {
        List<Token> tokens = new ArrayList<>();

        // Пропускаем пустые строки и комментарии (начинающиеся с //)
        if (line.isEmpty() || line.startsWith("//")) {
            return tokens;
        }

        // Для сравнения без учета регистра создаем копию строки в верхнем регистре
        String upperLine = line.toUpperCase();
        int pos = 0; // Текущая позиция в строке
        int column = 1; // Текущая колонка (начинается с 1)

        // Последовательно анализируем строку, пока не достигнем конца
        while (pos < upperLine.length()) {
            boolean matched = false; // Флаг, указывающий, что токен был распознан

            // Пропускаем пробельные символы
            if (Character.isWhitespace(upperLine.charAt(pos))) {
                pos++;
                column++;
                continue;
            }

            // ============================================================
            // ПРАВИЛО 1: Распознавание строк в кавычках
            // ============================================================
            if (line.charAt(pos) == '"') {
                int endQuote = line.indexOf('"', pos + 1);

                if (endQuote != -1) {
                    // Извлекаем строку вместе с кавычками
                    String stringValue = line.substring(pos, endQuote + 1);
                    tokens.add(new Token(TokenType.STRING, stringValue, lineNumber, column));

                    // Перемещаем позицию за закрывающей кавычкой
                    pos = endQuote + 1;
                    column += stringValue.length();
                    matched = true;
                    continue;
                }
            }

            // ============================================================
            // ПРАВИЛО 2: Распознавание позиций цифр [1], [2], и т.д.
            // ============================================================
            if (upperLine.charAt(pos) == '[') {
                int endBracket = upperLine.indexOf(']', pos + 1);

                if (endBracket != -1) {
                    // Извлекаем содержимое между скобками
                    String between = upperLine.substring(pos + 1, endBracket);

                    // Проверяем, состоит ли содержимое только из цифр
                    if (between.matches("\\d+")) {
                        String digitPos = line.substring(pos, endBracket + 1);
                        tokens.add(new Token(TokenType.DIGIT_POSITION, digitPos, lineNumber, column));

                        pos = endBracket + 1;
                        column += digitPos.length();
                        matched = true;
                        continue;
                    }
                }
            }

            // ============================================================
            // ПРАВИЛО 3: Распознавание чисел (последовательность цифр)
            // ============================================================
            if (Character.isDigit(upperLine.charAt(pos))) {
                int start = pos; // Запоминаем начало числа

                // Продвигаемся, пока идут цифры
                while (pos < upperLine.length() && Character.isDigit(upperLine.charAt(pos))) {
                    pos++;
                }

                // Извлекаем число как подстроку из оригинальной строки
                String number = line.substring(start, pos);
                tokens.add(new Token(TokenType.INTEGER, number, lineNumber, column));

                column += number.length();
                matched = true;
                continue;
            }

            // ============================================================
            // ПРАВИЛО 4: Распознавание ключевых слов и многословных токенов
            // ============================================================
            // Получаем упорядоченный список типов токенов (длинные паттерны в начале)
            List<TokenType> orderedTokenTypes = getOrderedTokenTypes();

            for (TokenType tokenType : orderedTokenTypes) {
                // Пропускаем токены, которые уже обработаны другими правилами
                if (tokenType == TokenType.UNKNOWN ||
                        tokenType == TokenType.STRING ||
                        tokenType == TokenType.INTEGER ||
                        tokenType == TokenType.DIGIT_POSITION) {
                    continue;
                }

                // Создаем регулярное выражение для текущего типа токена
                // ^ означает начало подстроки, CASE_INSENSITIVE - без учета регистра
                Pattern pattern = Pattern.compile("^" + tokenType.getPattern(), Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(upperLine.substring(pos));

                if (matcher.find()) {
                    String value = matcher.group(); // Найденное значение в верхнем регистре
                    String originalValue = line.substring(pos, pos + value.length()); // Оригинальное значение

                    // Проверяем, что это отдельное слово/фраза, а не часть другого слова
                    if (isWholeWordOrPhrase(value, upperLine, pos, line)) {
                        tokens.add(new Token(tokenType, originalValue, lineNumber, column));

                        pos += value.length();
                        column += value.length();
                        matched = true;
                        break; // Выходим из цикла, так как токен найден
                    }
                }
            }

            // ============================================================
            // ПРАВИЛО 5: Распознавание отдельных символов и коротких токенов
            // ============================================================
            if (!matched) {
                matched = checkSingleCharacters(line, pos, tokens, lineNumber, column);

                if (matched) {
                    pos++;
                    column++;
                    continue;
                }

                // ============================================================
                // ПРАВИЛО 6: Неизвестные токены (ошибка лексического анализа)
                // ============================================================
                int start = pos; // Начало неизвестной последовательности

                // Собираем последовательность символов до следующего разделителя
                while (pos < upperLine.length() &&
                        !Character.isWhitespace(upperLine.charAt(pos)) &&
                        upperLine.charAt(pos) != '[' && upperLine.charAt(pos) != ']' &&
                        upperLine.charAt(pos) != ',' && upperLine.charAt(pos) != '(' &&
                        upperLine.charAt(pos) != ')' && upperLine.charAt(pos) != ':' &&
                        upperLine.charAt(pos) != ';' && upperLine.charAt(pos) != '"') {
                    pos++;
                }

                if (start < pos) {
                    // Если собрали несколько символов
                    String unknown = line.substring(start, pos);
                    tokens.add(new Token(TokenType.UNKNOWN, unknown, lineNumber, column));
                    column += unknown.length();
                } else {
                    // Если один непонятный символ
                    String unknown = String.valueOf(line.charAt(pos));
                    tokens.add(new Token(TokenType.UNKNOWN, unknown, lineNumber, column));
                    pos++;
                    column++;
                }
            }
        }

        return tokens;
    }

    /**
     * Возвращает упорядоченный список типов токенов для распознавания.
     * Порядок ВАЖЕН: более длинные и специфичные токены должны идти первыми.
     *
     * Это предотвращает ситуации, когда "DECK" распознается раньше "DECK_SIZE",
     * хотя "DECK" - это начало "DECK_SIZE".
     *
     * @return Упорядоченный список типов токенов
     */
    private static List<TokenType> getOrderedTokenTypes() {
        return Arrays.asList(
                // ============================================================
                // ГРУППА 1: Самые длинные и специфичные токены (первыми!)
                // ============================================================
                TokenType.INCREASES_BY_INTEGER,   // "INCREASES_BY_INTEGER" (23 символа)
                TokenType.DECREASES_BY_INTEGER,   // "DECREASES_BY_INTEGER" (23 символа)
                TokenType.INCREASES_BY_FACTOR,    // "INCREASES_BY_FACTOR" (20 символов)
                TokenType.DECREASES_BY_FACTOR,    // "DECREASES_BY_FACTOR" (20 символов)
                TokenType.FORMATION_RULE,         // "FORMATION_RULE" (14 символов)
                TokenType.RESULTING_NUMBER,       // "RESULTING_NUMBER" (16 символов)
                TokenType.CHANGE_RULE,            // "CHANGE_RULE" (11 символов)
                TokenType.TRANSFORMATION,         // "TRANSFORMATION" (15 символов)
                TokenType.NUMBER_LENGTH,          // "NUMBER_LENGTH" (13 символов)
                TokenType.CANNOT_BE_ZERO,         // "CANNOT_BE_ZERO" (14 символов)
                TokenType.SINGLE_DIGIT,           // "SINGLE_DIGIT" (12 символов)
                TokenType.FORM_NUMBER,            // "FORM_NUMBER" (11 символов)
                TokenType.ALLOW_DUPLICATES,       // "ALLOW_DUPLICATES" (16 символов)

                // ============================================================
                // ГРУППА 2: Токены для задач с числами (NUMBERS)
                // ============================================================
                TokenType.MAX_DIGIT,              // "MAX_DIGIT" (9 символов)
                TokenType.FIRST_NOT_ZERO,         // "FIRST_NOT_ZERO" (14 символов)
                TokenType.ADJACENT_DIFFERENT,     // "ADJACENT_DIFFERENT" (18 символов)
                TokenType.NON_DECREASING,         // "NON_DECREASING" (14 символов)
                TokenType.NON_INCREASING,         // "NON_INCREASING" (14 символов)
                TokenType.COMPARE,                // "COMPARE" (7 символов)
                TokenType.RANGE_START,            // "RANGE_START" (11 символов)
                TokenType.RANGE_END,              // "RANGE_END" (9 символов)
                TokenType.TOTAL,                  // "TOTAL" (5 символов)

                // ============================================================
                // ГРУППА 3: Средние по длине токены (команды и параметры)
                // ============================================================
                TokenType.DIVISIBILITY,           // "DIVISIBILITY" (12 символов)
                TokenType.REMAINDERS,             // "REMAINDERS" (10 символов)
                TokenType.EQUATIONS,              // "EQUATIONS" (9 символов)
                TokenType.CALCULATE,              // "CALCULATE" (9 символов)
                TokenType.BOARD_HEIGHT,           // "BOARD_HEIGHT" (12 символов)
                TokenType.BOARD_WIDTH,            // "BOARD_WIDTH" (11 символов)
                TokenType.COEFFICIENTS,           // "COEFFICIENTS" (12 символов)
                TokenType.CONSTRAINTS,            // "CONSTRAINTS" (11 символов)
                TokenType.REPLACEMENT,            // "REPLACEMENT" (11 символов)
                TokenType.NO_REPLACEMENT,         // "NO_REPLACEMENT" (14 символов)
                TokenType.DRAW_COUNT,             // "DRAW_COUNT" (10 символов)
                TokenType.DIVIDES_BY,             // "делится на" или "divisible by"
                TokenType.GREATER_EQUAL,          // ">=" (2 символа, но специальный)
                TokenType.LESS_EQUAL,             // "<=" (2 символа, но специальный)
                TokenType.NOT_EQUALS,             // "!=" (2 символа, но специальный)

                // ============================================================
                // ГРУППА 4: Типы расчетов
                // ============================================================
                TokenType.PROBABILITY,            // "PROBABILITY" (11 символов)
                TokenType.COMBINATIONS,           // "COMBINATIONS" (12 символов)
                TokenType.EXPECTATION,            // "EXPECTATION" (11 символов)

                // ============================================================
                // ГРУППА 5: Основные команды (должны быть после типов задач!)
                // ============================================================
                TokenType.TASK,                   // "TASK" (4 символа)
                TokenType.DECK,                   // "DECK" (4 символа)
                TokenType.TARGET,                 // "TARGET" (6 символов)
                TokenType.DRAW,                   // "DRAW" (4 символа)
                TokenType.CONDITION,              // "CONDITION" (9 символов)

                // ============================================================
                // ГРУППА 6: Типы задач
                // ============================================================
                TokenType.CARDS,                  // "CARDS" (5 символов)
                TokenType.WORDS,                  // "WORDS" (5 символов)
                TokenType.CHESS,                  // "CHESS" (5 символов)
                TokenType.NUMBERS,                // "NUMBERS" (7 символов)
                TokenType.EQUATIONS,              // "EQUATIONS" (9 символов)
                TokenType.BALLS,                  // "BALLS" (5 символов)
                TokenType.DIVISIBILITY,           // "DIVISIBILITY" (12 символов)
                TokenType.REMAINDERS,             // "REMAINDERS" (10 символов)

                // ============================================================
                // ГРУППА 7: Шахматные фигуры (добавляем перед общими командами)
                // ============================================================
                TokenType.CHESS_ROOK,             // "CHESS_ROOK" (10 символов)
                TokenType.CHESS_KNIGHT,           // "CHESS_KNIGHT" (13 символов)
                TokenType.CHESS_BISHOP,           // "CHESS_BISHOP" (13 символов)
                TokenType.CHESS_QUEEN,            // "CHESS_QUEEN" (12 символов)
                TokenType.CHESS_KING,             // "CHESS_KING" (11 символов)
                TokenType.CHESS_PAWN,             // "CHESS_PAWN" (11 символов)

                // ============================================================
                // ГРУППА 8: Шахматные параметры
                // ============================================================
                TokenType.PIECES,                 // "PIECES" (6 символов)
                TokenType.ATTACKING,              // "ATTACKING" (9 символов)
                TokenType.NON_ATTACKING,          // "NON_ATTACKING" (13 символов)

                // ============================================================
                // ГРУППА 9: Типы колод карт
                // ============================================================
                TokenType.STANDARD,               // "STANDARD" (8 символов)
                TokenType.FRENCH,                 // "FRENCH" (6 символов)
                TokenType.SPANISH,                // "SPANISH" (7 символов)
                TokenType.CUSTOM,                 // "CUSTOM" (6 символов)

                // ============================================================
                // ГРУППА 10: Масти карт
                // ============================================================
                TokenType.HEARTS,                 // "HEARTS" (6 символов)
                TokenType.DIAMONDS,               // "DIAMONDS" (8 символов)
                TokenType.CLUBS,                  // "CLUBS" (5 символов)
                TokenType.SPADES,                 // "SPADES" (6 символов)

                // ============================================================
                // ГРУППА 11: Достоинства карт
                // ============================================================
                TokenType.ACE,                    // "ACE" (3 символа)
                TokenType.KING,                   // "KING" (4 символа)
                TokenType.QUEEN,                  // "QUEEN" (5 символов)
                TokenType.JACK,                   // "JACK" (4 символа)
                TokenType.RANK,                   // "RANK" (4 символа)

                // ============================================================
                // ГРУППА 12: Типы условий для карт
                // ============================================================
                TokenType.COUNT,                  // "COUNT" (5 символов)
                TokenType.SUIT,                   // "SUIT" (4 символа)
                TokenType.COLOR,                  // "COLOR" (5 символов)
                TokenType.RANK_TYPE,              // "RANK_TYPE" (9 символов)
                TokenType.RANK_VALUE,             // "RANK_VALUE" (10 символов)
                TokenType.RANK_RANGE,             // "RANK_RANGE" (10 символов)

                // ============================================================
                // ГРУППА 13: Значения для условий карт
                // ============================================================
                TokenType.RED,                    // "RED" (3 символа)
                TokenType.BLACK,                  // "BLACK" (5 символов)
                TokenType.NUMBER,                 // "NUMBER" (6 символов)
                TokenType.FACE,                   // "FACE" (4 символа)
                TokenType.ROYAL,                  // "ROYAL" (5 символов)
                TokenType.LOW,                    // "LOW" (3 символа)
                TokenType.HIGH,                   // "HIGH" (4 символа)
                TokenType.EVEN,                   // "EVEN" (4 символа)
                TokenType.ODD,                    // "ODD" (3 символа)

                // ============================================================
                // ГРУППА 14: Логические операторы
                // ============================================================
                TokenType.AND,                    // "AND" (3 символа)
                TokenType.OR,                     // "OR" (2 символа)
                TokenType.NOT,                    // "NOT" (3 символа)

                // ============================================================
                // ГРУППА 15: Операторы сравнения (кроме >=, <=, !=)
                // ============================================================
                TokenType.EQUALS,                 // "==" или "="
                TokenType.GREATER,                // ">" (1 символ)
                TokenType.LESS,                   // "<" (1 символ)

                // ============================================================
                // ГРУППА 16: Математические операторы
                // ============================================================
                TokenType.PLUS,                   // "+" (1 символ)
                TokenType.MINUS,                  // "-" (1 символ)
                TokenType.MULTIPLY,               // "*" (1 символ)
                TokenType.DIVIDE,                 // "/" (1 символ)
                TokenType.MODULO,                 // "%" (1 символ)

                // ============================================================
                // ГРУППА 17: Параметры задач
                // ============================================================
                TokenType.ALPHABET,               // "ALPHABET" (8 символов)
                TokenType.LENGTH,                 // "LENGTH" (6 символов)
                TokenType.UNIQUE,                 // "UNIQUE" (6 символов)
                TokenType.UNKNOWNS,               // "UNKNOWNS" (8 символов)
                TokenType.SUM,                    // "SUM" (3 символа)
                TokenType.DOMAIN,                 // "DOMAIN" (6 символов)
                TokenType.DIVIDEND,               // "DIVIDEND" (8 символов)
                TokenType.DIVISOR,                // "DIVISOR" (7 символов)
                TokenType.REMAINDER,              // "REMAINDER" (9 символов)
                TokenType.URN,                    // "URN" (3 символа)
                TokenType.CONTENTS,               // "CONTENTS" (8 символов)
                TokenType.DISTINCT,               // "DISTINCT" (8 символов)
                TokenType.ORDER,                  // "ORDER" (5 символов)
                TokenType.ASCENDING,              // "ASCENDING" (9 символов)
                TokenType.DESCENDING,             // "DESCENDING" (10 символов)
                TokenType.DIGITS,                 // "DIGITS" (6 символов)
                TokenType.RULE,                   // "RULE" (4 символа)
                TokenType.FACTOR,                 // "FACTOR" (6 символов)
                TokenType.RESULT,                 // "RESULT" (6 символов)
                TokenType.TIMES,                  // "TIMES" или "РАЗ"

                // ============================================================
                // ГРУППА 18: Цвета шаров
                // ============================================================
                TokenType.BLUE,                   // "BLUE" (4 символа)
                TokenType.GREEN,                  // "GREEN" (5 символов)
                TokenType.WHITE,                  // "WHITE" (5 символов)

                // ============================================================
                // ГРУППА 19: Типы вытягивания
                // ============================================================
                TokenType.SEQUENTIAL,             // "SEQUENTIAL" (10 символов)
                TokenType.SIMULTANEOUS,           // "SIMULTANEOUS" (12 символов)

                // ============================================================
                // ГРУППА 20: Значения для условий
                // ============================================================
                TokenType.UNCHANGED,              // "UNCHANGED" (9 символов)
                TokenType.INCREASING,             // "INCREASING" (10 символов)
                TokenType.DECREASING,             // "DECREASING" (10 символов)

                // ============================================================
                // ГРУППА 21: Переменные
                // ============================================================
                TokenType.VARIABLE,               // "x1", "x2", ..., "x10"

                // ============================================================
                // ГРУППА 22: Булевы значения
                // ============================================================
                TokenType.BOOLEAN,                // "YES", "NO", "TRUE", "FALSE"

                // ============================================================
                // ГРУППА 23: Одиночные цифры (самые короткие, в конце!)
                // ============================================================
                TokenType.DIGIT_VALUE             // "0", "1", ..., "9" (1 символ)
        );
    }

    /**
     * Проверяет, является ли найденная последовательность целым словом или фразой.
     *
     * Это важно для предотвращения частичного совпадения, например,
     * чтобы "CAR" не распозналось как начало "CARDS".
     *
     * @param value Найденная последовательность символов
     * @param upperLine Строка в верхнем регистре для проверки границ
     * @param pos Текущая позиция в строке
     * @param originalLine Оригинальная строка (для отладки)
     * @return true, если это отдельное слово/фраза, false в противном случае
     */
    private static boolean isWholeWordOrPhrase(String value, String upperLine, int pos, String originalLine) {
        // ============================================================
        // ОСОБЫЙ СЛУЧАЙ: Шахматные фигуры без префикса CHESS_
        // ============================================================
        if (value.equals("ROOK") || value.equals("KNIGHT") || value.equals("BISHOP") ||
                value.equals("QUEEN") || value.equals("KING") || value.equals("PAWN")) {
            return isWholeWord(value, upperLine, pos);
        }

        // ============================================================
        // ОСОБЫЙ СЛУЧАЙ: Многословные фразы (содержат пробелы или длинные)
        // ============================================================
        if (value.contains(" ") || value.length() > 15) {
            // Проверяем, что перед фразой нет буквенно-цифровых символов
            if (pos > 0) {
                char prevChar = upperLine.charAt(pos - 1);
                if (Character.isLetterOrDigit(prevChar) || prevChar == '_') {
                    return false; // Это часть более длинного слова
                }
            }

            // Проверяем, что после фразы нет буквенно-цифровых символов
            int endPos = pos + value.length();
            if (endPos < upperLine.length()) {
                char nextChar = upperLine.charAt(endPos);
                if (Character.isLetterOrDigit(nextChar) || nextChar == '_') {
                    return false; // Это часть более длинного слова
                }
            }

            return true; // Это отдельная фраза
        }

        // ============================================================
        // ОБЩИЙ СЛУЧАЙ: Обычные токены
        // ============================================================
        return isWholeWord(value, upperLine, pos);
    }

    /**
     * Проверяет отдельные символы и короткие токены (операторы, скобки и т.д.).
     *
     * @param line Оригинальная строка
     * @param pos Текущая позиция в строке
     * @param tokens Список токенов для добавления
     * @param lineNumber Номер строки
     * @param column Текущая колонка
     * @return true, если символ был распознан, false в противном случае
     */
    private static boolean checkSingleCharacters(String line, int pos, List<Token> tokens,
                                                 int lineNumber, int column) {
        char c = line.charAt(pos); // Текущий символ

        switch (c) {
            // ====================================================
            // СКОБКИ И РАЗДЕЛИТЕЛИ
            // ====================================================
            case '[':
                tokens.add(new Token(TokenType.LBRACKET, "[", lineNumber, column));
                return true;
            case ']':
                tokens.add(new Token(TokenType.RBRACKET, "]", lineNumber, column));
                return true;
            case '(':
                tokens.add(new Token(TokenType.LPAREN, "(", lineNumber, column));
                return true;
            case ')':
                tokens.add(new Token(TokenType.RPAREN, ")", lineNumber, column));
                return true;
            case ',':
                tokens.add(new Token(TokenType.COMMA, ",", lineNumber, column));
                return true;
            case ':':
                tokens.add(new Token(TokenType.COLON, ":", lineNumber, column));
                return true;
            case ';':
                tokens.add(new Token(TokenType.SEMICOLON, ";", lineNumber, column));
                return true;

            // ====================================================
            // ОПЕРАТОРЫ СРАВНЕНИЯ (могут состоять из двух символов)
            // ====================================================
            case '=':
                // Проверяем, не является ли это частью "==", "!=", ">=", "<="
                if (pos + 1 < line.length()) {
                    char nextChar = line.charAt(pos + 1);
                    if (nextChar == '=') {
                        // Это "==" (оператор сравнения)
                        tokens.add(new Token(TokenType.EQUALS, "==", lineNumber, column));
                        return true;
                    }
                }
                // Одиночное "=" (оператор присваивания или сравнения)
                tokens.add(new Token(TokenType.EQUALS, "=", lineNumber, column));
                return true;

            case '!':
                // Проверяем, является ли это частью "!="
                if (pos + 1 < line.length() && line.charAt(pos + 1) == '=') {
                    tokens.add(new Token(TokenType.NOT_EQUALS, "!=", lineNumber, column));
                    return true;
                }
                break; // Одиночный "!" не распознается

            case '>':
                // Проверяем, является ли это частью ">="
                if (pos + 1 < line.length() && line.charAt(pos + 1) == '=') {
                    tokens.add(new Token(TokenType.GREATER_EQUAL, ">=", lineNumber, column));
                    return true;
                }
                // Одиночный ">"
                tokens.add(new Token(TokenType.GREATER, ">", lineNumber, column));
                return true;

            case '<':
                // Проверяем, является ли это частью "<="
                if (pos + 1 < line.length() && line.charAt(pos + 1) == '=') {
                    tokens.add(new Token(TokenType.LESS_EQUAL, "<=", lineNumber, column));
                    return true;
                }
                // Одиночный "<"
                tokens.add(new Token(TokenType.LESS, "<", lineNumber, column));
                return true;

            // ====================================================
            // МАТЕМАТИЧЕСКИЕ ОПЕРАТОРЫ
            // ====================================================
            case '+':
                tokens.add(new Token(TokenType.PLUS, "+", lineNumber, column));
                return true;
            case '-':
                tokens.add(new Token(TokenType.MINUS, "-", lineNumber, column));
                return true;
            case '*':
                tokens.add(new Token(TokenType.MULTIPLY, "*", lineNumber, column));
                return true;
            case '/':
                tokens.add(new Token(TokenType.DIVIDE, "/", lineNumber, column));
                return true;
            case '%':
                tokens.add(new Token(TokenType.MODULO, "%", lineNumber, column));
                return true;
        }

        return false; // Символ не распознан
    }

    /**
     * Проверяет, является ли последовательность целым словом.
     *
     * Целое слово не является частью более длинного слова, т.е.
     * оно отделено от других слов не-буквенно-цифровыми символами.
     *
     * @param value Проверяемая последовательность
     * @param line Строка в верхнем регистре
     * @param pos Позиция начала последовательности
     * @return true, если это целое слово, false в противном случае
     */
    private static boolean isWholeWord(String value, String line, int pos) {
        // Проверяем символ перед словом
        if (pos > 0) {
            char prevChar = line.charAt(pos - 1);
            // Если перед словом буква, цифра или подчеркивание - это часть слова
            if (Character.isLetterOrDigit(prevChar) || prevChar == '_') {
                return false;
            }
        }

        // Проверяем символ после слова
        int endPos = pos + value.length();
        if (endPos < line.length()) {
            char nextChar = line.charAt(endPos);
            // Если после слова буква, цифра или подчеркивание - это часть слова
            if (Character.isLetterOrDigit(nextChar) || nextChar == '_') {
                return false;
            }
        }

        return true; // Слово отделено не-буквенно-цифровыми символами
    }
}