package yo.dbunitcli.application.component;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public interface ArgumentsParser {
    /**
     * @param args
     * @return args exclude this option is parsed
     * @throws CmdLineException
     */
    CmdLineParser parseArgument(String[] args) throws CmdLineException;

    class CompositeArgumentParser implements ArgumentsParser {
        private final ArgumentsParser[] leaf;

        public CompositeArgumentParser(ArgumentsParser[] leaf) {
            this.leaf = leaf;
        }

        @Override
        public CmdLineParser parseArgument(String[] args) throws CmdLineException {
            CmdLineParser result = null;
            for (ArgumentsParser delegate : this.leaf) {
                result = delegate.parseArgument(args);
            }
            return result;
        }
    }
}
