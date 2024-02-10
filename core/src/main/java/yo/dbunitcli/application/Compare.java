package yo.dbunitcli.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.Parameter;

public class Compare implements Command<CompareOption> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Compare.class);

    public static void main(final String[] args) throws Exception {
        try {
            new Compare().exec(args);
        } catch (final CommandFailException ex) {
            Compare.LOGGER.info(ex.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void exec(final CompareOption options) {
        final boolean success = options.compare();
        Compare.LOGGER.info("compare finish.");
        if (!success) {
            throw new CommandFailException("unexpected diff found.");
        }
        Compare.LOGGER.info("compare success.");
    }

    @Override
    public CompareOption getOptions() {
        return new CompareOption();
    }

    @Override
    public CompareOption getOptions(final Parameter param) {
        return new CompareOption(param);
    }
}