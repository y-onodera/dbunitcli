package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

public class Convert implements Command<ConvertDto, ConvertOption> {

    public static void main(final String[] args) {
        new Convert().exec(args);
    }

    @Override
    public void exec(final ConvertOption options) {
        options.convertDataset();
    }

    @Override
    public ConvertDto createDto(final String[] args) {
        return ConvertOption.toDto(args);
    }

    @Override
    public ConvertOption parseOption(final String resultFile, final ConvertDto dto, final Parameter param) {
        return new ConvertOption(resultFile, dto, param);
    }
}
