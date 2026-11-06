package yo.dbunitcli.dataset;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.introspection.JexlPermissions;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class JexlFunctions {

    public static final JexlEngine ENGINE = createEngine();

    private JexlFunctions() {
    }

    private static JexlEngine createEngine() {
        // null キーの名前空間は接頭辞なしで呼べる関数になる（Map.of は null キー不可）
        final Map<String, Object> namespaces = new HashMap<>();
        namespaces.put(null, JexlFunctions.class);
        // 既定の RESTRICTED 権限ではアプリケーションのクラスを呼べないため、このクラスだけ許可する
        return new JexlBuilder()
                .permissions(new JexlPermissions.ClassPermissions(JexlFunctions.class))
                .namespaces(namespaces)
                .create();
    }

    public static Object tryToNum(final Object value, final Object defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return JexlFunctions.parseNumber(value.toString());
        } catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }

    public static BigDecimal parseNumber(final String val) {
        // DecimalFormat はスレッドセーフでないため呼び出し毎に生成する
        final DecimalFormat format = new DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(Locale.ROOT));
        format.setParseBigDecimal(true);
        final ParsePosition pos = new ParsePosition(0);
        final Number result = format.parse(val, pos);
        if (result == null || pos.getIndex() != val.length()) {
            throw new NumberFormatException("invalid number: " + val);
        }
        return (BigDecimal) result;
    }
}
