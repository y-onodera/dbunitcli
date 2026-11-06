package yo.dbunitcli.resource.jexl;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.introspection.JexlPermissions;

import java.util.Collections;

public final class JexlEngines {

    /**
     * Dataset Setting の式（filter・追加カラム・結合条件）用。
     */
    public static final JexlEngine DATASET = JexlEngines.withFunctions()
            // 既定の RESTRICTED 権限ではアプリケーションのクラスを呼べないため、関数のクラスだけ許可する
            .permissions(new JexlPermissions.ClassPermissions(JexlFunctions.class))
            .create();

    /**
     * Excel テンプレートの式用。jxls 既定の評価器と同じ silent / strict / 権限に揃える。
     */
    public static final JexlEngine TEMPLATE = JexlEngines.withFunctions()
            .silent(true)
            .strict(false)
            .permissions(JexlPermissions.UNRESTRICTED)
            .create();

    private JexlEngines() {
    }

    private static JexlBuilder withFunctions() {
        return new JexlBuilder()
                .namespaces(Collections.singletonMap(null, JexlFunctions.class))
                // 同じ式を行ごとに評価するため、解析結果をキャッシュする
                .cache(256);
    }
}
