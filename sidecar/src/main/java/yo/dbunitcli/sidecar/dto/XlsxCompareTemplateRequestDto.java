package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

/**
 * 比較用xlsxテンプレート（データシート2枚＋比較シート）の生成リクエスト。
 * 列名は呼び出し側が読み込んだデータセットの列を渡す
 */
@Serdeable
public record XlsxCompareTemplateRequestDto(String fileName, String compareSheetName, Sheet first, Sheet second,
                                            List<ColumnPair> keys, List<ColumnPair> compares) {

    @Serdeable
    public record Sheet(String sheetName, List<String> columns) {
    }

    @Serdeable
    public record ColumnPair(String first, String second) {
    }
}
