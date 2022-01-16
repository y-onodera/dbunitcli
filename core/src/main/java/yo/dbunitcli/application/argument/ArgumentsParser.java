package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public interface ArgumentsParser {
    /**
     * @param args
     * @return args exclude this option is parsed
     * @throws CmdLineException
     */
    CmdLineParser parseArgument(String[] args) throws CmdLineException;

}
