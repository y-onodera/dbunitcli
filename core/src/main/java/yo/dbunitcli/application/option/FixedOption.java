package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public record FixedOption(
        String prefix
        , String fixedLength
        , String fixedLengthType
) implements ComparableDataSetParamOption {

    public FixedOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, dto.getFixedLength(), dto.getFixedLengthType());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.getPrefix())
                .put("-fixedLength", this.fixedLength)
                .put("-fixedLengthType", this.fixedLengthType);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setFixedLength(this.fixedLength)
                      .setFixedLengthType(this.fixedLengthType);
    }

}
