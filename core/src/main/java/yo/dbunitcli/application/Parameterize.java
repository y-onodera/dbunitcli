package yo.dbunitcli.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.Parameter;

public class Parameterize implements Command<ParameterizeDto, ParameterizeOption> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Parameterize.class);

    public static void main(final String[] args) throws Exception {
        new Parameterize().exec(args);
    }

    @Override
    public void exec(final ParameterizeOption options) {
        final int failCount = options.loadParams().map(it -> {
                    try {
                        options.createCommand(it).exec(options.createArgs(it), it);
                    } catch (final Command.CommandFailException fail) {
                        Parameterize.LOGGER.info(fail.getMessage());
                        return 1;
                    } catch (final AssertionError ae) {
                        if (!options.ignoreFail()) {
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
    public ParameterizeDto createDto(final String[] args) {
        return ParameterizeOption.toDto(args);
    }

    @Override
    public ParameterizeOption parseOption(final String resultFile, final ParameterizeDto dto, final Parameter param) {
        return new ParameterizeOption(dto, param);
    }

}
