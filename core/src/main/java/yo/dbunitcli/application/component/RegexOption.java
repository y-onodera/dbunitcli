package yo.dbunitcli.application.component;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public class RegexOption extends PrefixArgumentsParser implements ComparableDataSetParamOption{

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

    public String getRegDataSplit() {
        return this.regDataSplit;
    }

    public String getRegHeaderSplit() {
        return this.regHeaderSplit;
    }

}
