package yo.dbunitcli.application;

import yo.dbunitcli.Strings;
import yo.dbunitcli.common.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public record CommandParameters(CommandType type, String[] args) {

    public CommandParameters(CommandType type, final Map<String, String> input) {
        this(type, input.entrySet()
                        .stream()
                        .filter(entry -> Strings.isNotEmpty(entry.getValue()))
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .toArray(String[]::new));
    }

    public CommandParameters(CommandType type, final Path target) throws IOException {
        this(type, Files.readAllLines(target).toArray(new String[0]));
    }

    public CommandParameters(final CommandType type) {
        this(type, type.getCommand().parseOption(new String[]{}).toArgs(false));
    }

    public String content() {
        return String.join("\r\n", this.args());
    }

    public Option.Parameters toOptionParameters() {
        return this.type().getCommand().parseOption(this.args).toParameters();
    }

    public CommandParameters shrink() {
        return new CommandParameters(this.type, this.toOptionParameters()
                                                    .toArgs(false));
    }

    public void exec(final String name) {
        this.type.getCommand().exec(name, this.args(), Parameter.none());
    }

    public String resultDir(final String name) {
        return this.type.getCommand().resultDir(name, this.args(), Parameter.none());
    }
}
