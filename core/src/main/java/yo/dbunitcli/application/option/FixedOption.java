package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class FixedOption implements ComparableDataSetParamOption {
    private final String prefix;
    private String fixedLength;

    public FixedOption(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setFixedLength(this.fixedLength);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-fixedLength", this.fixedLength);
        return result;
    }

    @Override
    public void setUpComponent(final DataSetLoadDto dto) {
        this.fixedLength = dto.getFixedLength();
    }
}
