package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineParser;

import java.util.Collection;
import java.util.Map;

public class DefaultArgumentMapper implements ArgumentMapper {

    @Override
    public Collection<String> mapFilterArgument(Map<String, String> filterArguments, String prefix, CmdLineParser parser, String[] expandArgs) {
        return filterArguments.values();
    }

    @Override
    public String[] map(String[] args, String prefix, CmdLineParser parser) {
        return args;
    }
}
