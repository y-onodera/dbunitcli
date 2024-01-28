package yo.dbunitcli.application.argument;

import picocli.CommandLine;

public interface ArgumentMapper {

    String[] map(String[] args, String prefix, CommandLine cmdLine);
}
