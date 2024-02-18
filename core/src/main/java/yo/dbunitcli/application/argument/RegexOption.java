package yo.dbunitcli.application.argument;

import picocli.CommandLine;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class RegexOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {

    @CommandLine.Option(names = "-regDataSplit", description = "regex to use split data row")
    private String regDataSplit;

    @CommandLine.Option(names = "-regHeaderSplit", description = "regex to use split header row")
    private String regHeaderSplit;

    public RegexOption(final String prefix) {
        super(prefix);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setDataSplitPattern(this.regDataSplit)
                .setHeaderSplitPattern(this.regHeaderSplit);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-regDataSplit", this.regDataSplit);
        result.put("-regHeaderSplit", this.regHeaderSplit);
        return result;
    }

}
