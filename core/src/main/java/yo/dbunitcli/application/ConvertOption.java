package yo.dbunitcli.application;

import yo.dbunitcli.application.cli.CommandLineOption;
import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.dataset.Parameter;

import java.util.Map;

public class ConvertOption extends CommandLineOption<ConvertDto> {

    private final DataSetLoadOption src = new DataSetLoadOption("");

    public ConvertOption() {
        this(Parameter.none());
    }

    public ConvertOption(final Parameter param) {
        super(param);
    }

    public void convertDataset() {
        this.getConverterOption().setResultPath(this.getResultPath());
        this.getComparableDataSetLoader().loadDataSet(this.src.getParam().setConverter(this.converter()).build());
    }

    @Override
    public void parseArgument(final String[] args) {
        final ConvertDto dto = new ConvertDto();
        new CommandLineParser("", this.getArgumentMapper(), this.getArgumentFilter())
                .parseArgument(args, dto);
        new CommandLineParser(this.src.getPrefix()).parseArgument(args, dto.getDataSetLoad());
        new CommandLineParser(this.getConverterOption().getPrefix()).parseArgument(args, dto.getDataSetConverter());
        this.setUpComponent(dto);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(args);
        result.putAll(this.src.createOptionParam(args));
        result.putAll(this.getConverterOption().createOptionParam(args));
        return result;
    }

    @Override
    public void setUpComponent(final ConvertDto dto) {
        super.setUpComponent(dto);
        this.src.setUpComponent(dto.getDataSetLoad());
        this.getConverterOption().setUpComponent(dto.getDataSetConverter());
    }

}
