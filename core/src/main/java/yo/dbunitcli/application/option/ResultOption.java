package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.ResultType;
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

    /**
     * 出力先ディレクトリの絶対パス（DB出力なら空文字）
     */
    public String resultDir() {
        if (this.convertResult.resultType() == ResultType.table) {
            return "";
        }
        return this.convertResult.getResultDir().getAbsoluteFile().getPath();
    }

    public String getResultPath() {
        return Optional.ofNullable(this.convertResult.resultPath())
                .filter(it -> !it.isEmpty())
                .orElse(this.resultFile);
    }
}
