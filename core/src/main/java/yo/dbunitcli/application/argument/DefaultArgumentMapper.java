package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineParser;

import java.util.Collection;
import java.util.Map;

public class DefaultArgumentMapper implements ArgumentMapper {

    @Override
    public Collection<String> mapFilterArgument(final Map<String, String> filterArguments, final String prefix, final CmdLineParser parser, final String[] expandArgs) {
        return filterArguments.values();
    }

    @Override
    public String[] map(final String[] args, final String prefix, final CmdLineParser parser) {
        return args;
    }
}
