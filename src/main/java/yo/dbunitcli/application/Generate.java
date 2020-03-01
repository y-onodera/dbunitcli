package yo.dbunitcli.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.Parameter;

import java.io.File;
import java.io.IOException;

public class Generate implements Command<GenerateOption> {

    private static final Logger logger = LoggerFactory.getLogger(Generate.class);

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
                            File resultFile = new File(".", options.resultPath(param));
                            if (!resultFile.getParentFile().exists()) {
                                resultFile.getParentFile().mkdirs();
                            }
                            try {
                                options.write(resultFile, param);
                            } catch (IOException e) {
                                logger.error("failed write cause:", e);
                                System.exit(1);
                            }
                        }
                );
    }
}
