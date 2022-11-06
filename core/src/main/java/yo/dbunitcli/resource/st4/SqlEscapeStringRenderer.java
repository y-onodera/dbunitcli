package yo.dbunitcli.resource.st4;

import com.google.common.base.CaseFormat;
import org.stringtemplate.v4.StringRenderer;

import java.util.Locale;

public class SqlEscapeStringRenderer extends StringRenderer {

    @Override
    public String toString(final Object o, final String formatString, final Locale locale) {
        if (formatString == null) {
            return o.toString();
        } else if (formatString.equals("escapeSql")) {
            return o.toString().replace("'", "''");
        } else if (formatString.equals("LCamelToLSnake")) {
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, o.toString());
        } else if (formatString.equals("LCamelToUSnake")) {
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, o.toString());
        } else if (formatString.equals("LSnakeToLCamel")) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, o.toString());
        } else if (formatString.equals("USnakeToLCamel")) {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, o.toString());
        } else if (formatString.equals("jexlExp")) {
            return "${" + o.toString() + "}";
        } else if (formatString.equals("ST4Exp")) {
            return "$" + o.toString() + "$";
        }
        return super.toString(o, formatString, locale);
    }
}
