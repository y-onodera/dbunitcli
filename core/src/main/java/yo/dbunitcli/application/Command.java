package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

public interface Command<T extends CommandLineOption> {

    default void exec(final String[] args) {
        this.exec(this.parseOption(args, this.getOptions()));
    }

    default void exec(final String[] args, final Parameter param) {
        this.exec(this.parseOption(args, this.getOptions(param)));
    }

    void exec(T options);

    T getOptions();

    T getOptions(Parameter param);

    default T parseOption(final String[] args, final T options) {
        try {
            options.parse(args);
        } catch (final Exception exp) {
            throw new AssertionError("option parse failed.", exp);
        }
        return options;
    }

    class CommandFailException extends RuntimeException {
        private final String message;

        public CommandFailException(final String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }
}
