package yo.dbunitcli.resource.st4;

import org.stringtemplate.v4.StringRenderer;

import java.util.Locale;
import java.util.Optional;

public class SqlEscapeStringRenderer extends StringRenderer {

    @Override
    public String toString(final Object o, final String formatString, final Locale locale) {
        if (formatString == null) {
            return o.toString();
        } else if (formatString.equals("camelToSnake")) {
            return this.camelToSnake(o.toString());
        } else if (formatString.equals("snakeToCamel")) {
            return this.snakeToCamel(o.toString());
        } else if (formatString.equals("escapeSql")) {
            return o.toString().replace("'", "''");
        } else if (formatString.equals("jexlExp")) {
            return "${" + o.toString() + "}";
        } else if (formatString.equals("ST4Exp")) {
            return "$" + o.toString() + "$";
        }
        return super.toString(o, formatString, locale);
    }

    private String camelToSnake(final String camel) {
        if (Optional.ofNullable(camel).orElse("").isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(camel.length() + camel.length());
        for (int i = 0; i < camel.length(); i++) {
            final char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(sb.length() != 0 ? '_' : "").append(Character.toLowerCase(c));
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    private String snakeToCamel(final String snake) {
        if (Optional.ofNullable(snake).orElse("").isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(snake.length() + snake.length());
        for (int i = 0; i < snake.length(); i++) {
            final char c = snake.charAt(i);
            if (c == '_') {
                sb.append((i + 1) < snake.length() ? Character.toUpperCase(snake.charAt(++i)) : "");
            } else {
                sb.append(sb.length() == 0 ? Character.toUpperCase(c) : Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }
}
