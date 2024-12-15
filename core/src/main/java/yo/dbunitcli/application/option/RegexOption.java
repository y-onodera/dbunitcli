package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public record RegexOption(String prefix, String regHeaderSplit,
                          String regDataSplit) implements ComparableDataSetParamOption {

    public RegexOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, dto.getRegHeaderSplit(), dto.getRegDataSplit());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .put("-regDataSplit", this.regDataSplit)
                .put("-regHeaderSplit", this.regHeaderSplit);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setDataSplitPattern(this.regDataSplit)
                .setHeaderSplitPattern(this.regHeaderSplit);
    }

}
