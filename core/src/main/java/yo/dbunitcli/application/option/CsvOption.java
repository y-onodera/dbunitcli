package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class CsvOption implements ComparableDataSetParamOption {

    private final String prefix;
    private char delimiter = ',';

    public CsvOption(final String prefix) {
        this.prefix = prefix;
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
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-delimiter", this.delimiter);
        return result;
    }

    @Override
    public void setUpComponent(final DataSetLoadDto dto) {
        if (Strings.isNotEmpty(dto.getDelimiter())) {
            this.delimiter = dto.getDelimiter()
                    .replace("\\b", "\b")
                    .replace("\\t", "\t")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\f", "\f")
                    .charAt(0);
        }
    }
}
