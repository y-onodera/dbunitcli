package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public class EncodingOption implements ComparableDataSetParamOption {
    private final String prefix;

    private final String encoding;

    public EncodingOption(final String prefix, final DataSetLoadDto dto) {
        this.prefix = prefix;
        if (Strings.isNotEmpty(dto.getEncoding())) {
            this.encoding = dto.getEncoding();
        } else {
            this.encoding = System.getProperty("file.encoding");
        }
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setEncoding(this.encoding);
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-encoding", this.encoding);
        return result;
    }

}
