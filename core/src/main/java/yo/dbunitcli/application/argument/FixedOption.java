package yo.dbunitcli.application.argument;

import picocli.CommandLine;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class FixedOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {
    @CommandLine.Option(names = "-fixedLength", description = "comma separate column Lengths")
    private String fixedLength;

    public FixedOption(final String prefix) {
        super(prefix);
    }

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
