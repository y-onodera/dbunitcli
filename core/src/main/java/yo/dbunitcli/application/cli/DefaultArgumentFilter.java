package yo.dbunitcli.application.cli;

import picocli.CommandLine;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultArgumentFilter implements ArgumentFilter {

    private final List<String> mapKeyNames;

    public DefaultArgumentFilter(final String... keyNames) {
        if (keyNames != null) {
            this.mapKeyNames = Arrays.asList(keyNames);
        } else {
            this.mapKeyNames = new ArrayList<>();
        }
    }

    @Override
    public String[] filterArguments(final String prefix, final CommandLine commandLine, final String[] expandArgs) {
        final List<String> mapArgs = new ArrayList<>(Arrays.stream(expandArgs)
                .filter(it -> this.mapKeyNames.stream().anyMatch(it::startsWith))
                .toList());
        final Map<String, String> options = Arrays.stream(expandArgs)
                .filter(it -> this.mapKeyNames.stream().noneMatch(it::startsWith))
                .filter(this.filterOptionNameMatch(commandLine))
                .collect(Collectors.toMap(this.extractKey(), it -> it));
        this.overrideByPrefixUsedOption(prefix, commandLine, expandArgs, options);
        mapArgs.addAll(options.values());
        return mapArgs.toArray(new String[0]);
    }

    protected Predicate<String> filterOptionNameMatch(final CommandLine commandLine) {
        return it -> commandLine.getCommandSpec().options().stream()
                .flatMap(optionSpec -> Arrays.stream(optionSpec.names())
                        .map(name -> optionSpec.type().isAssignableFrom(Map.class) ? name : name + "="))
                .anyMatch(it::startsWith);
    }

    protected void overrideByPrefixUsedOption(final String prefix, final CommandLine commandLine, final String[] expandArgs, final Map<String, String> nonPrefixOption) {
        if (!Optional.ofNullable(prefix).orElse("").isEmpty()) {
            final String myArgs = "-" + prefix + ".";
            final Map<String, String> prefixUsedOption = Arrays.stream(expandArgs)
                    .filter(it -> it.startsWith(myArgs))
                    .map(it -> it.replace(myArgs, "-"))
                    .filter(this.filterOptionNameMatch(commandLine))
                    .collect(Collectors.toMap(this.extractKey(), it -> it));
            nonPrefixOption.putAll(prefixUsedOption);
        }
    }
}
