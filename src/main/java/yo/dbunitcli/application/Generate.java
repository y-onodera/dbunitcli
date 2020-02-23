package yo.dbunitcli.application;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ErrorManager;

import java.io.File;
import java.util.Map;

public class Generate implements Command<GenerateOption> {

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
        for (Map<String, Object> param : options.targetDataSet().toMap()) {
            File resultFile = new File(".", options.resultPath(param));
            if (!resultFile.getParentFile().exists()) {
                resultFile.getParentFile().mkdirs();
            }
            options.getTemplate(param)
                    .write(resultFile, ErrorManager.DEFAULT_ERROR_LISTENER, options.getOutputEncoding());
        }
    }
}
