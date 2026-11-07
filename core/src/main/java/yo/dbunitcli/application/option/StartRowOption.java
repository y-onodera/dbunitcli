package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.List;

/**
 * @param startRow 入力値。既定値はアクセサで適用する
 */
public record StartRowOption(
        String prefix,
        String startRow
) implements ComparableDataSetParamOption {

    public StartRowOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, dto.getStartRow());
    }

    @Override
    public String startRow() {
        return Strings.isNotEmpty(this.startRow) ? this.startRow : "1";
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public List<Violation> validate() {
        return this.validateInteger("-startRow", this.startRow);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setStartRow(Integer.parseInt(this.startRow().trim()));
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.prefix)
                .putWithDefault("-startRow", this.startRow, "1");
    }
}