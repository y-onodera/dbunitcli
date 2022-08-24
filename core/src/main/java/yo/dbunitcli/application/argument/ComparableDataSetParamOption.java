package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public interface ComparableDataSetParamOption extends ArgumentsParser {

    ComparableDataSetParamOption NONE = new None();

    static ComparableDataSetParamOption join(ComparableDataSetParamOption... leaf) {
        return new CompositeOption(leaf);
    }

    ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder);

    @Override
    default void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        // nothing
    }

    ;

    @Override
    default OptionParam createOptionParam(Map<String, String> args) {
        return new OptionParam(this.getPrefix(), args);
    }

    class None implements ComparableDataSetParamOption {

        @Override
        public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
            return builder;
        }

    }

    class CompositeOption implements ComparableDataSetParamOption {
        private final ComparableDataSetParamOption[] leaf;

        public CompositeOption(ComparableDataSetParamOption... leaf) {
            this.leaf = leaf;
        }

        @Override
        public CmdLineParser parseArgument(String[] args) throws CmdLineException {
            CmdLineParser result = null;
            for (ComparableDataSetParamOption delegate : this.leaf) {
                result = delegate.parseArgument(args);
            }
            return result;
        }

        @Override
        public OptionParam createOptionParam(Map<String, String> args) {
            OptionParam result = null;
            for (ComparableDataSetParamOption delegate : this.leaf) {
                if (result == null) {
                    result = delegate.createOptionParam(args);
                } else {
                    result.putAll(delegate.createOptionParam(args));
                }
            }
            return result;
        }

        @Override
        public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
            for (ComparableDataSetParamOption delegate : this.leaf) {
                builder = delegate.populate(builder);
            }
            return builder;
        }
    }
}
