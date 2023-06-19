package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class RecursiveOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {

    @Option(name = "-recursive", usage = "default true. whether traversal recursively")
    private String recursive = "true";

    public RecursiveOption(final String prefix) {
        super(prefix);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-recursive", this.recursive);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setRecursive(Boolean.parseBoolean(this.recursive));
    }

}
