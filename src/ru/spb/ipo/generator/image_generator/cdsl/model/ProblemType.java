package ru.spb.ipo.generator.image_generator.cdsl.model;
// Пакет моделей данных - содержит основные типы данных системы

public enum ProblemType {
// Перечисление типов задач, поддерживаемых системой
// Связывает внутреннее представление с CDSL синтаксисом и отображаемыми названиями

    CARDS("Карты", "CARDS"),
    // Задачи с картами: TASK CARDS
    // displayName: "Карты" (для отображения в UI)
    // cdslName: "CARDS" (ключевое слово в CDSL)

    WORDS("Слова", "WORDS"),
    // Задачи со словами: TASK WORDS

    NUMBERS("Числа", "NUMBERS"),
    // Задачи с числами: TASK NUMBERS

    EQUATIONS("Уравнения", "EQUATIONS"),
    // Задачи с уравнениями: TASK EQUATIONS

    BALLS_AND_URNS("Шары и урны", "BALLS"),
    // Задачи с шарами и урнами: TASK BALLS
    // Обратите внимание: displayName полное, cdslName сокращенное

    DIVISIBILITY("Делимости", "DIVISIBILITY"),
    // Задачи на делимость: TASK DIVISIBILITY

    REMAINDERS("Остатки", "REMAINDERS"),
    // Задачи на остатки: TASK REMAINDERS

    CHESS("Шахматы", "CHESS");
    // Шахматные задачи: TASK CHESS

    private final String displayName;
    // Человекочитаемое название для отображения в интерфейсе

    private final String cdslName;
    // Название, используемое в CDSL синтаксисе

    ProblemType(String displayName, String cdslName) {
        // Конструктор перечисления
        this.displayName = displayName;
        // Инициализация отображаемого названия
        this.cdslName = cdslName;
        // Инициализация CDSL названия
    }

    public String getDisplayName() {
        // Геттер для отображаемого названия
        return displayName;
        // Возвращаем displayName
    }

    public String getCdslName() {
        // Геттер для CDSL названия
        return cdslName;
        // Возвращаем cdslName
    }

    public static ProblemType fromCdslName(String cdslName) {
        // Статический метод для преобразования строки CDSL в тип задачи
        for (ProblemType type : values()) {
            // Перебираем все значения перечисления
            if (type.cdslName.equalsIgnoreCase(cdslName)) {
                // Если CDSL название совпадает (без учета регистра)
                return type;
                // Возвращаем найденный тип
            }
        }
        throw new IllegalArgumentException("Unknown CDSL task type: " + cdslName);
        // Если не нашли - бросаем исключение
    }
}