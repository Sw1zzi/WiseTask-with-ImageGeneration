package ru.spb.ipo.generator.digits.mods;

import ru.spb.ipo.generator.base.BaseGenerator;
import ru.spb.ipo.generator.base.UIUtils;
import ru.spb.ipo.generator.base.ui.BaseGeneratorUI;
import ru.spb.ipo.generator.base.ui.ConstraintPanel;

import java.awt.*;
import java.util.Map;

public class ModGenerator extends BaseGeneratorUI {

    public ModGenerator() {
        initialize();
    }

    protected void initialize() {
        super.initialize();
        UIUtils.enableAll(getFunctionListPanel(), false);
    }

    protected ConstraintPanel getSetPanel() {
        if (setPanel == null) {
            setPanel = new ModSetPanel(this);
        }
        return setPanel;
    }

    public static void main(String[] args) {
        new ModGenerator().setVisible(true);
    }

    protected Dimension getRightPanelDimension() {
        return new Dimension(380, 190);
    }


    protected boolean checkCanSave() {
        return super.checkCanSave() && ((ModSetPanel) getSetPanel()).checkCanSave();
    }

    protected void clear() {
        super.clear();
        ((ModSetPanel) getSetPanel()).clear();
    }

    public BaseGenerator createGenerator(Map source, Map func, Map task) {
        return new ModXmlGenerator(source, func, task);
    }

    @Override
    public String getHelpString() {
        return "Редактор \"Арифметика остатков\"";
    }


}
