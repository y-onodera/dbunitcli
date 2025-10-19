package yo.dbunitcli.application;

import yo.dbunitcli.common.Parameter;

public class Compare implements Command<CompareDto, CompareOption> {

    public static void main(final String[] args) {
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
    public CompareDto createDto(final String[] args) {
        return CompareOption.toDto(args);
    }

    @Override
    public CompareOption parseOption(final String resultFile, final CompareDto dto, final Parameter param) {
        return new CompareOption(resultFile, dto, param);
    }

}