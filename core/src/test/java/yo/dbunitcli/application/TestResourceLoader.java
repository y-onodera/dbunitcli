package yo.dbunitcli.application;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * テストリソースの取得用インターフェース
 */
public interface TestResourceLoader {
    /**
     * テストリソースのディレクトリパスを取得します。
     * target/test-classes → src/test/resources への変換を行います。
     *
     * @return テストリソースのディレクトリパス
     */
    default String getTestResourceDirectory() {
        return URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources");
    }

    /**
     * テストリソースファイルを取得します。
     *
     * @param fileName リソースファイル名
     * @return テストリソースファイル
     * @throws Exception パス解決時に発生する可能性のある例外
     */
    default File getTestResourceFile(final String fileName) throws Exception {
        final File file = new File(this.getTestResourceDirectory(), fileName);
        if (!file.exists()) {
            throw new IllegalArgumentException("テストデータファイルが見つかりません: " + fileName);
        }
        return file;
    }
}