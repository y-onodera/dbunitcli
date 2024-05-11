package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public class RegexOption implements ComparableDataSetParamOption {
    private final String prefix;

    private final String regDataSplit;

    private final String regHeaderSplit;

    public RegexOption(final String prefix, final DataSetLoadDto dto) {
        this.prefix = prefix;
        this.regHeaderSplit = dto.getRegHeaderSplit();
        this.regDataSplit = dto.getRegDataSplit();
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setDataSplitPattern(this.regDataSplit)
                .setHeaderSplitPattern(this.regHeaderSplit);
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-regDataSplit", this.regDataSplit);
        result.put("-regHeaderSplit", this.regHeaderSplit);
        return result;
    }

}
