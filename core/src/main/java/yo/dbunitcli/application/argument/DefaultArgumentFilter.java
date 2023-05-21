package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.NamedOptionDef;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultArgumentFilter implements ArgumentFilter {

    @Override
    public Map<String, String> filterArguments(final String prefix, final CmdLineParser parser, final String[] expandArgs) {
        final Map<String, String> defaultArgs = Arrays.stream(expandArgs)
                .filter(this.parserTarget(parser))
                .collect(Collectors.toMap(this.argsToMapEntry(), it -> it));
        if (!Optional.ofNullable(prefix).orElse("").isEmpty()) {
            final String myArgs = "-" + prefix + ".";
            final Map<String, String> overrideArgs = Arrays.stream(expandArgs)
                    .filter(it -> it.startsWith(myArgs))
                    .map(it -> it.replace(myArgs, "-"))
                    .filter(this.parserTarget(parser))
                    .collect(Collectors.toMap(this.argsToMapEntry(), it -> it));
            defaultArgs.putAll(overrideArgs);
        }
        return defaultArgs;
    }

    protected Function<String, String> argsToMapEntry() {
        return it -> it.replaceAll("(-[^=]+=).+", "$1");
    }

    protected Predicate<String> parserTarget(final CmdLineParser parser) {
        return it -> parser.getOptions()
                .stream()
                .anyMatch(handler -> it.startsWith(((NamedOptionDef) handler.option).name() + "="));
    }

}
