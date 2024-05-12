package yo.dbunitcli.application;

import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.dataset.Parameter;

public class ConvertOption extends CommandLineOption<ConvertDto> {

    private final DataSetLoadOption srcData;

    public static ConvertDto toDto(final String[] args) {
        final ConvertDto dto = new ConvertDto();
        new CommandLineParser("", CommandLineOption.DEFAULT_COMMANDLINE_MAPPER, CommandLineOption.DEFAULT_COMMANDLINE_FILTER)
                .parseArgument(args, dto);
        new CommandLineParser("").parseArgument(args, dto.getSrcData());
        new CommandLineParser("result").parseArgument(args, dto.getConvertResult());
        return dto;
    }

    public ConvertOption(final String resultFile, final ConvertDto dto, final Parameter param) {
        super(resultFile, dto, param);
        this.srcData = new DataSetLoadOption("", dto.getSrcData());
    }

    @Override
    public ConvertDto toDto() {
        return ConvertOption.toDto(this.toArgs(true));
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs();
        result.addComponent("srcData", this.srcData.toCommandLineArgs());
        result.addComponent("convertResult", this.getConvertResult().toCommandLineArgs());
        return result;
    }

    public void convertDataset() {
        this.getComparableDataSetLoader()
                .loadDataSet(this.srcData.getParam()
                        .setConverter(this.converter(it -> it.setResultPath(this.getResultPath())))
                        .build());
    }

}
