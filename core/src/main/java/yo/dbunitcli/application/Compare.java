package yo.dbunitcli.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yo.dbunitcli.dataset.Parameter;

public class Compare implements Command<CompareOption> {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(final String[] args) throws Exception {
        try {
            new Compare().exec(args);
        } catch (final DiffFoundException ex) {
            System.exit(1);
        }
    }

    @Override
    public CompareOption getOptions() {
        return new CompareOption();
    }

    @Override
    public CompareOption getOptions(final Parameter param) {
        return new CompareOption(param);
    }

    @Override
    public void exec(final CompareOption options) {
        final boolean success = options.compare();
        LOGGER.info("compare finish.");
        if (!success) {
            LOGGER.info("unexpected diff found.");
            throw new DiffFoundException();
        }
        LOGGER.info("compare success.");
    }

    private static class DiffFoundException extends RuntimeException {
    }
}