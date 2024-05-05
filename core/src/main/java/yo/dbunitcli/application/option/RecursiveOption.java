package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class RecursiveOption implements ComparableDataSetParamOption {

    private final String prefix;
    private String recursive = "true";

    public RecursiveOption(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setRecursive(Boolean.parseBoolean(this.recursive));
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-recursive", this.recursive);
        return result;
    }

    @Override
    public void setUpComponent(final DataSetLoadDto dto) {
        if (Strings.isNotEmpty(dto.getRecursive())) {
            this.recursive = dto.getRecursive();
        }
    }

}
