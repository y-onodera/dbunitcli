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
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-regDataSplit", this.regDataSplit);
        result.put("-regHeaderSplit", this.regHeaderSplit);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setDataSplitPattern(this.regDataSplit)
                .setHeaderSplitPattern(this.regHeaderSplit);
    }

}
