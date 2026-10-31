package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
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
                , this.validateColumnCount()
                , this.validateOneOfIgnoreCase("-fixedLengthType", this.fixedLengthType
                        , ComparableDataSetParam.LengthType.values()));
    }

    /**
     * -fixedLengthの数と-headerNameの列数は一致させる
     */
    private List<Violation> validateColumnCount() {
        if (Strings.isEmpty(this.fixedLength) || Strings.isEmpty(this.headerName)) {
            return List.of();
        }
        final int lengthCount = this.fixedLength.split(",").length;
        final int headerCount = this.headerName.split(",").length;
        if (lengthCount == headerCount) {
            return List.of();
        }
        return List.of(new Violation(List.of(this.optionName("-fixedLength"), this.optionName("-headerName"))
                , "Error: " + this.optionLabel("-fixedLength") + " count (" + lengthCount + ") must match "
                + this.optionLabel("-headerName") + " count (" + headerCount + ")"));
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setFixedLength(this.fixedLength)
                      .setFixedLengthType(this.fixedLengthType);
    }

}
