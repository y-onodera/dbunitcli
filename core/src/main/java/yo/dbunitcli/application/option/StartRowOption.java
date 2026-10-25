package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.List;

public record StartRowOption(
        String prefix,
        String startRow
) implements ComparableDataSetParamOption {

    public StartRowOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, Strings.isNotEmpty(dto.getStartRow()) ? dto.getStartRow() : "1");
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
        return builder.setStartRow(Integer.parseInt(this.startRow.trim()));
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.prefix)
                .put("-startRow", this.startRow);
    }
}