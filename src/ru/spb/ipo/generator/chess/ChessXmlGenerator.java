package ru.spb.ipo.generator.chess;

import java.io.File;
import java.util.ArrayList;
import ru.spb.ipo.generator.base.BaseGenerator;
import ru.spb.ipo.generator.base.SetUtil;
import ru.spb.ipo.generator.base.ui.BaseGeneratorUI;
import ru.spb.ipo.generator.chess.ChessStringGeneration;
import java.util.Map;

public class ChessXmlGenerator extends BaseGenerator {

    public int n = Integer.valueOf((String) sourceParams.get("nabor"));
    public int m = Integer.valueOf((String) sourceParams.get("resultX"));
    public static String chooses = null;

    public ChessXmlGenerator(Map sourceParams, Map funcParams, Map taskParams) {
        super(sourceParams, funcParams, taskParams);
    }

   

    public String getDescription(Map sourceParams, Map funcParams, Map taskParams) {
        String genParam = getParams();
        ArrayList images = (ArrayList) taskParams.get("images");
        String imageStr = "";
        for (int i = 0; i < images.size(); i++) {
            String str = (String) images.get(i);
            imageStr += "<img>chess/" + str + "</img>\n";
        }
        if (imageStr.length() > 0) {
            imageStr = "<imgs>" + imageStr + "</imgs>";
        }

        genParam = replace(genParam, sourceParams);
        return "<description>\n<![CDATA[" + taskParams.get("description") + "\n]]>"
                + (imageStr.length() == 0 ? "" : imageStr)
                + "</description>\n" + genParam;
    }

    public String getVerifier(Map funcParams) {
        StringBuffer sb = new StringBuffer();

                    int rookM = 0;
            sb.append(ChessStringGeneration.Starter());
            switch (ChessFuncPanel.ChooseY) {
                case 1:
                    sb.append(ChessStringGeneration.Slon());
                    rookM = (ChessFuncPanel.numberFigur - 1);
                    break;
                case 2:
                    sb.append(ChessStringGeneration.Koni());
                    rookM = (ChessFuncPanel.numberFigur);
                    break;
                case 3:
                    sb.append(ChessStringGeneration.Ladya());
                    rookM = (ChessFuncPanel.numberFigur - 1);
                    break;
                case 5:
                    sb.append(ChessStringGeneration.Ferz());
                    rookM = (ChessFuncPanel.numberFigur - 1);
                    break;
                case 6:
                    sb.append(ChessStringGeneration.Pesh());
                    rookM = (ChessFuncPanel.numberFigur - 1);
                    break;
                case 7:
                    sb.append(ChessStringGeneration.Korol());
                    rookM = (ChessFuncPanel.numberFigur - 1);
                    break;
            }
            if(ChessFuncPanel.isKill) rookM = (ChessFuncPanel.numberFigur);
            if (ChessFuncPanel.isDiag) {
                sb.append(ChessStringGeneration.Diag());
            }
            if (ChessFuncPanel.isPob) {
                sb.append(ChessStringGeneration.PobDiag(n));
            }
            sb.append(ChessStringGeneration.Footer(rookM));

            return sb.toString();

         }

    public String getSourceTemplate() {
        String set = SetUtil.decart(SetUtil.numericSet("" + 0, "${result}"));
        String source = replace(set, getBaseSourceParameters());
       return "";
        
    }

    public String generateDescription() {

        int nabor = Integer.valueOf((String) sourceParams.get("nabor"));
        String str = nabor + "";
        str += " на " + sourceParams.get("resultX");
        if (ChessFuncPanel.numberFigur > 0) {
            return "Сколькими способами на доске " + str + " можно расставить "
                    + (isEmptyInline() ? "" : "" + taskParams.get("inlineDesc") + " ") + ", так чтобы они" + (ChessFuncPanel.isKill ? " " : " не ") + "били друг друга";
        } else {
            return "Нужно что то ставить! Фигур то нет!";
        }
    }

    public String getParams() {
        String genParam = "<description-params>\n"
                + "	<param name=\"n\">\n"
                + " 		<value>${nabor}</value>\n"
                + " 	</param>\n"
                + "	<param name=\"m\">\n"
                + " 		<value>${resultX}</value>\n"
                + " 	</param>\n"
                + "	<param name=\"rook\">\n"
                + " 		<value>" + ChessFuncPanel.numberFigur + "</value>\n"
                + " 	</param>\n"
                + "</description-params>";
        return genParam;
    }
}
