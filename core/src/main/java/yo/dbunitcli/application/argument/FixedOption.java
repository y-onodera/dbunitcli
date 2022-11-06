package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class FixedOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {
    public FixedOption(final String prefix) {
        super(prefix);
    }

    @Option(name = "-fixedLength", usage = "comma separate column Lengths")
    private String fixedLength;

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setFixedLength(this.fixedLength);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-fixedLength", this.fixedLength);
        return result;
    }
}
