package yo.dbunitcli.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yo.dbunitcli.dataset.Parameter;

public class ParameterizeExecute {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(final String[] args) throws Exception {
        final ParameterizeOption options = new ParameterizeOption();
        try {
            options.parse(args);
        } catch (final Exception exp) {
            throw new AssertionError("option parse failed.", exp);
        }
        final Integer[] rowNum = new Integer[]{0};
        options.loadParams().forEach(it -> {
            final Parameter parameter = new Parameter(rowNum[0]++, it);
            try {
                options.createCommand(it).exec(options.createArgs(parameter), parameter);
            } catch (final Command.CommandFailException fail) {
                if (!options.isIgnoreFail()) {
                    throw fail;
                } else {
                    LOGGER.info(fail.getMessage());
                }
            }
        });
    }

}
