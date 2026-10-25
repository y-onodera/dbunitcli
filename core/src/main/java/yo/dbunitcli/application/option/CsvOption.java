package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.List;
import java.util.Optional;

public record CsvOption(
        String prefix
        , String delimiter
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
                        ).orElse(",")
                , dto.getIgnoreQuoted());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.getPrefix())
                .put("-delimiter", this.delimiter
                        .replace("\b", "\\b")
                        .replace("\t", "\\t")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\f", "\\f")
                )
                .put("-ignoreQuoted", this.ignoreQuoted);
    }

    /**
     * エスケープ（\t等）を戻した後の区切り文字は1文字でなければならない
     */
    @Override
    public List<Violation> validate() {
        return this.validateSingleChar("-delimiter", this.delimiter);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder
                .setDelimiter(this.delimiter.charAt(0))
                .setIgnoreQuoted(this.ignoreQuoted);
    }

}
