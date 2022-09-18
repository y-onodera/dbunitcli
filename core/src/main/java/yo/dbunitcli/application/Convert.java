package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

public class Convert implements Command<ConvertOption>{

    public static void main(String[] args) throws Exception {
        new Convert().exec(args);
    }

    @Override
    public ConvertOption getOptions() {
        return new ConvertOption();
    }

    @Override
    public ConvertOption getOptions(Parameter param) {
        return new ConvertOption(param);
    }

    @Override
    public void exec(ConvertOption options) throws Exception {
        options.convertDataset();
    }

}
