package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public class NoneOption implements ComparableDataSetParamOption {
    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder;
    }

    @Override
    public void setUpComponent(final DataSetLoadDto dto) {

    }

}
