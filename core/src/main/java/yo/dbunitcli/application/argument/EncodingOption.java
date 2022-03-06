package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class EncodingOption extends PrefixArgumentsParser implements ComparableDataSetParamOption {

    @Option(name = "-encoding", usage = "csv file encoding")
    private String encoding = System.getProperty("file.encoding");

    public EncodingOption(String prefix) {
        super(prefix);
    }

    @Override
    public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
        return builder.setEncoding(this.encoding);
    }

    @Override
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-encoding", this.encoding);
        return result;
    }

}
