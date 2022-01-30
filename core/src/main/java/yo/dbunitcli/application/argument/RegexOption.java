package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.LinkedHashMap;
import java.util.Map;

public class RegexOption extends PrefixArgumentsParser implements ComparableDataSetParamOption {

    @Option(name = "-regDataSplit", usage = "regex to use split data row")
    private String regDataSplit;

    @Option(name = "-regHeaderSplit", usage = "regex to use split header row")
    private String regHeaderSplit;

    public RegexOption(String prefix) {
        super(prefix);
    }

    @Override
    public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
        return builder.setDataSplitPattern(this.regDataSplit)
                .setHeaderSplitPattern(this.regHeaderSplit);
    }

    @Override
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = super.expandOption(args);
        result.put("-regDataSplit", this.regDataSplit);
        result.put("-regHeaderSplit", this.regHeaderSplit);
        return result;
    }

}
