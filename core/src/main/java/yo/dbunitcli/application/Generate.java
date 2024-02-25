package yo.dbunitcli.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.Parameter;

import java.io.File;
import java.io.IOException;

public class Generate implements Command<GenerateOption> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Generate.class);

    public static void main(final String[] strings) throws Exception {
        try {
            new Generate().exec(strings);
        } catch (final Throwable th) {
            if (!(th instanceof CommandFailException)) {
                Generate.LOGGER.error("error:", th);
            }
            throw th;
        }
    }

    @Override
    public void exec(final GenerateOption options) {
        options.parameterStream()
                .forEach(param -> {
                            final File resultFile = new File(options.getResultDir(), options.resultPath(param));
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
    public GenerateOption getOptions() {
        return new GenerateOption();
    }

    @Override
    public GenerateOption getOptions(final Parameter param) {
        return new GenerateOption(param);
    }
}
