package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineParser;

import java.util.Collection;
import java.util.Map;

public interface ArgumentMapper {

    Collection<String> mapFilterArgument(Map<String, String> filterArguments, String prefix, CmdLineParser parser, String[] expandArgs);

    String[] map(String[] args, String prefix, CmdLineParser parser);
}
