package yo.dbunitcli.application;

import yo.dbunitcli.Strings;
import yo.dbunitcli.common.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

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

    public CommandParameters resolveFilePaths(final BiFunction<Option.BaseDir, String, String> resolver) {
        final Option.Parameters params = this.toOptionParameters();
        boolean changed = false;
        final List<String> newArgs = new ArrayList<>();
        for (final String key : params.keySet()) {
            final Option.Arg arg = params.getArg(key);
            if (arg == null || arg.value().isEmpty()) {
                continue;
            }
            final String value = arg.value();
            final String resolved = resolver.apply(arg.attribute().defaultPath(), value);
            if (!resolved.equals(value)) {
                changed = true;
            }
            newArgs.add(key + "=" + resolved);
        }
        if (!changed) {
            return this;
        }
        newArgs.addAll(this.inputParamArgs());
        return new CommandParameters(this.type, newArgs.toArray(new String[0]));
    }

    public CommandParameters shrink() {
        final List<String> newArgs = new ArrayList<>(Arrays.asList(this.toOptionParameters().toArgs(false)));
        newArgs.addAll(this.inputParamArgs());
        return new CommandParameters(this.type, newArgs.toArray(new String[0]));
    }

    public Map<String, String> inputParam() {
        final Map<String, String> result = new LinkedHashMap<>();
        this.inputParamArgs().forEach(it -> {
            final int separator = it.indexOf('=');
            if (separator > 2) {
                result.put(it.substring(2, separator), it.substring(separator + 1));
            }
        });
        return result;
    }

    private List<String> inputParamArgs() {
        return Arrays.stream(this.args).filter(it -> it.startsWith("-P")).toList();
    }

    public Map<String,Object> serialize() {
        return this.toOptionParameters().serialize();
    }

    public void exec(final String name) {
        this.type.getCommand().exec(name, this.args(), Parameter.none());
    }

    public String resultDir(final String name) {
        return this.type.getCommand().resultDir(name, this.args(), Parameter.none());
    }
}
