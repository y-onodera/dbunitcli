package yo.dbunitcli.application.argument;

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.NamedOptionDef;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultArgumentFilter implements ArgumentFilter {

    @Override
    public Map<String, String> filterArguments(String prefix, CmdLineParser parser, String[] expandArgs) {
        Map<String, String> defaultArgs = Arrays.stream(expandArgs)
                .filter(this.parserTarget(parser))
                .collect(Collectors.toMap(this.argsToMapEntry(), it -> it));
        if (!Strings.isNullOrEmpty(prefix)) {
            String myArgs = "-" + prefix + ".";
            Map<String, String> overrideArgs = Arrays.stream(expandArgs)
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

    protected Predicate<String> parserTarget(CmdLineParser parser) {
        return it -> parser.getOptions()
                .stream()
                .anyMatch(handler -> it.startsWith(((NamedOptionDef) handler.option).name() + "="));
    }

}
