package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public record EncodingOption(String prefix, String encoding) implements ComparableDataSetParamOption {

    public EncodingOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, Strings.isNotEmpty(dto.getEncoding())
                ? dto.getEncoding()
                : System.getProperty("file.encoding"));
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-encoding", this.encoding);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setEncoding(this.encoding);
    }

}
