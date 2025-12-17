package ru.spb.ipo.generator.base;

public class SetUtil {

    public static String decart(String origin) {
        String source = "	<set type=\"DecartSet\">\n" +
                "<for name=\"di\" first=\"1\" last=\"${length}\" inc=\"1\">\n" +
                origin +
                "</for>" +
                "	</set>\n";
        return source;
    }

    public static String numericSet(String first, String last) {
        String s = "<set type=\"NumericSet\" first=\"" + first + "\" last=\"" + last + "\"/>\n";
        return s;
    }
}
