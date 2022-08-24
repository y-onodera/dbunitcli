package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineParser;

import java.util.Map;

public interface ArgumentFilter {

    Map<String, String> filterArguments(String prefix, CmdLineParser parser, String[] expandArgs);

}
