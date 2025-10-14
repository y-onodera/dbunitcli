package yo.dbunitcli.application;

import yo.dbunitcli.common.Parameter;

import java.io.File;
import java.io.IOException;

public class Generate implements Command<GenerateDto, GenerateOption> {

    public static void main(final String[] strings) {
        new Generate().exec(strings);
    }

    @Override
    public void exec(final GenerateOption options) {
        options.parameterStream()
                .forEach(param -> {
                            final File resultFile = options.resultFile(param);
                            if (!resultFile.getParentFile().exists()) {
                                if (!resultFile.getParentFile().mkdirs()) {
                                    throw new AssertionError("failed create directory " + resultFile.getParentFile());
                                }
                            }
                            try {
                                options.write(resultFile, param);
                            } catch (final IOException e) {
                                throw new AssertionError(e);
                            }
                        }
                );
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
