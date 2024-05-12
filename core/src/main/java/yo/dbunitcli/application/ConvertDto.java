package yo.dbunitcli.application;

import yo.dbunitcli.application.dto.DataSetLoadDto;

public class ConvertDto extends CommandDto {

    private DataSetLoadDto srcData = new DataSetLoadDto();

    public DataSetLoadDto getSrcData() {
        return this.srcData;
    }

    public void setSrcData(final DataSetLoadDto srcData) {
        this.srcData = srcData;
    }

}
