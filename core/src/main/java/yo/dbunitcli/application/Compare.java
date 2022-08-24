package yo.dbunitcli.application;

import org.dbunit.DatabaseUnitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.Parameter;

public class Compare implements Command<CompareOption> {

    private static final Logger logger = LoggerFactory.getLogger(Compare.class);

    public static void main(String[] args) throws Exception {
        new Compare().exec(args);
    }

    @Override
    public CompareOption getOptions() {
        return new CompareOption();
    }

    @Override
    public CompareOption getOptions(Parameter param) {
        return new CompareOption(param);
    }

    @Override
    public void exec(CompareOption options) throws DatabaseUnitException {
        options.compare();
        logger.info("compare success.");
    }
}