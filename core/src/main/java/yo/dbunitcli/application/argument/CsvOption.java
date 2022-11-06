package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class CsvOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {

    @Option(name = "-delimiter", usage = "default is comma", handler = EscapeSequenceEnableCharOptionHandler.class)
    private char delimiter = ',';

    public CsvOption(final String prefix) {
        super(prefix);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setDelimiter(this.delimiter);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-delimiter", this.delimiter);
        return result;
    }
}
