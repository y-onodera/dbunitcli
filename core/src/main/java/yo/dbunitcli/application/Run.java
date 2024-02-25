package yo.dbunitcli.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.Parameter;

public class Run implements Command<RunOption> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Run.class);

    public static void main(final String[] strings) throws Exception {
        try {
            new Run().exec(strings);
        } catch (final Throwable th) {
            if (!(th instanceof CommandFailException)) {
                Run.LOGGER.error("error:", th);
            }
            throw th;
        }
    }

    @Override
    public void exec(final RunOption option) {
        option.runner().run(option.targetFiles());
    }

    @Override
    public RunOption getOptions() {
        return new RunOption();
    }

    @Override
    public RunOption getOptions(final Parameter param) {
        return new RunOption(param);
    }
}
