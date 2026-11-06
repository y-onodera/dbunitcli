package yo.dbunitcli.resource.jexl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.Locale;

public final class NumberParser {

    private static final DecimalFormat PROTOTYPE = createFormat();

    private NumberParser() {
    }

    private static DecimalFormat createFormat() {
        final DecimalFormat format = new DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(Locale.ROOT));
        format.setParseBigDecimal(true);
        return format;
    }

    public static BigDecimal parse(final Object value) {
        final BigDecimal result = NumberParser.parseOrNull(value);
        if (result == null) {
            throw new NumberFormatException("invalid number: " + value);
        }
        return result;
    }

    public static BigDecimal parseOrNull(final Object value) {
        if (value instanceof final BigDecimal decimal) {
            return decimal;
        }
        if (value == null) {
            return null;
        }
        final String val = value.toString();
        // DecimalFormat はスレッドセーフでないため複製して使う
        final DecimalFormat format = (DecimalFormat) NumberParser.PROTOTYPE.clone();
        final ParsePosition pos = new ParsePosition(0);
        final Number result = format.parse(val, pos);
        if (result == null || pos.getIndex() != val.length()) {
            return null;
        }
        return (BigDecimal) result;
    }
}
