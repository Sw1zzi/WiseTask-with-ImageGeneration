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
 * Парсер для задач со словами (WORDS) из человеческого текста
 */
public class WordsParser {

    private ProblemContext lastContext;

    /**
     * Основной метод парсинга задачи со словами
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

            // 5. Дополнительно заполняем контекст извлеченными данными
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
     * Анализирует текст задачи и преобразует в CDSL синтаксис
     */
    private String analyzeAndConvertToCDSL(String text) {
        StringBuilder cdsl = new StringBuilder();

        // 1. TASK WORDS "Название"
        cdsl.append("TASK WORDS \"").append(extractTaskName(text)).append("\"\n");

        // 2. ALPHABET "<строка>"
        String alphabet = extractAlphabet(text);
        cdsl.append("ALPHABET \"").append(alphabet).append("\"\n");

        // 3. LENGTH <n>
        int length = extractWordLength(text);
        cdsl.append("LENGTH ").append(length).append("\n");

        // 4. UNIQUE YES|NO
        boolean uniqueLetters = extractUniqueLetters(text);
        cdsl.append("UNIQUE ").append(uniqueLetters ? "YES" : "NO").append("\n");

        // 5. TARGET [условия]
        List<String> conditions = extractConditions(text);
        if (!conditions.isEmpty() && !conditions.contains("NONE")) {
            cdsl.append("TARGET [");
            for (int i = 0; i < conditions.size(); i++) {
                if (i > 0) cdsl.append(", ");
                cdsl.append(conditions.get(i));
            }
            cdsl.append("]\n");
        }

        // 6. CALCULATE COMBINATIONS или CALCULATE COUNT
        String calculateType = determineCalculateType(text);
        cdsl.append("CALCULATE ").append(calculateType);

        return cdsl.toString();
    }

    /**
     * Извлекает алфавит из текста задачи
     */
    private String extractAlphabet(String text) {
        // Паттерны для поиска алфавита
        Pattern[] patterns = {
                // "алфавит A = {а, в, д }"
                Pattern.compile("алфавит[а]?\\s+[A-ZА-Я]?\\s*=\\s*\\{([^}]+)\\}"),
                Pattern.compile("\\{([^}]+)\\}\\s*\\)"), // {...})
                Pattern.compile("букв[аы]?\\s+([А-Яа-я,\\s]+)"),
                Pattern.compile("из\\s+букв\\s+([А-Яа-я,\\s]+)"),
                Pattern.compile("использу[а-я]+\\s+([А-Яа-я,\\s]+)"),
                Pattern.compile("состав[а-я]+\\s+из\\s+([А-Яа-я,\\s]+)")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find() && matcher.group(1) != null) {
                String letters = matcher.group(1);
                // Очищаем строку от лишних символов
                letters = letters.replaceAll("[,\\s{}]", "").trim();

                if (!letters.isEmpty()) {
                    return letters.toUpperCase();
                }
            }
        }

        // Альтернативный поиск: ищем конкретные буквы в тексте
        Set<Character> foundLetters = new TreeSet<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c >= 'А' && c <= 'Я') || (c >= 'а' && c <= 'я')) {
                foundLetters.add(Character.toUpperCase(c));
            }
        }

        if (!foundLetters.isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (Character c : foundLetters) {
                result.append(c);
            }
            return result.toString();
        }

        // По умолчанию: русский алфавит (сокращенный)
        return "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    }

    /**
     * Извлекает длину слова из текста задачи
     */
    private int extractWordLength(String text) {
        String lowerText = text.toLowerCase();

        // Паттерны для поиска длины слова
        Pattern[] patterns = {
                // "слов длины 5"
                Pattern.compile("слов[ао]?\\s+длины\\s+(\\d+)"),
                Pattern.compile("длин[аы]?\\s+(\\d+)\\s+слов"),
                Pattern.compile("(\\d+)-буквенн[а-я]+"),
                Pattern.compile("(\\d+)-значн[а-я]+"),
                Pattern.compile("(\\d+)\\s+букв[аы]?\\b"),

                // "слова длины n" (общий паттерн)
                Pattern.compile("длины\\s+(\\d+)\\s+в\\s+алфавит"),
                Pattern.compile("слов[ао]?\\s+из\\s+(\\d+)\\s+букв")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                try {
                    int length = Integer.parseInt(matcher.group(1));
                    if (length > 0 && length <= 20) { // разумные пределы
                        return length;
                    }
                } catch (NumberFormatException e) {
                    // continue
                }
            }
        }

        // Проверяем словесные обозначения
        if (lowerText.contains("однобуквенн") || lowerText.contains("1 букв")) return 1;
        if (lowerText.contains("двухбуквенн") || lowerText.contains("2 букв") || lowerText.contains("двубуквенн")) return 2;
        if (lowerText.contains("трехбуквенн") || lowerText.contains("3 букв") || lowerText.contains("трёхбуквенн")) return 3;
        if (lowerText.contains("четырехбуквенн") || lowerText.contains("4 букв") || lowerText.contains("четырёхбуквенн")) return 4;
        if (lowerText.contains("пятибуквенн") || lowerText.contains("5 букв")) return 5;
        if (lowerText.contains("шестибуквенн") || lowerText.contains("6 букв")) return 6;
        if (lowerText.contains("семибуквенн") || lowerText.contains("7 букв")) return 7;
        if (lowerText.contains("восьмибуквенн") || lowerText.contains("8 букв")) return 8;
        if (lowerText.contains("девятибуквенн") || lowerText.contains("9 букв")) return 9;
        if (lowerText.contains("десятибуквенн") || lowerText.contains("10 букв")) return 10;

        // По умолчанию: 5 букв (самый частый случай)
        return 5;
    }

    /**
     * Определяет, могут ли буквы повторяться
     */
    private boolean extractUniqueLetters(String text) {
        String lowerText = text.toLowerCase();

        // Если явно указано, что буквы могут повторяться
        if (lowerText.contains("буквы могут повторяться") ||
                lowerText.contains("буквы повторяются") ||
                lowerText.contains("с повторением") ||
                lowerText.contains("могут повторяться")) {
            return false; // NO - буквы могут повторяться
        }

        // Если указано, что буквы разные/не повторяются
        if (lowerText.contains("буквы не повторяются") ||
                lowerText.contains("без повторения") ||
                lowerText.contains("различные буквы") ||
                lowerText.contains("все буквы различны")) {
            return true; // YES - буквы уникальные
        }

        // Если алфавит маленький, а длина слова большая - буквы должны повторяться
        String alphabet = extractAlphabet(text);
        int wordLength = extractWordLength(text);

        if (wordLength > alphabet.length()) {
            return false; // Буквы должны повторяться
        }

        // По умолчанию: буквы могут повторяться (более частый случай в задачах)
        return false;
    }

    /**
     * Извлекает условия из текста задачи
     */
    private List<String> extractConditions(String text) {
        List<String> conditions = new ArrayList<>();
        String lowerText = text.toLowerCase();

        // 1. ПАЛИНДРОМ
        if (lowerText.contains("палиндром") ||
                lowerText.contains("читается одинаково") ||
                lowerText.contains("симметричн") ||
                lowerText.contains("перевертыш")) {
            conditions.add("PALINDROME");
        }

        // 2. ЧЕРЕДОВАНИЕ ГЛАСНЫХ И СОГЛАСНЫХ
        if (lowerText.contains("чередуются") &&
                (lowerText.contains("гласн") || lowerText.contains("согласн"))) {
            conditions.add("ALTERNATING");
        }

        // 3. ПОСЛЕ КАЖДОЙ СОГЛАСНОЙ ИДЕТ ГЛАСНАЯ
        if ((lowerText.contains("после каждой согласной идет гласная") ||
                lowerText.contains("после согласной идет гласная") ||
                lowerText.contains("согласная перед гласной")) &&
                lowerText.contains("гласн")) {
            conditions.add("CONSONANT_FOLLOWED_BY_VOWEL");
        }

        // 4. ПОСЛЕ КАЖДОЙ ГЛАСНОЙ ИДЕТ СОГЛАСНАЯ
        if ((lowerText.contains("после каждой гласной идет согласная") ||
                lowerText.contains("после гласной идет согласная") ||
                lowerText.contains("гласная перед согласной")) &&
                lowerText.contains("согласн")) {
            conditions.add("VOWEL_FOLLOWED_BY_CONSONANT");
        }

        // 5. ГЛАСНЫХ БОЛЬШЕ ЧЕМ СОГЛАСНЫХ
        if ((lowerText.contains("гласных больше чем согласных") ||
                lowerText.contains("гласных больше, чем согласных") ||
                lowerText.contains("больше гласных")) &&
                lowerText.contains("согласн")) {
            conditions.add("MORE_VOWELS_THAN_CONSONANTS");
        }

        // 6. СОГЛАСНЫХ БОЛЬШЕ ЧЕМ ГЛАСНЫХ
        if ((lowerText.contains("согласных больше чем гласных") ||
                lowerText.contains("согласных больше, чем гласных") ||
                lowerText.contains("больше согласных")) &&
                lowerText.contains("гласн")) {
            conditions.add("MORE_CONSONANTS_THAN_VOWELS");
        }

        // 7. ГЛАСНЫХ СТОЛЬКО ЖЕ СКОЛЬКО СОГЛАСНЫХ
        if ((lowerText.contains("гласных столько же сколько согласных") ||
                lowerText.contains("столько же гласных и согласных") ||
                lowerText.contains("равное количество гласных и согласных")) &&
                lowerText.contains("гласн") && lowerText.contains("согласн")) {
            conditions.add("EQUAL_VOWELS_CONSONANTS");
        }

        // 8. НАЧИНАЕТСЯ С ГЛАСНОЙ/СОГЛАСНОЙ
        if (lowerText.contains("начинается с гласной")) {
            conditions.add("STARTS_WITH_VOWEL");
        }
        if (lowerText.contains("начинается с согласной")) {
            conditions.add("STARTS_WITH_CONSONANT");
        }

        // 9. ОКАНЧИВАЕТСЯ НА ГЛАСНУЮ/СОГЛАСНУЮ
        if (lowerText.contains("оканчивается на гласную") ||
                lowerText.contains("кончается на гласную")) {
            conditions.add("ENDS_WITH_VOWEL");
        }
        if (lowerText.contains("оканчивается на согласную") ||
                lowerText.contains("кончается на согласную")) {
            conditions.add("ENDS_WITH_CONSONANT");
        }

        // 10. СОДЕРЖИТ ОПРЕДЕЛЕННУЮ БУКВУ
        Pattern containsPattern = Pattern.compile("содержит букву\\s+([А-Яа-яA-Z])");
        Matcher containsMatcher = containsPattern.matcher(text);
        while (containsMatcher.find()) {
            String letter = containsMatcher.group(1).toUpperCase();
            conditions.add("CONTAINS_LETTER_" + letter);
        }

        // 11. НЕ СОДЕРЖИТ ОПРЕДЕЛЕННУЮ БУКВУ
        Pattern notContainsPattern = Pattern.compile("не содержит букву\\s+([А-Яа-яA-Z])");
        Matcher notContainsMatcher = notContainsPattern.matcher(text);
        while (notContainsMatcher.find()) {
            String letter = notContainsMatcher.group(1).toUpperCase();
            conditions.add("NOT_CONTAINS_LETTER_" + letter);
        }

        // 12. СЛОВО ИМЕЕТ ОПРЕДЕЛЕННУЮ БУКВУ НА ОПРЕДЕЛЕННОМ МЕСТЕ
        Pattern positionPattern = Pattern.compile("([0-9]+)-я буква\\s+([А-Яа-яA-Z])");
        Matcher positionMatcher = positionPattern.matcher(lowerText);
        while (positionMatcher.find()) {
            String position = positionMatcher.group(1);
            String letter = positionMatcher.group(2).toUpperCase();
            conditions.add("POSITION_" + position + "_IS_" + letter);
        }

        // Если условий не найдено, добавляем NONE
        if (conditions.isEmpty()) {
            conditions.add("NONE");
        }

        return conditions;
    }

    /**
     * Определяет тип расчета
     */
    private String determineCalculateType(String text) {
        String lowerText = text.toLowerCase();

        if (lowerText.contains("подсчитайте количество") ||
                lowerText.contains("сколько") ||
                lowerText.contains("найти количество") ||
                lowerText.contains("определить количество")) {
            return "COMBINATIONS";
        }

        if (lowerText.contains("вероятность") ||
                lowerText.contains("шанс") ||
                lowerText.contains("какова вероятность")) {
            return "PROBABILITY";
        }

        return "COMBINATIONS"; // По умолчанию
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
            result += " (задача со словами)";
        }

        return result;
    }

    /**
     * Обогащает контекст данными из текста
     */
    private void enrichContextFromText(ProblemContext context, String text, String taskTitle) {
        if (context == null) return;

        // Устанавливаем тип задачи
        context.setProblemType(ProblemType.WORDS);
        context.setTaskName(taskTitle);

        // Устанавливаем параметры
        String alphabet = extractAlphabet(text);
        context.setAlphabet(alphabet);

        int wordLength = extractWordLength(text);
        context.setWordLength(wordLength);

        boolean uniqueLetters = extractUniqueLetters(text);
        context.setUniqueLetters(uniqueLetters);

        // Устанавливаем расчет
        String calculateType = determineCalculateType(text);
        context.setCalculationType(calculateType);

        // Добавляем условия
        List<String> conditions = extractConditions(text);
        for (String condition : conditions) {
            if (!"NONE".equals(condition)) {
                context.addGeneralCondition(condition);
            }
        }

        // Сохраняем исходный текст
        context.setParameter("originalText", text);

        // Определяем гласные и согласные для алфавита
        String vowels = extractVowels(alphabet);
        String consonants = extractConsonants(alphabet);
        context.setParameter("vowels", vowels);
        context.setParameter("consonants", consonants);
        context.setParameter("alphabetSize", alphabet.length());
        context.setParameter("vowelsCount", vowels.length());
        context.setParameter("consonantsCount", consonants.length());
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

    /**
     * Извлекает гласные из алфавита
     */
    private String extractVowels(String alphabet) {
        StringBuilder vowels = new StringBuilder();
        for (char c : alphabet.toCharArray()) {
            if (isVowel(c)) {
                vowels.append(c);
            }
        }
        return vowels.toString();
    }

    /**
     * Извлекает согласные из алфавита
     */
    private String extractConsonants(String alphabet) {
        StringBuilder consonants = new StringBuilder();
        for (char c : alphabet.toCharArray()) {
            if (!isVowel(c) && Character.isLetter(c)) {
                consonants.append(c);
            }
        }
        return consonants.toString();
    }

    /**
     * Проверяет, является ли буква гласной
     */
    private boolean isVowel(char c) {
        char upperC = Character.toUpperCase(c);
        return "АЕЁИОУЫЭЮЯAEIOUY".indexOf(upperC) != -1;
    }

    /**
     * Возвращает контекст задачи после парсинга
     */
    public ProblemContext getLastContext() {
        return lastContext;
    }
}