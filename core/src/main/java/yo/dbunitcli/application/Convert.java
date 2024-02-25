package yo.dbunitcli.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.Parameter;

public class Convert implements Command<ConvertOption> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Convert.class);

    public static void main(final String[] args) throws Exception {
        try {
            new Convert().exec(args);
        } catch (final Throwable th) {
            if (!(th instanceof CommandFailException)) {
                Convert.LOGGER.error("error:", th);
            }
            throw th;
        }
    }

    @Override
    public void exec(final ConvertOption options) {
        options.convertDataset();
    }

    @Override
    public ConvertOption getOptions() {
        return new ConvertOption();
    }

    @Override
    public ConvertOption getOptions(final Parameter param) {
        return new ConvertOption(param);
    }

}
