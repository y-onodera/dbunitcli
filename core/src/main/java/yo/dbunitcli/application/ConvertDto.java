package yo.dbunitcli.application;

import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.application.dto.DataSetLoadDto;

public class ConvertDto extends CommandDto {

    private DataSetLoadDto srcData = new DataSetLoadDto();

    private DataSetConverterDto convertResult = new DataSetConverterDto();

    public DataSetConverterDto getConvertResult() {
        return this.convertResult;
    }

    public void setConvertResult(final DataSetConverterDto convertResult) {
        this.convertResult = convertResult;
    }

    public DataSetLoadDto getSrcData() {
        return this.srcData;
    }

    public void setSrcData(final DataSetLoadDto srcData) {
        this.srcData = srcData;
    }

}
