package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

public class Run implements Command<RunOption> {

    @Override
    public RunOption getOptions() {
        return this.getOptions(Parameter.NONE);
    }

    @Override
    public RunOption getOptions(Parameter param) {
        return new RunOption(param);
    }

    @Override
    public void exec(RunOption option) throws Exception {
        option.runner().run(option.targetFiles());
    }
}
