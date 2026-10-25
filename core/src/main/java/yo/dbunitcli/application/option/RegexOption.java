package yo.dbunitcli.application.option;

import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.List;

public record RegexOption(String prefix, String regHeaderSplit,
                          String regDataSplit, String headerName) implements ComparableDataSetParamOption {

    public RegexOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, dto.getRegHeaderSplit(), dto.getRegDataSplit(), dto.getHeaderName());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.getPrefix())
                .put("-regDataSplit", this.regDataSplit)
                .put("-regHeaderSplit", this.regHeaderSplit);
    }

    /**
     * データ行の分割には-regDataSplitが要る。ヘッダ行は-headerNameで列名を与えないときだけ-regHeaderSplitで分割する
     */
    @Override
    public List<Violation> validate() {
        return Option.concat(this.validateRequired("-regDataSplit", this.regDataSplit)
                , this.validatePattern("-regDataSplit", this.regDataSplit)
                , this.validateRequiredEither("-regHeaderSplit", this.regHeaderSplit, "-headerName", this.headerName)
                , this.validatePattern("-regHeaderSplit", this.regHeaderSplit));
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setDataSplitPattern(this.regDataSplit)
                .setHeaderSplitPattern(this.regHeaderSplit);
    }

}
