package yo.dbunitcli.application;

import org.stringtemplate.v4.StringRenderer;

import java.util.Locale;

public class SqlEscapeStringRenderer extends StringRenderer {

    @Override
    public String toString(Object o, String formatString, Locale locale) {
        if (formatString == null) {
            return o.toString();
        } else if (formatString.equals("escapeSql")) {
            return o.toString().replace("'", "''");
        }
        return super.toString(o, formatString, locale);
    }
}
