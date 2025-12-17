package ru.spb.ipo.generator.image_generator.cdsl.parser;
// Пакет парсера - здесь находится AST (абстрактное синтаксическое дерево)

import java.util.ArrayList;
// Импорт для использования списка
import java.util.List;
// Импорт интерфейса списка

public class ASTNode {
// Класс узла абстрактного синтаксического дерева (AST)
// AST представляет структуру программы в иерархическом виде

    private String type;
    // Тип узла - определяет семантику узла (например, "TASK_DECLARATION", "CARD")

    private Object value;
    // Значение узла - может быть строкой, числом, булевым значением и т.д.

    private List<ASTNode> children;
    // Список дочерних узлов - формирует древовидную структуру

    public ASTNode(String type) {
        // Конструктор для узла без значения
        this.type = type;
        // Инициализируем тип узла
        this.children = new ArrayList<>();
        // Создаем пустой список дочерних узлов
    }

    public ASTNode(String type, Object value) {
        // Конструктор для узла со значением
        this.type = type;
        // Инициализируем тип узла
        this.value = value;
        // Инициализируем значение узла
        this.children = new ArrayList<>();
        // Создаем пустой список дочерних узлов
    }

    public String getType() { return type; }
    // Геттер для получения типа узла

    public void setType(String type) { this.type = type; }
    // Сеттер для установки типа узла

    public Object getValue() { return value; }
    // Геттер для получения значения узла

    public void setValue(Object value) { this.value = value; }
    // Сеттер для установки значения узла

    public List<ASTNode> getChildren() { return children; }
    // Геттер для получения списка дочерних узлов

    public void addChild(ASTNode node) { children.add(node); }
    // Метод для добавления дочернего узла

    public ASTNode getChild(int index) {
        // Метод для получения дочернего узла по индексу
        return children.size() > index ? children.get(index) : null;
        // Если индекс в пределах списка - возвращаем узел, иначе null
    }

    @Override
    public String toString() {
        // Переопределение метода для строкового представления узла
        return toString(0);
        // Вызываем рекурсивный метод с нулевым отступом
    }

    private String toString(int indent) {
        // Рекурсивный метод для форматированного вывода дерева
        StringBuilder sb = new StringBuilder();
        // Создаем StringBuilder для построения строки

        for (int i = 0; i < indent; i++) {
            // Добавляем отступы в зависимости от уровня вложенности
            sb.append("  ");
            // Два пробела на каждый уровень
        }
        sb.append(type);
        // Добавляем тип узла

        if (value != null) {
            // Если у узла есть значение
            sb.append(": ").append(value);
            // Добавляем значение через двоеточие
        }
        sb.append("\n");
        // Переход на новую строку

        for (ASTNode child : children) {
            // Рекурсивно обрабатываем всех дочерних узлов
            sb.append(child.toString(indent + 1));
            // Вызываем toString с увеличенным отступом
        }

        return sb.toString();
        // Возвращаем форматированную строку
    }
}