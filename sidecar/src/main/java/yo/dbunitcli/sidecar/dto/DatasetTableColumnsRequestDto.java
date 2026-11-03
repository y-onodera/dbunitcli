package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class DatasetTableColumnsRequestDto extends DatasetTablePreviewRequestDto {

    /**
     * 未保存のcsvqのSQL。指定時はsrcType・srcの代わりに、このSQLをcsvqとして実行したときの列を返す（CSVREADの列の選択用）
     */
    private String inlineQuery;

    public String getInlineQuery() {
        return this.inlineQuery;
    }

    public void setInlineQuery(final String inlineQuery) {
        this.inlineQuery = inlineQuery;
    }
}
