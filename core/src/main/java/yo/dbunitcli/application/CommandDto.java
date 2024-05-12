package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.dto.DataSetConverterDto;

import java.util.HashMap;
import java.util.Map;

public class CommandDto {

    @CommandLine.Option(names = "-P")
    private Map<String, String> inputParam = new HashMap<>();

    private DataSetConverterDto convertResult = new DataSetConverterDto();

    public Map<String, String> getInputParam() {
        return this.inputParam;
    }

    public void setInputParam(final Map<String, String> inputParam) {
        this.inputParam = inputParam;
    }

    public DataSetConverterDto getConvertResult() {
        return this.convertResult;
    }

    public void setConvertResult(final DataSetConverterDto convertResult) {
        this.convertResult = convertResult;
    }
}
