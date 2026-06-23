package yo.dbunitcli.application.command;

import yo.dbunitcli.application.Command;
import yo.dbunitcli.common.Parameter;

import java.io.IOException;

public class Scaffold implements Command<ScaffoldDto, ScaffoldOption> {

    public static void main(final String[] strings) {
        new Scaffold().exec(strings);
    }

    @Override
    public void exec(final ScaffoldOption options) {
        try {
            options.execute();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String resultDir(final String name, final String[] args, final Parameter parameter) {
        return this.parseOption(name, args, parameter).getResultDir().getAbsoluteFile().getPath();
    }

    @Override
    public ScaffoldDto createDto(final String[] args) {
        return ScaffoldOption.toDto(args);
    }

    @Override
    public ScaffoldOption parseOption(final String resultFile, final ScaffoldDto dto, final Parameter param) {
        return new ScaffoldOption(resultFile, dto, param);
    }
}
