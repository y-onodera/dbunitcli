package yo.dbunitcli.application.command;

import yo.dbunitcli.application.Command;
import yo.dbunitcli.common.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Generate implements Command<GenerateDto, GenerateOption> {

    public static void main(final String[] strings) {
        new Generate().exec(strings);
    }

    @Override
    public void execute(final GenerateOption options) {
        final GenerateType.Writer writer;
        try {
            writer = options.writer();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
        // unit=tableでは複数スレッドから呼ばれる
        options.forEachParameter((resultFile, param) -> {
            try {
                Files.createDirectories(resultFile.getParentFile().toPath());
                writer.write(resultFile, param);
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        });
    }

    @Override
    public String resultDir(final String name, final String[] args, final Parameter parameter) {
        return this.parseOption(name, args, parameter).getResultDir().getAbsoluteFile().getPath();
    }

    @Override
    public GenerateDto createDto(final String[] args) {
        return GenerateOption.toDto(args);
    }

    @Override
    public GenerateOption parseOption(final String resultFile, final GenerateDto dto, final Parameter param) {
        return new GenerateOption(resultFile, dto, param);
    }
}
