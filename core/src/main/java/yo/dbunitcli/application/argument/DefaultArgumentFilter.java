package yo.dbunitcli.application.argument;

import picocli.CommandLine;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultArgumentFilter implements ArgumentFilter {

    @Override
    public String[] filterArguments(final String prefix, final CommandLine commandLine, final String[] expandArgs) {
        final Map<String, String> options = Arrays.stream(expandArgs)
                .filter(this.filterOptionNameMatch(commandLine))
                .collect(Collectors.toMap(this.argsToMapEntry(), it -> it));
        this.overrideByPrefixUsedOption(prefix, commandLine, expandArgs, options);
        return options.values().toArray(new String[0]);
    }

    protected Function<String, String> argsToMapEntry() {
        return it -> it.replaceAll("(-[^=]+=).+", "$1");
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
                    .collect(Collectors.toMap(this.argsToMapEntry(), it -> it));
            nonPrefixOption.putAll(prefixUsedOption);
        }
    }
}
