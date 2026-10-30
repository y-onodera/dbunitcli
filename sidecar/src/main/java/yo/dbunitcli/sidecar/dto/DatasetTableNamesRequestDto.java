package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Map;

@Serdeable
public class DatasetTableNamesRequestDto {

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
}
