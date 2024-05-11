package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Optional;

public class CsvOption implements ComparableDataSetParamOption {

    private final String prefix;
    private final char delimiter;

    public CsvOption(final String prefix, final DataSetLoadDto dto) {
        this.prefix = prefix;
        this.delimiter = Optional.ofNullable(dto.getDelimiter())
                .filter(Strings::isNotEmpty)
                .map(it -> it
                        .replace("\\b", "\b")
                        .replace("\\t", "\t")
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\f", "\f")
                        .charAt(0)
                ).orElse(',');
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setDelimiter(this.delimiter);
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-delimiter", String.valueOf(this.delimiter)
                .replace("\b", "\\b")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
        );
        return result;
    }

}
