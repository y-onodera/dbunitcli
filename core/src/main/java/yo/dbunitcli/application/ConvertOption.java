package yo.dbunitcli.application;

import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.ResultOption;
import yo.dbunitcli.common.Parameter;

public record ConvertOption(Parameter parameter, ResultOption result,
                            DataSetLoadOption srcData) implements CommandLineOption<ConvertDto> {

    public static ConvertDto toDto(final String[] args) {
        final ConvertDto dto = new ConvertDto();
        new CommandLineParser("", CommandLineOption.DEFAULT_COMMANDLINE_MAPPER, CommandLineOption.DEFAULT_COMMANDLINE_FILTER)
                .parseArgument(args, dto);
        new CommandLineParser("src").parseArgument(args, dto.getSrcData());
        new CommandLineParser("result").parseArgument(args, dto.getConvertResult());
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
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder()
                .addComponent("srcData", this.srcData.toCommandLineArgs())
                .addComponent("convertResult", this.result().convertResult().toCommandLineArgs());
    }

    public void convertDataset() {
        this.getComparableDataSetLoader()
                .loadDataSet(this.srcData.getParam()
                        .setConverter(this.result().converter(it -> it.setResultPath(this.result().getResultPath())))
                        .build());
    }

}
