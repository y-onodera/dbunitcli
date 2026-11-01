package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

/**
 * generateの-unitSettingのプレビュー。srcの読込で得たtableNameのテーブルに、unitSettingを適用した結果を求める
 */
@Serdeable
public class DatasetUnitTablePreviewRequestDto extends DatasetTablePreviewRequestDto {

    /**
     * generateの-unitSettingの値（settingの基準ディレクトリ→workspaceの順に探す）
     */
    private String unitSetting;

    /**
     * generateの-unitSettingEncodingの値。空ならシステムの既定
     */
    private String unitSettingEncoding;

    public String getUnitSetting() {
        return this.unitSetting;
    }

    public void setUnitSetting(final String unitSetting) {
        this.unitSetting = unitSetting;
    }

    public String getUnitSettingEncoding() {
        return this.unitSettingEncoding;
    }

    public void setUnitSettingEncoding(final String unitSettingEncoding) {
        this.unitSettingEncoding = unitSettingEncoding;
    }
}
