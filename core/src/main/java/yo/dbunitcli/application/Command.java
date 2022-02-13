package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

public interface Command<T extends CommandLineOption> {

    default void exec(String[] args) throws Exception {
        this.exec(this.parseOption(args, getOptions()));
    }

    default void exec(String[] args, Parameter param) throws Exception {
        this.exec(this.parseOption(args, getOptions(param)));
    }

    void exec(T options) throws Exception;

    T getOptions();

    T getOptions(Parameter param);

    default T parseOption(String[] args, T options) {
        try {
            options.parse(args);
        } catch (Exception exp) {
            throw new AssertionError("option parse failed.", exp);
        }
        return options;
    }

}
