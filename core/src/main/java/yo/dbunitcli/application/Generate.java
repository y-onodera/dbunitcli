package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

import java.io.File;
import java.io.IOException;

public class Generate implements Command<GenerateOption> {

    public static void main(final String[] strings) throws Exception {
        new Generate().exec(strings);
    }

    @Override
    public GenerateOption getOptions() {
        return new GenerateOption();
    }

    @Override
    public GenerateOption getOptions(final Parameter param) {
        return new GenerateOption(param);
    }

    @Override
    public void exec(final GenerateOption options) {
        options.parameterStream()
                .forEach(param -> {
                            final File resultFile = new File(options.getResultDir(), options.resultPath(param));
                            if (!resultFile.getParentFile().exists()) {
                                resultFile.getParentFile().mkdirs();
                            }
                            try {
                                options.write(resultFile, param);
                            } catch (final IOException e) {
                                throw new AssertionError(e);
                            }
                        }
                );
    }
}
