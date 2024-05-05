package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class HeaderNameOption implements ComparableDataSetParamOption {

    private final String prefix;
    private String headerName;

    public HeaderNameOption(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setHeaderName(this.headerName);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-headerName", this.headerName);
        return result;
    }

    @Override
    public void setUpComponent(final DataSetLoadDto dto) {
        this.headerName = dto.getHeaderName();
    }

}
