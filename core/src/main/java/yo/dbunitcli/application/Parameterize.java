package yo.dbunitcli.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.Parameter;

public class Parameterize implements Command<ParameterizeOption> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Parameterize.class);

    public static void main(final String[] args) throws Exception {
        try {
            new Parameterize().exec(args);
        } catch (final Command.CommandFailException fail) {
            System.exit(1);
        }
    }

    @Override
    public void exec(final ParameterizeOption options) {
        final Integer[] rowNum = new Integer[]{0};
        final int failCount = options.loadParams().map(it -> {
                    final Parameter parameter = new Parameter(rowNum[0]++, it);
                    try {
                        options.createCommand(it).exec(options.createArgs(parameter), parameter);
                    } catch (final Command.CommandFailException fail) {
                        Parameterize.LOGGER.info(fail.getMessage());
                        return 1;
                    } catch (final AssertionError ae) {
                        if (!options.isIgnoreFail()) {
                            throw ae;
                        } else {
                            Parameterize.LOGGER.info(ae.getMessage());
                            return 1;
                        }
                    }
                    return 0;
                })
                .reduce(0, Integer::sum);
        if (failCount > 0) {
            Parameterize.LOGGER.info("total fail count:" + failCount);
            throw new Command.CommandFailException("execute has fail count");
        }
    }

    @Override
    public ParameterizeOption getOptions() {
        return new ParameterizeOption();
    }

    @Override
    public ParameterizeOption getOptions(final Parameter param) {
        return new ParameterizeOption(param);
    }
}
