package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

public class Run implements Command<RunOption> {

    public static void main(final String[] strings) throws Exception {
        new Run().exec(strings);
    }

    @Override
    public RunOption getOptions() {
        return new RunOption();
    }

    @Override
    public RunOption getOptions(final Parameter param) {
        return new RunOption(param);
    }

    @Override
    public void exec(final RunOption option) {
        option.runner().run(option.targetFiles());
    }
}
