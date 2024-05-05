package yo.dbunitcli.application.cli;

import picocli.CommandLine;
import yo.dbunitcli.application.dto.DataSetConverterDto;

import java.util.HashMap;
import java.util.Map;

public class CommandDto {

    @CommandLine.Option(names = "-P")
    private Map<String, String> inputParam = new HashMap<>();

    private DataSetConverterDto dataSetConverter = new DataSetConverterDto();

    public Map<String, String> getInputParam() {
        return this.inputParam;
    }

    public void setInputParam(final Map<String, String> inputParam) {
        this.inputParam = inputParam;
    }

    public DataSetConverterDto getDataSetConverter() {
        return this.dataSetConverter;
    }

    public void setDataSetConverter(final DataSetConverterDto dataSetConverter) {
        this.dataSetConverter = dataSetConverter;
    }
}
