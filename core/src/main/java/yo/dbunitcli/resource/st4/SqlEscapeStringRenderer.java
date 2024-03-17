package yo.dbunitcli.resource.st4;

import org.stringtemplate.v4.StringRenderer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

public class SqlEscapeStringRenderer extends StringRenderer {

    @Override
    public String toString(final Object o, final String formatString, final Locale locale) {
        if (formatString == null) {
            return o.toString();
        } else if (formatString.equals("camelToSnake")) {
            return this.camelToSnake(o.toString(), Character::toLowerCase);
        } else if (formatString.equals("camelToUpperSnake")) {
            return this.camelToSnake(o.toString(), Character::toUpperCase);
        } else if (formatString.equals("snakeToCamel")) {
            return this.snakeToCamel(o.toString(), Character::toLowerCase);
        } else if (formatString.equals("snakeToUpperCamel")) {
            return this.snakeToCamel(o.toString(), Character::toUpperCase);
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

    private String camelToSnake(final String camel, final Function<Character, Character> caseFormat) {
        if (camel.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(camel.length() + camel.length());
        for (int i = 0; i < camel.length(); i++) {
            final char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(sb.length() != 0 ? '_' : "").append(caseFormat.apply(c));
            } else {
                sb.append(caseFormat.apply(c));
            }
        }
        return sb.toString();
    }

    private String snakeToCamel(final String snake, final Function<Character, Character> caseFormat) {
        if (snake.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(snake.length() + snake.length());
        for (int i = 0; i < snake.length(); i++) {
            final char c = snake.charAt(i);
            if (c == '_') {
                sb.append((i + 1) < snake.length() ? Character.toUpperCase(snake.charAt(++i)) : "");
            } else {
                sb.append(sb.length() == 0 ? caseFormat.apply(c) : Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }
}
