package yo.dbunitcli.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yo.dbunitcli.dataset.Parameter;

import java.io.File;
import java.io.IOException;

public class Generate implements Command<GenerateOption> {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] strings) throws Exception {
        new Generate().exec(strings);
    }

    @Override
    public GenerateOption getOptions() {
        return new GenerateOption();
    }

    @Override
    public GenerateOption getOptions(Parameter param) {
        return new GenerateOption(param);
    }

    @Override
    public void exec(GenerateOption options) throws Exception {
        options.parameterStream()
                .forEach(param -> {
                            File resultFile = new File(options.getResultDir(), options.resultPath(param));
                            if (!resultFile.getParentFile().exists()) {
                                resultFile.getParentFile().mkdirs();
                            }
                            try {
                                options.write(resultFile, param);
                            } catch (IOException e) {
                                LOGGER.error("failed write cause:", e);
                                System.exit(1);
                            }
                        }
                );
    }
}
