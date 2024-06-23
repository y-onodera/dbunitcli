package yo.dbunitcli.resource.st4;

import org.stringtemplate.v4.StringRenderer;
import yo.dbunitcli.Strings;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

public class SqlEscapeStringRenderer extends StringRenderer {

    @Override
    public String toString(final Object o, final String formatString, final Locale locale) {
        if (formatString == null) {
            return o.toString();
        } else if (formatString.equals("camelToSnake")) {
            return Strings.camelToSnake(o.toString(), Character::toLowerCase);
        } else if (formatString.equals("camelToUpperSnake")) {
            return Strings.camelToSnake(o.toString(), Character::toUpperCase);
        } else if (formatString.equals("snakeToCamel")) {
            return Strings.snakeToCamel(o.toString(), Character::toLowerCase);
        } else if (formatString.equals("snakeToUpperCamel")) {
            return Strings.snakeToCamel(o.toString(), Character::toUpperCase);
        } else if (formatString.equals("camelToKebab")) {
            return Strings.camelToKebab(o.toString(), Character::toLowerCase);
        } else if (formatString.equals("camelToUpperKebab")) {
            return Strings.camelToKebab(o.toString(), Character::toUpperCase);
        } else if (formatString.equals("kebabToCamel")) {
            return Strings.kebabToCamel(o.toString(), Character::toLowerCase);
        } else if (formatString.equals("kebabToUpperCamel")) {
            return Strings.kebabToCamel(o.toString(), Character::toUpperCase);
        } else if (formatString.equals("escapeSql")) {
            return this.toClob(o.toString().replace("'", "''"));
        } else if (formatString.equals("jexlExp")) {
            return "${" + o.toString() + "}";
        } else if (formatString.equals("ST4Exp")) {
            return "$" + o.toString() + "$";
        }
        return super.toString(o, formatString, locale);
    }

    private String toClob(final String toString) {
        final int length = toString.getBytes(StandardCharsets.UTF_8).length;
        if (toString.contains("\r\n")) {
            return Arrays.stream(toString.split("\r\n"))
                    .map(it -> this.quoted(it, length))
                    .reduce("", (current, newOne) -> current.isEmpty() ? newOne : current + " || CHR(13) || CHR(10) || " + newOne);
        } else if (toString.contains("\n")) {
            return Arrays.stream(toString.split("\n"))
                    .map(toString1 -> this.quoted(toString1, length))
                    .reduce("", (current, newOne) -> current.isEmpty() ? newOne : current + " || CHR(10) || " + newOne);
        }
        return this.quoted(toString, length);
    }

    private String quoted(final String toString, final int length) {
        if (length < 4000) {
            return "'" + toString + "'";
        }
        final int limit = toString.getBytes(StandardCharsets.UTF_8).length;
        int start = 0;
        int end = Math.min(4000, limit);
        final StringBuilder result = new StringBuilder();
        while (start < end) {
            result.append("TO_CLOB('");
            result.append(toString, start, end);
            result.append("')");
            start = end;
            end = Math.min(end + 4000, limit);
            if (start < end) {
                result.append(" || ");
            }
        }
        return result.toString();
    }

}
