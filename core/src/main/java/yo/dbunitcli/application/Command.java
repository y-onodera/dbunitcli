package yo.dbunitcli.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.Parameter;

import java.util.Arrays;

public interface Command<T extends CommandLineOption<?>> {

    Logger LOGGER = LoggerFactory.getLogger(Command.class);

    default void exec(final String[] args) {
        try {
            this.exec(this.parseOption(args, this.getOptions()));
        } catch (final CommandFailException ex) {
            Command.LOGGER.info(ex.getMessage());
            System.exit(1);
        } catch (final Throwable th) {
            Command.LOGGER.error("args:" + Arrays.toString(args));
            Command.LOGGER.error("error:", th);
            throw th;
        }
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
