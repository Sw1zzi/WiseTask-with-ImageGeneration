package ru.spb.ipo.generator.image_generator.taskparse;

/**
 * Чистый диспетчер - принимает тип задачи и вызывает соответствующий парсер
 */
public class TaskParse {

    /**
     * Просто диспетчер - передает управление нужному парсеру
     * @param taskTitle Название задачи
     * @param taskText Текст задачи
     * @param taskType Тип задачи от редактора
     * @return Результат от парсера
     */
    public String parseTask(String taskTitle, String taskText, String taskType) {
        System.out.println("TaskParse: диспетчер -> тип задачи: " + taskType);

        // Просто вызываем соответствующий парсер
        switch (taskType.toUpperCase()) {
            case "CARDS":
                return parseCardsTask(taskTitle, taskText);
            case "WORDS":
                return parseWordsTask(taskTitle, taskText);
            case "NUMBERS":
                return parseNumbersTask(taskTitle, taskText);
            case "CHESS":
                return parseChessTask(taskTitle, taskText);
            case "EQUATIONS":
                return parseEquationsTask(taskTitle, taskText);
            case "BALLS":
                return parseBallsTask(taskTitle, taskText);
            case "DIVISIBILITY":
                return parseDivisibilityTask(taskTitle, taskText);
            case "REMAINDERS":
                return parseRemaindersTask(taskTitle, taskText);
            default:
                return parseUnknownTask(taskTitle, taskText);
        }
    }



    private String parseCardsTask(String title, String text) {
        System.out.println("Вызов CardParser...");
        CardParser cardParser = new CardParser();
        System.out.println(text);
        return cardParser.parse(title, text);
    }

    private String parseWordsTask(String title, String text) {
        System.out.println("TaskParse: парсинг словесной задачи");
        WordsParser wordsParser = new WordsParser();
        System.out.println(text);
        return wordsParser.parse(title, text);
    }

    private String parseNumbersTask(String title, String text) {
        System.out.println("TaskParse: парсинг числовой задачи");
        NumbersParser numbersParser = new NumbersParser();
        return numbersParser.parse(title, text);
    }

    private String parseChessTask(String title, String text) {
        System.out.println("Вызов ChessParser...");
        ChessParser chessParser = new ChessParser();
        return chessParser.parse(title, text);
    }

    private String parseEquationsTask(String title, String text) {
        System.out.println("Вызов EquationsParser...");
        EquationParser equationsParser = new EquationParser();
        return equationsParser.parse(title, text);
    }

    private String parseBallsTask(String title, String text) {
        System.out.println("TaskParse: парсинг шаров и урн");
        BallsParser ballsParser = new BallsParser();
        return ballsParser.parse(title, text);
    }

    private String parseDivisibilityTask(String title, String text) {
        System.out.println("TaskParse: парсинг делимости");
        DivisibilityParser divisibilityParser = new DivisibilityParser();
        return divisibilityParser.parse(title, text);
    }

    private String parseRemaindersTask(String title, String text) {
        System.out.println("TaskParse: парсинг остатков");
        RemainderParser reminderParser = new RemainderParser();
        return reminderParser.parse(title, text);
    }

    private String parseUnknownTask(String title, String text) {
        System.out.println("TaskParse: неизвестный тип задачи");
        return "Неизвестный тип задачи";
    }

}