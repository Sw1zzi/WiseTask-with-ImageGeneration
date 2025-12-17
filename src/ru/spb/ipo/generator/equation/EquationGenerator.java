package ru.spb.ipo.generator.equation;

import ru.spb.ipo.generator.base.BaseGenerator;
import ru.spb.ipo.generator.base.ui.BaseGeneratorUI;
import ru.spb.ipo.generator.base.ui.ConstraintPanel;

import java.awt.*;
import java.util.Map;

public class EquationGenerator extends BaseGeneratorUI {

    public EquationGenerator() {
        initialize();
    }

    protected Dimension getGeneratorSize() {
        return new Dimension(840, 420);
    }

    protected ConstraintPanel getFunctionPanel() {
        if (functionPanel == null) {
            functionPanel = new EquationFuncPanel(this);
        }
        return functionPanel;
    }

    protected ConstraintPanel getSetPanel() {
        if (setPanel == null) {
            setPanel = new EquationSetPanel(this);
        }
        return setPanel;
    }

    public static void main(String[] args) {
        new EquationGenerator().setVisible(true);
    }

    public BaseGenerator createGenerator(Map source, Map func, Map task) {
        return new EquationXmlGenerator(source, func, task);
    }

    public String getHelpString() {
        return "Редактор \"Количество решений уравнения\"";
        //return "Редактор \"Комбинаторика на числах\"";//""Редактор задач на простейшее уравнение в целых числах";
    }


}
