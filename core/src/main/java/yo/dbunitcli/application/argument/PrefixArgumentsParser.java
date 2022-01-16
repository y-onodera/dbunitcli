package yo.dbunitcli.application.argument;

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.NamedOptionDef;
import org.kohsuke.args4j.spi.OptionHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class PrefixArgumentsParser implements ArgumentsParser {

    private final String prefix;

    public PrefixArgumentsParser(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @param args
     * @return args exclude this option is parsed
     * @throws CmdLineException
     */
    @Override
    public CmdLineParser parseArgument(String[] args) throws CmdLineException {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(this.filterArguments(parser, args));
        } catch (CmdLineException cx) {
            System.out.println("usage:");
            parser.printSingleLineUsage(System.out);
            System.out.println();
            parser.printUsage(System.out);
            throw cx;
        }
        setUpComponent(parser, args);
        return parser;
    }

    protected Collection<String> filterArguments(CmdLineParser parser, String[] expandArgs) {
        Map<String, String> defaultArgs = Arrays.stream(expandArgs)
                .filter(parserTarget(parser))
                .filter(parserTarget(parser))
                .collect(Collectors.toMap(this.argsToMapEntry(), it -> it));
        if (!Strings.isNullOrEmpty(this.getPrefix())) {
            String myArgs = "-" + getPrefix() + ".";
            Map<String, String> overrideArgs = Arrays.stream(expandArgs)
                    .filter(it -> it.startsWith(myArgs))
                    .map(it -> it.replace(myArgs, "-"))
                    .filter(parserTarget(parser))
                    .collect(Collectors.toMap(this.argsToMapEntry(), it -> it));
            defaultArgs.putAll(overrideArgs);
        }
        return defaultArgs.values();
    }

    protected Function<String, String> argsToMapEntry() {
        return it -> it.replaceAll("(-[^=]+=).+", "$1");
    }

    protected Predicate<String> parserTarget(CmdLineParser parser) {
        return it -> {
            for (OptionHandler handler : parser.getOptions()) {
                if (it.startsWith(((NamedOptionDef) handler.option).name() + "=")) {
                    return true;
                }
            }
            return false;
        };
    }

    protected void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
    }

    protected String getPrefix() {
        return this.prefix;
    }

}
