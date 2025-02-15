package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

public class Run implements Command<RunDto, RunOption> {

    public static void main(final String[] strings) {
        new Run().exec(strings);
    }

    @Override
    public void exec(final RunOption option) {
        option.runner().run(option.targetFiles());
    }

    @Override
    public RunDto createDto(final String[] args) {
        return RunOption.toDto(args);
    }

    @Override
    public RunOption parseOption(final String resultFile, final RunDto dto, final Parameter param) {
        return new RunOption(dto, param);
    }
}


