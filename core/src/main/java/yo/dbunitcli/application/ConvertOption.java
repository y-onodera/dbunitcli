package yo.dbunitcli.application;

import yo.dbunitcli.application.cli.ArgumentMapper;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.ResultOption;
import yo.dbunitcli.common.Parameter;

public record ConvertOption(Parameter parameter, ResultOption result,
                            DataSetLoadOption srcData) implements CommandLineOption<ConvertDto> {

    public static ConvertDto toDto(final String[] args) {
        final ConvertDto dto = new ConvertDto();
        new ArgumentMapper("", CommandLineOption.ARGUMENT_FUNCTION, CommandLineOption.ARGUMENT_FILTER)
                .populate(args, dto);
        new ArgumentMapper("src").populate(args, dto.getSrcData());
        new ArgumentMapper("result").populate(args, dto.getConvertResult());
        return dto;
    }

    public ConvertOption(final String resultFile, final ConvertDto dto, final Parameter param) {
        this(param
                , new ResultOption(resultFile, dto.getConvertResult())
                , new DataSetLoadOption("src", dto.getSrcData()));
    }

    @Override
    public ConvertDto toDto() {
        return ConvertOption.toDto(this.toArgs(true));
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder()
                .addComponent("srcData", this.srcData.toParameters())
                .addComponent("convertResult", this.result().convertResult().toParameters());
    }

    public void convertDataset() {
        this.getComparableDataSetLoader()
                .loadDataSet(this.srcData.getParam()
                        .setConverter(this.result().converter(it -> it.setResultPath(this.result().getResultPath())))
                        .build());
    }

}
