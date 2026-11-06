package yo.dbunitcli.resource.jexl;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * public static メソッドはすべて JEXL 式から接頭辞なしで呼べる関数になる。
 * 内部用の処理はここに置かない。
 */
public final class JexlFunctions {

    private static final Map<String, DateTimeFormatter> PARSERS = new ConcurrentHashMap<>();

    private static final Map<String, DateTimeFormatter> FORMATTERS = new ConcurrentHashMap<>();

    private JexlFunctions() {
    }

    private static DateTimeFormatter strictParser(final String pattern) {
        // STRICT は 2/30 等を読めない値として扱うが、yyyy（暦年）は紀元がないと日付に解決できないため西暦を補う
        return new DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .parseDefaulting(ChronoField.ERA, 1)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);
    }

    public static Object tryToNum(final Object value, final Object defaultValue) {
        final Object result = NumberParser.parseOrNull(value);
        return result != null ? result : defaultValue;
    }

    public static Object formatDate(final Object value, final String fromPattern, final String toPattern) {
        return JexlFunctions.formatDate(value, fromPattern, toPattern, value);
    }

    public static Object formatDate(final Object value, final String fromPattern, final String toPattern, final Object defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        final DateTimeFormatter from = JexlFunctions.PARSERS.computeIfAbsent(fromPattern, JexlFunctions::strictParser);
        final DateTimeFormatter to = JexlFunctions.FORMATTERS.computeIfAbsent(toPattern, DateTimeFormatter::ofPattern);
        try {
            return to.format(from.parse(value.toString()));
        } catch (final DateTimeParseException ex) {
            return defaultValue;
        }
    }
}
