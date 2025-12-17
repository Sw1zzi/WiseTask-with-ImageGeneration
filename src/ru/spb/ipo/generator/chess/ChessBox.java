package ru.spb.ipo.generator.chess;

import ru.spb.ipo.generator.base.ComplexElement;
import ru.spb.ipo.generator.base.FuncUtil;

public class ChessBox implements ComplexElement {

    private String figura;
    private String axis = "";
    private boolean isEqual;
    private boolean isDiag;
    private boolean isPob;



    public ChessBox(String figura, int axis ,  boolean isDiag, boolean isPob) {
        this.figura = "фигур вида " + figura + " на нашем поле";
        this.axis = axis + "";
        this.isDiag = isDiag;
        this.isPob = isPob;
    }

    public String generateXml() {
        StringBuffer sb = new StringBuffer();

        System.out.println(sb.toString());
        return sb.toString();
    }

    public String toDescription() {
        return toString();
    }

    public String toString() {
            String i = isDiag ? axis + " " + figura + " без главной диагонали" : axis + " " + figura;
            return isPob ? i + " без побочной диагонали" : i;
        }

    }


