package yo.dbunitcli.application;

import yo.dbunitcli.application.cli.CommandDto;
import yo.dbunitcli.application.dto.DataSetLoadDto;

public class ConvertDto extends CommandDto {

    private DataSetLoadDto dataSetLoad = new DataSetLoadDto();

    public DataSetLoadDto getDataSetLoad() {
        return this.dataSetLoad;
    }

    public void setDataSetLoad(final DataSetLoadDto dataSetLoad) {
        this.dataSetLoad = dataSetLoad;
    }

}
