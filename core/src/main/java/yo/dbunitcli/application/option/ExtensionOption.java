package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class ExtensionOption implements ComparableDataSetParamOption {

    private final String prefix;
    private String extension;

    public ExtensionOption(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setExtension(this.extension);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-extension", this.extension);
        return result;
    }

    @Override
    public void setUpComponent(final DataSetLoadDto dto) {
        this.extension = dto.getExtension();
    }

}
