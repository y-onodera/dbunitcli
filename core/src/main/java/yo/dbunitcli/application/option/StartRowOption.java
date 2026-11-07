package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.List;

public record StartRowOption(
        String prefix,
        String startRow
) implements ComparableDataSetParamOption {

    private static final String DEFAULT_START_ROW = "1";

    public StartRowOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, dto.getStartRow());
    }

    @Override
    public String startRow() {
        return Strings.orDefault(this.startRow, DEFAULT_START_ROW);
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
                .putWithDefault("-startRow", this.startRow, DEFAULT_START_ROW);
    }
}