package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class FixedOption extends PrefixArgumentsParser implements ComparableDataSetParamOption {
    public FixedOption(String prefix) {
        super(prefix);
    }

    @Option(name = "-fixedLength", usage = "comma separate column Lengths")
    private String fixedLength;

    @Override
    public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
        return builder.setFixedLength(this.fixedLength);
    }

    @Override
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-fixedLength", this.fixedLength);
        return result;
    }
}
