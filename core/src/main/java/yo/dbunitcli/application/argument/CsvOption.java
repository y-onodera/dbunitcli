package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class CsvOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {

    @Option(name = "-delimiter", usage = "default is comma", handler = EscapeSequenceEnableCharOptionHandler.class)
    private char delimiter = ',';

    public CsvOption(String prefix) {
        super(prefix);
    }

    @Override
    public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
        return builder.setDelimiter(this.delimiter);
    }

    @Override
    public OptionParam createOptionParam(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-delimiter", this.delimiter);
        return result;
    }
}
