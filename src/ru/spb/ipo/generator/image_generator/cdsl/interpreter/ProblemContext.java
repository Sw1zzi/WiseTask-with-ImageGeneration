package ru.spb.ipo.generator.image_generator.cdsl.interpreter;

import ru.spb.ipo.generator.image_generator.cdsl.model.Card;
import ru.spb.ipo.generator.image_generator.cdsl.model.ProblemType;

import java.util.*;

public class ProblemContext {
    private ProblemType problemType;
    private String taskName;
    private String calculationType;

    // === ОБЩИЕ ПАРАМЕТРЫ ДЛЯ ВСЕХ ТИПОВ ЗАДАЧ ===
    private Map<String, Object> parameters = new HashMap<>();
    private List<String> generalConditions = new ArrayList<>();

    // === СПЕЦИАЛЬНЫЕ СТРУКТУРЫ ДЛЯ КОНКРЕТНЫХ ТИПОВ ЗАДАЧ ===

    // Для карт
    private List<Card> targetCards = new ArrayList<>();
    private List<CountCondition> countConditions = new ArrayList<>();

    // Для остатков и делимости
    private String dividend;
    private int divisor;
    private int remainder;

    // Для уравнений
    private List<String> constraints = new ArrayList<>();

    // Для шахмат
    private Map<String, Integer> pieces = new HashMap<>();
    private int boardHeight;
    private int boardWidth;
    private boolean attacking = false;

    // Для чисел (NUMBERS)
    private int digits = 3;
    private int maxDigit = 9;
    private boolean firstNotZero = false;
    private boolean distinctDigits = false;
    private boolean adjacentDifferent = false;
    private String order = null;
    private List<String> compareLeft = new ArrayList<>();
    private List<String> compareRight = new ArrayList<>();
    private String compareOperator = null;

    // Для делимости
    private String rule;
    private String transformation;
    private int factor;
    private String operationType;
    private List<String> digitPositions = new ArrayList<>();
    private List<String> divisibilityConditions = new ArrayList<>();

    // === КЛАСС ДЛЯ УНИВЕРСАЛЬНЫХ УСЛОВИЙ COUNT() ===

    public static class CountCondition {
        private String countType;
        private String countValue;
        private String operator;
        private int targetValue;

        public CountCondition() {}

        public CountCondition(String countType, String countValue, String operator, int targetValue) {
            this.countType = countType;
            this.countValue = countValue;
            this.operator = operator;
            this.targetValue = targetValue;
        }

        public String getCountType() { return countType; }
        public void setCountType(String countType) { this.countType = countType; }

        public String getCountValue() { return countValue; }
        public void setCountValue(String countValue) { this.countValue = countValue; }

        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }

        public int getTargetValue() { return targetValue; }
        public void setTargetValue(int targetValue) { this.targetValue = targetValue; }

        @Override
        public String toString() {
            return String.format("COUNT(%s %s) %s %d", countType, countValue, operator, targetValue);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CountCondition that = (CountCondition) o;
            return targetValue == that.targetValue &&
                    Objects.equals(countType, that.countType) &&
                    Objects.equals(countValue, that.countValue) &&
                    Objects.equals(operator, that.operator);
        }

        @Override
        public int hashCode() {
            return Objects.hash(countType, countValue, operator, targetValue);
        }
    }

    // === ОСНОВНЫЕ ГЕТТЕРЫ И СЕТТЕРЫ ===

    public ProblemType getProblemType() { return problemType; }
    public void setProblemType(ProblemType problemType) { this.problemType = problemType; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getCalculationType() { return calculationType; }
    public void setCalculationType(String calculationType) { this.calculationType = calculationType; }

    // === УНИВЕРСАЛЬНЫЙ ДОСТУП К ПАРАМЕТРАМ ===

    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> type) {
        Object value = parameters.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public int getIntParameter(String key) {
        Object value = parameters.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public String getStringParameter(String key) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : null;
    }

    public boolean getBooleanParameter(String key) {
        Object value = parameters.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }

    public Map<String, Object> getAllParameters() {
        return new HashMap<>(parameters);
    }

    // === УСЛОВИЯ ===

    public List<String> getGeneralConditions() { return generalConditions; }
    public void setGeneralConditions(List<String> generalConditions) { this.generalConditions = generalConditions; }
    public void addGeneralCondition(String condition) { this.generalConditions.add(condition); }
    public void addAllGeneralConditions(Collection<String> conditions) { this.generalConditions.addAll(conditions); }

    // === СПЕЦИАЛЬНЫЕ ГЕТТЕРЫ ДЛЯ ЧАСТО ИСПОЛЬЗУЕМЫХ ПАРАМЕТРОВ ===

    // Для карт
    public String getDeckType() { return getStringParameter("deckType"); }
    public void setDeckType(String deckType) { setParameter("deckType", deckType); }
    public int getDeckSize() { return getIntParameter("deckSize"); }
    public void setDeckSize(int deckSize) { setParameter("deckSize", deckSize); }

    public Card getTargetCard() {
        List<Card> cards = getTargetCards();
        return cards.isEmpty() ? null : cards.get(0);
    }

    public void setTargetCard(Card targetCard) {
        targetCards.clear();
        targetCards.add(targetCard);
    }

    public List<Card> getTargetCards() { return targetCards; }
    public void setTargetCards(List<Card> targetCards) { this.targetCards = targetCards; }

    public int getDrawCount() { return getIntParameter("drawCount"); }
    public void setDrawCount(int drawCount) { setParameter("drawCount", drawCount); }
    public boolean isWithReplacement() { return getBooleanParameter("withReplacement"); }
    public void setWithReplacement(boolean withReplacement) { setParameter("withReplacement", withReplacement); }

    // ============================================================
    // МЕТОДЫ ДЛЯ ЗАДАЧ СО СЛОВАМИ (WORDS)
    // ============================================================

    /**
     * Возвращает алфавит для задачи со словами
     * @return строка с алфавитом или null
     */
    public String getAlphabet() {
        return getStringParameter("alphabet");
    }

    /**
     * Устанавливает алфавит для задачи со словами
     * @param alphabet строка с алфавитом
     */
    public void setAlphabet(String alphabet) {
        setParameter("alphabet", alphabet);
    }

    /**
     * Возвращает длину слова
     * @return длина слова или 0
     */
    public int getWordLength() {
        return getIntParameter("wordLength");
    }

    /**
     * Устанавливает длину слова
     * @param wordLength длина слова
     */
    public void setWordLength(int wordLength) {
        setParameter("wordLength", wordLength);
    }

    /**
     * Проверяет, должны ли буквы в слове быть уникальными
     * @return true если буквы должны быть уникальными
     */
    public boolean isUniqueLetters() {
        return getBooleanParameter("uniqueLetters");
    }

    /**
     * Устанавливает уникальность букв в слове
     * @param uniqueLetters true если буквы должны быть уникальными
     */
    public void setUniqueLetters(boolean uniqueLetters) {
        setParameter("uniqueLetters", uniqueLetters);
    }

    /**
     * Возвращает список условий для слов
     * @return список условий
     */
    public List<String> getWordConditions() {
        return getGeneralConditions();
    }

    /**
     * Добавляет условие для слов
     * @param condition условие
     */
    public void addWordCondition(String condition) {
        addGeneralCondition(condition);
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ УРАВНЕНИЙ
    // ============================================================

    public int getUnknowns() { return getIntParameter("unknowns"); }
    public void setUnknowns(int unknowns) { setParameter("unknowns", unknowns); }
    public int getSum() { return getIntParameter("sum"); }
    public void setSum(int sum) { setParameter("sum", sum); }
    public String getDomain() { return getStringParameter("domain"); }
    public void setDomain(String domain) { setParameter("domain", domain); }
    public List<String> getConstraints() { return constraints; }
    public void setConstraints(List<String> constraints) { this.constraints = constraints; }
    public void addConstraint(String constraint) { constraints.add(constraint); }

    // ============================================================
    // МЕТОДЫ ДЛЯ ОСТАТКОВ И ДЕЛИМОСТИ
    // ============================================================

    public String getDividend() { return dividend; }
    public void setDividend(String dividend) { this.dividend = dividend; }
    public int getDivisor() { return divisor; }
    public void setDivisor(int divisor) { this.divisor = divisor; }
    public int getRemainder() { return remainder; }
    public void setRemainder(int remainder) { this.remainder = remainder; }

    // ============================================================
    // МЕТОДЫ ДЛЯ COUNT УСЛОВИЙ
    // ============================================================

    public List<CountCondition> getCountConditions() { return countConditions; }
    public void setCountConditions(List<CountCondition> countConditions) { this.countConditions = countConditions; }
    public void addCountCondition(CountCondition condition) { countConditions.add(condition); }
    public boolean hasCountConditions() { return !countConditions.isEmpty(); }

    public List<CountCondition> getCountConditionsByType(String countType) {
        List<CountCondition> result = new ArrayList<>();
        for (CountCondition condition : countConditions) {
            if (countType.equals(condition.getCountType())) {
                result.add(condition);
            }
        }
        return result;
    }

    public List<CountCondition> getSuitConditions() { return getCountConditionsByType("SUIT"); }
    public List<CountCondition> getColorConditions() { return getCountConditionsByType("COLOR"); }
    public List<CountCondition> getRankTypeConditions() { return getCountConditionsByType("RANK_TYPE"); }

    public boolean hasSuitCondition(String suit) {
        for (CountCondition condition : countConditions) {
            if ("SUIT".equals(condition.getCountType()) && suit.equals(condition.getCountValue())) {
                return true;
            }
        }
        return false;
    }

    public CountCondition getSuitCondition(String suit) {
        for (CountCondition condition : countConditions) {
            if ("SUIT".equals(condition.getCountType()) && suit.equals(condition.getCountValue())) {
                return condition;
            }
        }
        return null;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ NUMBERS
    // ============================================================

    public int getDigits() {
        if (digits <= 0) {
            digits = getIntParameter("digits");
            if (digits <= 0) digits = 3;
        }
        return Math.max(3, Math.min(10, digits));
    }

    public void setDigits(int digits) {
        this.digits = digits;
        setParameter("digits", digits);
    }

    public int getMaxDigit() {
        if (maxDigit <= 0) {
            maxDigit = getIntParameter("maxDigit");
            if (maxDigit <= 0) maxDigit = 9;
        }
        return maxDigit;
    }

    public void setMaxDigit(int maxDigit) {
        this.maxDigit = maxDigit;
        setParameter("maxDigit", maxDigit);
    }

    public boolean isFirstNotZero() {
        if (!parameters.containsKey("firstNotZero")) {
            return firstNotZero;
        }
        return getBooleanParameter("firstNotZero");
    }

    public void setFirstNotZero(boolean firstNotZero) {
        this.firstNotZero = firstNotZero;
        setParameter("firstNotZero", firstNotZero);
    }

    public boolean isDistinctDigits() {
        if (!parameters.containsKey("distinct")) {
            return distinctDigits;
        }
        return getBooleanParameter("distinct");
    }

    public void setDistinctDigits(boolean distinctDigits) {
        this.distinctDigits = distinctDigits;
        setParameter("distinct", distinctDigits);
    }

    public boolean isAdjacentDifferent() {
        if (!parameters.containsKey("adjacentDifferent")) {
            return adjacentDifferent;
        }
        return getBooleanParameter("adjacentDifferent");
    }

    public void setAdjacentDifferent(boolean adjacentDifferent) {
        this.adjacentDifferent = adjacentDifferent;
        setParameter("adjacentDifferent", adjacentDifferent);
    }

    public String getOrder() {
        if (order == null) {
            order = getStringParameter("order");
        }
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
        setParameter("order", order);
    }

    @SuppressWarnings("unchecked")
    public List<String> getCompareLeft() {
        if (compareLeft.isEmpty()) {
            Object left = getParameter("compareLeft");
            if (left instanceof List) {
                compareLeft = (List<String>) left;
            }
        }
        return compareLeft;
    }

    public void setCompareLeft(List<String> compareLeft) {
        this.compareLeft.clear();
        this.compareLeft.addAll(compareLeft);
        setParameter("compareLeft", new ArrayList<>(compareLeft));
    }

    @SuppressWarnings("unchecked")
    public List<String> getCompareRight() {
        if (compareRight.isEmpty()) {
            Object right = getParameter("compareRight");
            if (right instanceof List) {
                compareRight = (List<String>) right;
            }
        }
        return compareRight;
    }

    public void setCompareRight(List<String> compareRight) {
        this.compareRight.clear();
        this.compareRight.addAll(compareRight);
        setParameter("compareRight", new ArrayList<>(compareRight));
    }

    public String getCompareOperator() {
        if (compareOperator == null) {
            compareOperator = getStringParameter("compareOperator");
        }
        return compareOperator;
    }

    public void setCompareOperator(String compareOperator) {
        this.compareOperator = compareOperator;
        setParameter("compareOperator", compareOperator);
    }

    public boolean hasComparison() {
        return !getCompareLeft().isEmpty() && !getCompareRight().isEmpty() &&
                getCompareOperator() != null;
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ДЕЛИМОСТИ
    // ============================================================

    public String getRule() {
        if (rule == null) {
            rule = getStringParameter("rule");
        }
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
        setParameter("rule", rule);
    }

    public String getTransformation() {
        if (transformation == null) {
            transformation = getStringParameter("transformation");
            if (transformation == null) {
                transformation = "увеличивается в целое число раз";
            }
        }
        return transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
        setParameter("transformation", transformation);
    }

    public int getFactor() {
        if (factor <= 0) {
            factor = getIntParameter("factor");
            if (factor <= 0) factor = 2;
        }
        return factor;
    }

    public void setFactor(int factor) {
        this.factor = factor;
        setParameter("factor", factor);
    }

    public String getOperationType() {
        if (operationType == null) {
            operationType = getStringParameter("operationType");
            if (operationType == null) {
                operationType = "INCREASE";
            }
        }
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
        setParameter("operationType", operationType);
    }

    public List<String> getDigitPositions() {
        if (digitPositions.isEmpty()) {
            Object positions = getParameter("digitPositions");
            if (positions instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> positionsList = (List<String>) positions;
                digitPositions.addAll(positionsList);
            }
        }
        return digitPositions;
    }

    public void setDigitPositions(List<String> digitPositions) {
        this.digitPositions.clear();
        this.digitPositions.addAll(digitPositions);
        setParameter("digitPositions", new ArrayList<>(digitPositions));
    }

    public List<String> getDivisibilityConditions() {
        if (divisibilityConditions.isEmpty()) {
            Object conditionsParam = getParameter("divisibilityConditions");
            if (conditionsParam instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> conditionsList = (List<String>) conditionsParam;
                divisibilityConditions.addAll(conditionsList);
            }
        }
        return divisibilityConditions;
    }

    public void setDivisibilityConditions(List<String> divisibilityConditions) {
        this.divisibilityConditions.clear();
        this.divisibilityConditions.addAll(divisibilityConditions);
        setParameter("divisibilityConditions", new ArrayList<>(divisibilityConditions));
    }

    public void addDivisibilityCondition(String condition) {
        divisibilityConditions.add(condition);
        addGeneralCondition(condition);
        setParameter("divisibilityConditions", new ArrayList<>(divisibilityConditions));
    }

    public String getDivisibilityDescription() {
        return getStringParameter("description");
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ШАХМАТ
    // ============================================================

    public int getBoardHeight() {
        int height = boardHeight;
        if (height <= 0) {
            height = getIntParameter("boardHeight");
            if (height <= 0) height = 8;
        }
        return height;
    }

    public void setBoardHeight(int boardHeight) {
        this.boardHeight = boardHeight;
        setParameter("boardHeight", boardHeight);
    }

    public int getBoardWidth() {
        int width = boardWidth;
        if (width <= 0) {
            width = getIntParameter("boardWidth");
            if (width <= 0) width = 8;
        }
        return width;
    }

    public void setBoardWidth(int boardWidth) {
        this.boardWidth = boardWidth;
        setParameter("boardWidth", boardWidth);
    }

    public Map<String, Integer> getPieces() {
        if (pieces.isEmpty()) {
            Object piecesParam = getParameter("pieces");
            if (piecesParam instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> piecesMap = (Map<String, Integer>) piecesParam;
                pieces.putAll(piecesMap);
            }
        }
        return pieces;
    }

    public void setPieces(Map<String, Integer> pieces) {
        this.pieces.clear();
        this.pieces.putAll(pieces);
        setParameter("pieces", new HashMap<>(pieces));
    }

    public boolean isAttacking() {
        if (!parameters.containsKey("attacking")) {
            return attacking;
        }
        return getBooleanParameter("attacking");
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
        setParameter("attacking", attacking);
    }

    public int getTotalPieces() {
        return getIntParameter("totalPieces");
    }

    public String getPiecesString() {
        return getStringParameter("piecesStr");
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    public List<String> getAllConditionStrings() {
        List<String> allConditions = new ArrayList<>();

        // Count условия
        for (CountCondition condition : countConditions) {
            allConditions.add(condition.toString());
        }

        // Общие условия (включая условия для слов)
        allConditions.addAll(generalConditions);

        // Условия делимости
        allConditions.addAll(divisibilityConditions);

        // Уравнения
        allConditions.addAll(constraints);

        return allConditions;
    }

    public void clearAllConditions() {
        countConditions.clear();
        generalConditions.clear();
        constraints.clear();
        divisibilityConditions.clear();
    }

    public boolean hasAnyConditions() {
        return !countConditions.isEmpty() || !generalConditions.isEmpty() ||
                !constraints.isEmpty() || !divisibilityConditions.isEmpty();
    }

    // ============================================================
    // toString()
    // ============================================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Task: %s '%s'",
                problemType != null ? problemType.getDisplayName() : "Unknown",
                taskName != null ? taskName : "Unnamed"));

        if (problemType != null) {
            sb.append(", Type: ").append(problemType.name());
        }

        if (!parameters.isEmpty()) {
            sb.append(", Parameters: ").append(parameters);
        }

        // WORDS информация
        if (problemType == ProblemType.WORDS) {
            String alphabet = getAlphabet();
            if (alphabet != null && !alphabet.isEmpty()) {
                sb.append(", Alphabet: ").append(alphabet.length() > 20 ? alphabet.substring(0, 20) + "..." : alphabet);
            }
            sb.append(", Word Length: ").append(getWordLength());
            sb.append(", Unique Letters: ").append(isUniqueLetters());
            if (!getWordConditions().isEmpty()) {
                sb.append(", Word Conditions: ").append(getWordConditions());
            }
        }

        // NUMBERS информация
        if (problemType == ProblemType.NUMBERS) {
            sb.append(", Digits: ").append(getDigits());
            sb.append(", MaxDigit: ").append(getMaxDigit());
            sb.append(", FirstNotZero: ").append(isFirstNotZero());
            sb.append(", Distinct: ").append(isDistinctDigits());
            sb.append(", AdjacentDifferent: ").append(isAdjacentDifferent());
            if (getOrder() != null) {
                sb.append(", Order: ").append(getOrder());
            }
            if (hasComparison()) {
                sb.append(", Compare: ").append(getCompareLeft())
                        .append(" ").append(getCompareOperator())
                        .append(" ").append(getCompareRight());
            }
        }

        // Карты
        if (!targetCards.isEmpty()) {
            sb.append(", Target Cards: ").append(targetCards);
        }

        if (!countConditions.isEmpty()) {
            sb.append(", Count Conditions: ").append(countConditions);
        }

        if (!generalConditions.isEmpty()) {
            sb.append(", General Conditions: ").append(generalConditions);
        }

        if (!constraints.isEmpty()) {
            sb.append(", Constraints: ").append(constraints);
        }

        if (!divisibilityConditions.isEmpty()) {
            sb.append(", Divisibility Conditions: ").append(divisibilityConditions);
        }

        if (calculationType != null) {
            sb.append(", Calculate: ").append(calculationType);
        }

        // Делимость
        if (problemType == ProblemType.DIVISIBILITY) {
            sb.append(", Digits: ").append(getDigits());
            if (getRule() != null) {
                sb.append(", Rule: ").append(getRule());
            }
            if (getTransformation() != null) {
                sb.append(", Transformation: ").append(getTransformation());
            }
            if (getFactor() > 0) {
                sb.append(", Factor: ").append(getFactor());
            }
            if (!getDigitPositions().isEmpty()) {
                sb.append(", Digit Positions: ").append(getDigitPositions());
            }
        }

        // Шахматы
        if (problemType == ProblemType.CHESS) {
            sb.append(", Board: ").append(getBoardWidth()).append("x").append(getBoardHeight());
            if (!pieces.isEmpty()) {
                sb.append(", Pieces: ").append(pieces);
            }
            sb.append(", Attacking: ").append(isAttacking());
        }

        return sb.toString();
    }

    // ============================================================
    // МЕТОДЫ ДЛЯ ОЧИСТКИ И СБРОСА
    // ============================================================

    public void clear() {
        problemType = null;
        taskName = null;
        calculationType = null;
        parameters.clear();
        generalConditions.clear();
        targetCards.clear();
        countConditions.clear();
        dividend = null;
        divisor = 0;
        remainder = 0;
        constraints.clear();
        pieces.clear();
        boardHeight = 0;
        boardWidth = 0;
        attacking = false;

        // NUMBERS поля
        digits = 3;
        maxDigit = 9;
        firstNotZero = false;
        distinctDigits = false;
        adjacentDifferent = false;
        order = null;
        compareLeft.clear();
        compareRight.clear();
        compareOperator = null;

        // Делимость поля
        rule = null;
        transformation = null;
        factor = 0;
        operationType = null;
        digitPositions.clear();
        divisibilityConditions.clear();
    }

    public ProblemContext copy() {
        ProblemContext copy = new ProblemContext();
        copy.problemType = this.problemType;
        copy.taskName = this.taskName;
        copy.calculationType = this.calculationType;

        // Глубокое копирование параметров
        copy.parameters.putAll(this.parameters);

        // Глубокое копирование коллекций
        copy.generalConditions.addAll(this.generalConditions);
        copy.targetCards.addAll(this.targetCards);
        copy.countConditions.addAll(this.countConditions);
        copy.constraints.addAll(this.constraints);
        copy.pieces.putAll(this.pieces);
        copy.digitPositions.addAll(this.digitPositions);
        copy.divisibilityConditions.addAll(this.divisibilityConditions);

        // NUMBERS копирование
        copy.digits = this.digits;
        copy.maxDigit = this.maxDigit;
        copy.firstNotZero = this.firstNotZero;
        copy.distinctDigits = this.distinctDigits;
        copy.adjacentDifferent = this.adjacentDifferent;
        copy.order = this.order;
        copy.compareLeft.addAll(this.compareLeft);
        copy.compareRight.addAll(this.compareRight);
        copy.compareOperator = this.compareOperator;

        // Копирование остальных полей
        copy.dividend = this.dividend;
        copy.divisor = this.divisor;
        copy.remainder = this.remainder;
        copy.boardHeight = this.boardHeight;
        copy.boardWidth = this.boardWidth;
        copy.attacking = this.attacking;
        copy.rule = this.rule;
        copy.transformation = this.transformation;
        copy.factor = this.factor;
        copy.operationType = this.operationType;

        return copy;
    }
}