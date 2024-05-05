package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

public class Compare implements Command<CompareOption> {

    public static void main(final String[] args) throws Exception {
        new Compare().exec(args);
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