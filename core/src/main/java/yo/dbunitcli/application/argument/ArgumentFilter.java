package yo.dbunitcli.application.argument;

import picocli.CommandLine;

public interface ArgumentFilter {

    String[] filterArguments(String prefix, CommandLine commandLine, String[] expandArgs);

}
