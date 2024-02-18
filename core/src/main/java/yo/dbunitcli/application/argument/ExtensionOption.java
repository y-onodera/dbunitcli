package yo.dbunitcli.application.argument;

import picocli.CommandLine;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class ExtensionOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {

    @CommandLine.Option(names = "-extension", description = "target extension")
    private String extension;

    public ExtensionOption(final String prefix) {
        super(prefix);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-extension", this.extension);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setExtension(this.extension);
    }

}
