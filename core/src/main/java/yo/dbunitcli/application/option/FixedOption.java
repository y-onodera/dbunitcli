package yo.dbunitcli.application.option;

import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.List;

public record FixedOption(
        String prefix
        , String fixedLength
        , String fixedLengthType
        , String headerName
) implements ComparableDataSetParamOption {

    public FixedOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, dto.getFixedLength(), dto.getFixedLengthType(), dto.getHeaderName());
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

    /**
     * 固定長ファイルはヘッダ行を持たないため、列名は-headerNameで与える
     */
    @Override
    public List<Violation> validate() {
        return Option.concat(this.validateRequired("-fixedLength", this.fixedLength)
                , this.validateIntegers("-fixedLength", this.fixedLength)
                , this.validateRequired("-headerName", this.headerName)
                , this.validateOneOfIgnoreCase("-fixedLengthType", this.fixedLengthType
                        , ComparableDataSetParam.LengthType.names()));
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setFixedLength(this.fixedLength)
                      .setFixedLengthType(this.fixedLengthType);
    }

}
