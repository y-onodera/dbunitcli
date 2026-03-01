package yo.dbunitcli.application.command;

import yo.dbunitcli.application.Command;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ResultType;

public class Convert implements Command<ConvertDto, ConvertOption> {

    public static void main(final String[] args) {
        new Convert().exec(args);
    }

    @Override
    public void exec(final ConvertOption options) {
        options.convertDataset();
    }

    @Override
    public String resultDir(final String name, final String[] args, final Parameter parameter) {
        var options = this.parseOption(name, args, parameter);
        if (options.result().convertResult().resultType() == ResultType.table) {
            return "";
        }
        return options.result().convertResult().getResultDir().getAbsoluteFile().getPath();
    }

    @Override
    public ConvertDto createDto(final String[] args) {
        return ConvertOption.toDto(args);
    }

    @Override
    public ConvertOption parseOption(final String resultFile, final ConvertDto dto, final Parameter param) {
        return new ConvertOption(resultFile, dto, param);
    }
}
