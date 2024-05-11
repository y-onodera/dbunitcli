package yo.dbunitcli.application;

import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.dataset.Parameter;

public class ConvertOption extends CommandLineOption<ConvertDto> {

    private final DataSetLoadOption src;

    public static ConvertDto toDto(final String[] args) {
        final ConvertDto dto = new ConvertDto();
        new CommandLineParser("", CommandLineOption.DEFAULT_COMMANDLINE_MAPPER, CommandLineOption.DEFAULT_COMMANDLINE_FILTER)
                .parseArgument(args, dto);
        new CommandLineParser("").parseArgument(args, dto.getDataSetLoad());
        new CommandLineParser("result").parseArgument(args, dto.getDataSetConverter());
        return dto;
    }

    public ConvertOption(final String resultFile, final ConvertDto dto, final Parameter param) {
        super(resultFile, dto, param);
        this.src = new DataSetLoadOption("", dto.getDataSetLoad());
    }

    @Override
    public ConvertDto toDto() {
        return ConvertOption.toDto(this.toArgs(true));
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs();
        result.putAll(this.src.toCommandLineArgs());
        result.putAll(this.getConverterOption().toCommandLineArgs());
        return result;
    }

    public void convertDataset() {
        this.getComparableDataSetLoader()
                .loadDataSet(this.src.getParam()
                        .setConverter(this.converter(it -> it.setResultPath(this.getResultPath())))
                        .build());
    }

}
