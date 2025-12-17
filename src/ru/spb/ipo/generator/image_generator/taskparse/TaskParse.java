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
        return cardParser.parse(title, text);
    }

    private String parseWordsTask(String title, String text) {
        System.out.println("TaskParse: парсинг словесной задачи");
        return "Словесная задача: в разработке";
    }

    private String parseNumbersTask(String title, String text) {
        System.out.println("TaskParse: парсинг числовой задачи");
        return "Числовая задача: в разработке";
    }

    private String parseChessTask(String title, String text) {
        System.out.println("TaskParse: парсинг шахматной задачи");
        return "Шахматная задача: в разработке";
    }

    private String parseEquationsTask(String title, String text) {
        System.out.println("TaskParse: парсинг уравнений");
        return "Уравнения: в разработке";
    }

    private String parseBallsTask(String title, String text) {
        System.out.println("TaskParse: парсинг шаров и урн");
        return "Шары и урны: в разработке";
    }

    private String parseDivisibilityTask(String title, String text) {
        System.out.println("TaskParse: парсинг делимости");
        return "Делимость: в разработке";
    }

    private String parseRemaindersTask(String title, String text) {
        System.out.println("TaskParse: парсинг остатков");
        return "Остатки: в разработке";
    }

    private String parseUnknownTask(String title, String text) {
        System.out.println("TaskParse: неизвестный тип задачи");
        return "Неизвестный тип задачи";
    }

}