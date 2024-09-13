package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Optional;

public record CsvOption(
        String prefix
        , char delimiter
        , boolean ignoreQuoted
) implements ComparableDataSetParamOption {

    public CsvOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix
                , Optional.ofNullable(dto.getDelimiter())
                        .filter(Strings::isNotEmpty)
                        .map(it -> it
                                .replace("\\b", "\b")
                                .replace("\\t", "\t")
                                .replace("\\n", "\n")
                                .replace("\\r", "\r")
                                .replace("\\f", "\f")
                                .charAt(0)
                        ).orElse(',')
                , dto.getIgnoreQuoted());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
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
        result.put("-ignoreQuoted", this.ignoreQuoted);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder
                .setDelimiter(this.delimiter)
                .setIgnoreQuoted(this.ignoreQuoted);
    }

}
