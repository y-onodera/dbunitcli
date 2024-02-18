package yo.dbunitcli.application.argument;

import picocli.CommandLine;

public class DefaultArgumentMapper implements ArgumentMapper {

    @Override
    public String[] map(final String[] args, final String prefix, final CommandLine cmdLine) {
        return args;
    }
}
