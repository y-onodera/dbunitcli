package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class ExtensionOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {

    @Option(name = "-extension", usage = "target extension")
    private String extension;

    public ExtensionOption(String prefix) {
        super(prefix);
    }

    @Override
    public OptionParam createOptionParam(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-extension", this.extension);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
        return builder.setExtension(this.extension);
    }

}
