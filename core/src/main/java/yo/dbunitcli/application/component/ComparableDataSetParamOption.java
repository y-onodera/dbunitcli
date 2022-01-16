package yo.dbunitcli.application.component;

import org.kohsuke.args4j.CmdLineParser;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public interface ComparableDataSetParamOption extends ArgumentsParser {

    ComparableDataSetParamOption NONE = new None();

    ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder);

    class None implements ComparableDataSetParamOption {
        @Override
        public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
            return builder;
        }

        @Override
        public CmdLineParser parseArgument(String[] args) {
            return new CmdLineParser(this);
        }
    }
}
