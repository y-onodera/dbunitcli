package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.converter.DataSetConverterLoader;

import java.util.Optional;
import java.util.function.UnaryOperator;

public record ResultOption(String resultFile
        , DataSetConverterOption convertResult) {
    public ResultOption(final String resultFile, final DataSetConverterDto dto) {
        this(resultFile
                , new DataSetConverterOption("result", dto));
    }

    public IDataSetConverter converter() {
        return new DataSetConverterLoader().get(this.convertResult.getParam().build());
    }

    public IDataSetConverter converter(final UnaryOperator<DataSetConverterParam.Builder> customizer) {
        return new DataSetConverterLoader().get(customizer.apply(this.convertResult.getParam()).build());
    }

    public String getResultPath() {
        return Optional.ofNullable(this.convertResult.resultPath())
                .filter(it -> !it.isEmpty())
                .orElse(this.resultFile);
    }
}
