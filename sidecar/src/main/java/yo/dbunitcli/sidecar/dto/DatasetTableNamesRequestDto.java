package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Map;

@Serdeable
public class DatasetTableNamesRequestDto {

    /**
     * sql・csvqのinlineQueryを読み込んだテーブルの名前
     */
    public static final String INLINE_QUERY_TABLE_NAME = "inlineQuery";

    private Map<String, String> parameters;

    /**
     * データセット読込フォームの値（srcType・src・setting等のオプション名→値）。-src.&lt;オプション名&gt;として解釈する
     */
    private Map<String, String> srcOptions;

    /**
     * 未保存の設定1件。指定時はsettingより優先し、この設定の対象のテーブルだけをこの設定を適用して読み込む（設定ダイアログのプレビュー用）
     */
    private DatasetSettingDto inlineSetting;

    /**
     * 未保存のxlsxスキーマ。指定時はxlsxSchemaより優先し、このスキーマの対象のシート・テーブルだけを読み込む（xlsxスキーマの設定ダイアログのプレビュー用）
     */
    private JsonXlsxSchemaDto inlineXlsxSchema;

    /**
     * 未保存のsrcの内容（srcTypeがsql・table・csvqのときのみ）。指定時はsrcのファイルの代わりに読む。
     * sql・csvqはSQLを{@link #INLINE_QUERY_TABLE_NAME}のテーブルとして、tableは1行1テーブル名として読む（SQL Editorのプレビュー・CSVREADの列の選択用）
     */
    private String inlineQuery;

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getSrcOptions() {
        return this.srcOptions;
    }

    public void setSrcOptions(final Map<String, String> srcOptions) {
        this.srcOptions = srcOptions;
    }

    public DatasetSettingDto getInlineSetting() {
        return this.inlineSetting;
    }

    public void setInlineSetting(final DatasetSettingDto inlineSetting) {
        this.inlineSetting = inlineSetting;
    }

    public JsonXlsxSchemaDto getInlineXlsxSchema() {
        return this.inlineXlsxSchema;
    }

    public void setInlineXlsxSchema(final JsonXlsxSchemaDto inlineXlsxSchema) {
        this.inlineXlsxSchema = inlineXlsxSchema;
    }

    public String getInlineQuery() {
        return this.inlineQuery;
    }

    public void setInlineQuery(final String inlineQuery) {
        this.inlineQuery = inlineQuery;
    }
}
