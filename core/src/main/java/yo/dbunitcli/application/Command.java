package yo.dbunitcli.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface Command<DTO extends CommandDto, T extends CommandLineOption<DTO>> {

    Logger LOGGER = LoggerFactory.getLogger(Command.class);

    default void exec(final String[] args) {
        try {
            this.exec(this.parseOption(args, Parameter.none()));
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
        this.exec(this.parseOption(args, param));
    }

    default void exec(final String resultFile, final String[] args, final Parameter param) {
        this.exec(this.parseOption(resultFile, args, param));
    }

    void exec(T options);

    DTO createDto(String[] args);

    default T parseOption(final String[] args) {
        return this.parseOption(args, Parameter.NONE);
    }

    default T parseOption(final String[] args, final Parameter param) {
        return this.parseOption(null, args, param);
    }

    default T parseOption(final String result, final String[] args, final Parameter param) {
        final DTO dto = this.createDto(this.getExpandArgs(args));
        return this.parseOption(Optional.ofNullable(result)
                .filter(Strings::isNotEmpty)
                .orElseGet(() -> {
                            String resultFile = "result";
                            if (args.length > 0 && args[0].startsWith("@")) {
                                resultFile = FileResources.resultDir(args[0].replace("@", "")).getName();
                                resultFile = resultFile.substring(0, resultFile.lastIndexOf("."));
                            }
                            return resultFile;
                        }
                ), dto, param.addAll(dto.getInputParam()));
    }

    T parseOption(String resultFile, DTO dto, final Parameter param);

    default String[] getExpandArgs(final String[] args) {
        final List<String> result = new ArrayList<>();
        for (final String arg : args) {
            if (arg.startsWith("@")) {
                final File file = FileResources.searchWorkspace(arg.substring(1));
                if (!file.exists()) {
                    throw new AssertionError("file not exists :" + file.getPath());
                }
                try {
                    result.addAll(Files.readAllLines(file.toPath()));
                } catch (final IOException ex) {
                    throw new AssertionError("Failed to parse " + file, ex);
                }
            } else {
                result.add(arg);
            }
        }
        return result.toArray(new String[0]);
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
