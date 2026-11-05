package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.json.FromJsonXlsxSchemaBuilder;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.FileResources;

import java.nio.charset.Charset;
import java.util.List;

/**
 * xlsxSchemaは-settingと同じく-settingEncodingで読む。-settingEncodingの出力・検証はDataSetLoadOptionが行う
 */
public record ExcelOption(String prefix, String xlsxSchemaSource, String settingEncoding) implements ComparableDataSetParamOption {

    public ExcelOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix
                , dto.getXlsxSchemaSource()
                , Strings.isNotEmpty(dto.getSettingEncoding())
                        ? dto.getSettingEncoding()
                        : Charset.defaultCharset().displayName());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.getPrefix())
                .putFile("-xlsxSchema", this.xlsxSchemaSource, BaseDir.XLSX_SCHEMA);
    }

    @Override
    public List<Violation> validate() {
        return this.validateFileExists("-xlsxSchema", this.xlsxSchemaSource, BaseDir.XLSX_SCHEMA);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        try {
            return builder.setXlsxSchema(new FromJsonXlsxSchemaBuilder()
                    .build(FileResources.searchXlsxSchema(this.xlsxSchemaSource), this.settingEncoding));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
