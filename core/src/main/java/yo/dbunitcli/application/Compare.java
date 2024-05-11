package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

import java.util.Arrays;

public class Compare implements Command<CompareDto, CompareOption> {

    public static void main(final String[] args) throws Exception {
        new Compare().exec(args);
    }

    @Override
    public void exec(final String[] args) {
        try {
            this.exec(this.parseOption(args, Parameter.none()));
        } catch (final CommandFailException ex) {
            Command.LOGGER.info(ex.getMessage());
            System.exit(1);
        } catch (final Throwable th) {
            Command.LOGGER.error("args:" + Arrays.toString(args));
            Command.LOGGER.error("error:", th);
            throw th;
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
    public CompareDto createDto(final String[] args) {
        return CompareOption.toDto(args);
    }

    @Override
    public CompareOption getOptions(final String resultFile, final CompareDto dto, final Parameter param) {
        return new CompareOption(resultFile, dto, param);
    }

}