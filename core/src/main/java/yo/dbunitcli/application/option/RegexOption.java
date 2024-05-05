package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class RegexOption implements ComparableDataSetParamOption {
    private final String prefix;

    private String regDataSplit;

    private String regHeaderSplit;

    public RegexOption(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setDataSplitPattern(this.regDataSplit)
                .setHeaderSplitPattern(this.regHeaderSplit);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-regDataSplit", this.regDataSplit);
        result.put("-regHeaderSplit", this.regHeaderSplit);
        return result;
    }

    @Override
    public void setUpComponent(final DataSetLoadDto dto) {
        this.regHeaderSplit = dto.getRegHeaderSplit();
        this.regDataSplit = dto.getRegDataSplit();
    }

}
