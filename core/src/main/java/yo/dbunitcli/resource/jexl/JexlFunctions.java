package yo.dbunitcli.resource.jexl;

/**
 * public static メソッドはすべて JEXL 式から接頭辞なしで呼べる関数になる。
 * 内部用の処理はここに置かない。
 */
public final class JexlFunctions {

    private JexlFunctions() {
    }

    public static Object tryToNum(final Object value, final Object defaultValue) {
        final Object result = NumberParser.parseOrNull(value);
        return result != null ? result : defaultValue;
    }
}
