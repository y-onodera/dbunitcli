package yo.dbunitcli.dataset;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.introspection.JexlPermissions;

import java.util.Collections;

/**
 * public static メソッドはすべて JEXL 式から接頭辞なしで呼べる関数になる。
 */
public final class JexlFunctions {

    public static final JexlEngine ENGINE = new JexlBuilder()
            // 既定の RESTRICTED 権限ではアプリケーションのクラスを呼べないため、このクラスだけ許可する
            .permissions(new JexlPermissions.ClassPermissions(JexlFunctions.class))
            .namespaces(Collections.singletonMap(null, JexlFunctions.class))
            // 同じ式を行ごとに評価するため、解析結果をキャッシュする
            .cache(256)
            .create();

    private JexlFunctions() {
    }

    public static Object tryToNum(final Object value, final Object defaultValue) {
        final Object result = NumberParser.parseOrNull(value);
        return result != null ? result : defaultValue;
    }
}
