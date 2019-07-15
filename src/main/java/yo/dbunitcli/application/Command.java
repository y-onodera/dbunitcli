package yo.dbunitcli.application;

import java.util.Map;

public interface Command<T extends CommandLineOption> {

    default void exec(String[] args) throws Exception {
        T options = this.parseOption(args, getOptions());
        this.exec(options);
    }

    default void exec(ParameterizeOption options, Map<String, Object> param) throws Exception {
        T option = this.parseOption(options.createArgs(param), getOptions(param));
        this.exec(option);
    }

    T getOptions();

    T getOptions(Map<String, Object> param);

    void exec(T options) throws Exception;

    default T parseOption(String[] args, T options) {
        try {
            options.parse(args);
        } catch (Exception exp) {
            throw new AssertionError("option parse failed.", exp);
        }
        return options;
    }

}
