package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.Strings;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ArgumentMapper(String prefix, ArgumentFunction function, ArgumentFilter filter) {

    private static final Pattern ENV_VARIABLE = Pattern.compile("\\$\\{env\\.([^}]+)}");

    public ArgumentMapper(final String prefix) {
        this(prefix, new DefaultArgumentFunction(), new DefaultArgumentFilter());
    }

    public ArgumentMapper(final String prefix, final ArgumentFunction argumentMapper) {
        this(prefix, argumentMapper, new DefaultArgumentFilter());
    }

    public void populate(final String[] args, final CompositeDto compositeDto) {
        compositeDto.dto().forEach(dto -> this.populate(args, dto));
    }

    public void populate(final String[] args, final Object dto) {
        final CommandLine cmdLine = new CommandLine(dto);
        final String[] targetArgs = this.replaceEnv(this.function().apply(args, this.prefix()));
        try {
            cmdLine.parseArgs(this.filter().filterArguments(this.prefix(), cmdLine, targetArgs));
        } catch (final CommandLine.ParameterException ex) {
            throw new CommandLine.ParameterException(cmdLine, this.withPrefix(ex.getMessage(), cmdLine, targetArgs), ex);
        }
    }

    /**
     * prefix付きで指定されたオプションは、エラー表示のオプション名もprefix付きにする（picocliはprefixを除いた名前で表示するため）
     */
    private String withPrefix(final String message, final CommandLine cmdLine, final String[] args) {
        if (Strings.isEmpty(this.prefix())) {
            return message;
        }
        String result = message;
        for (final CommandLine.Model.OptionSpec option : cmdLine.getCommandSpec().options()) {
            for (final String name : option.names()) {
                final String prefixed = "-" + this.prefix() + "." + name.substring(1);
                if (Arrays.stream(args).anyMatch(it -> it.startsWith(prefixed + "="))) {
                    result = result.replace("'" + name + "'", "'" + prefixed + "'")
                                   .replace("'" + name + "=", "'" + prefixed + "=");
                }
            }
        }
        return result;
    }

    private String[] replaceEnv(final String[] args) {
        if (Arrays.stream(args).noneMatch(it -> ENV_VARIABLE.matcher(it).find())) {
            return args;
        }
        final Properties env = ArgumentMapper.loadEnv();
        return Arrays.stream(args)
                .map(it -> ArgumentMapper.replaceEnv(it, env))
                .toArray(String[]::new);
    }

    private static String replaceEnv(final String arg, final Properties env) {
        final Matcher matcher = ENV_VARIABLE.matcher(arg);
        final StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            final String value = env.getProperty(matcher.group(1));
            if (value == null) {
                throw new AssertionError("env variable not defined :" + matcher.group(1)
                        + " in " + FileResources.envFile().getPath());
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static Properties loadEnv() {
        final Properties result = new Properties();
        final File envFile = FileResources.envFile();
        if (envFile.exists()) {
            try (final Reader reader = Files.newBufferedReader(envFile.toPath(), StandardCharsets.UTF_8)) {
                result.load(reader);
            } catch (final IOException ex) {
                throw new AssertionError("Failed to load " + envFile, ex);
            }
        }
        return result;
    }
}
