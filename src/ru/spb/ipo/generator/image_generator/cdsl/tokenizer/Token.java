package ru.spb.ipo.generator.image_generator.cdsl.tokenizer;
// Пакет содержит классы для лексического анализа (токенизации)

public class Token {
    // Объявление класса Token для представления лексем

    private final TokenType type;
    // Тип токена - определяет категорию (ключевое слово, оператор, число и т.д.)

    private final String value;
    // Строковое значение токена - фактический текст, который был распознан

    private final int line;
    // Номер строки в исходном коде, где найден токен

    private final int column;
    // Позиция в строке (номер колонки), где начинается токен

    public Token(TokenType type, String value, int line, int column) {
        // Конструктор, принимающий все необходимые параметры токена

        this.type = type;
        // Инициализация поля type

        this.value = value;
        // Инициализация поля value

        this.line = line;
        // Инициализация поля line

        this.column = column;
        // Инициализация поля column
    }

    public TokenType getType() { return type; }
    // Геттер для получения типа токена

    public String getValue() { return value; }
    // Геттер для получения строкового значения токена

    public int getLine() { return line; }
    // Геттер для получения номера строки

    public int getColumn() { return column; }
    // Геттер для получения номера колонки

    @Override
    public String toString() {
        // Переопределение метода toString() для удобного вывода информации о токене

        return String.format("Token(%s, '%s', %d:%d)", type, value, line, column);
        // Форматирование строки: Token(ТИП, 'ЗНАЧЕНИЕ', СТРОКА:КОЛОНКА)
    }

    @Override
    public boolean equals(Object obj) {
        // Переопределение метода equals() для сравнения токенов

        if (this == obj) return true;
        // Если сравниваемый объект - это тот же самый объект, возвращаем true

        if (obj == null || getClass() != obj.getClass()) return false;
        // Если объект null или классы не совпадают, возвращаем false

        Token token = (Token) obj;
        // Приводим объект к типу Token

        return line == token.line &&
                // Сравниваем номера строк
                column == token.column &&
                // Сравниваем номера колонок
                type == token.type &&
                // Сравниваем типы токенов (enum, можно использовать ==)
                value.equals(token.value);
        // Сравниваем строковые значения (String, используем equals)
    }

    @Override
    public int hashCode() {
        // Переопределение метода hashCode() для корректной работы с коллекциями

        int result = type.hashCode();
        // Начинаем с хеш-кода типа токена

        result = 31 * result + value.hashCode();
        // Умножаем на простое число 31 и добавляем хеш-код значения

        result = 31 * result + line;
        // Добавляем номер строки

        result = 31 * result + column;
        // Добавляем номер колонки

        return result;
        // Возвращаем итоговый хеш-код
    }
}