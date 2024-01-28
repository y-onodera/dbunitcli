package yo.dbunitcli.application.argument;

import picocli.CommandLine;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class EncodingOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {

    @CommandLine.Option(names = "-encoding", description = "csv file encoding")
    private String encoding = System.getProperty("file.encoding");

    public EncodingOption(final String prefix) {
        super(prefix);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setEncoding(this.encoding);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-encoding", this.encoding);
        return result;
    }

}
