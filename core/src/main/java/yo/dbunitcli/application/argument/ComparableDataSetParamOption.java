package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public interface ComparableDataSetParamOption extends ArgumentsParser {

    ComparableDataSetParamOption NONE = new None();

    static ComparableDataSetParamOption join(final ComparableDataSetParamOption... leaf) {
        return new CompositeOption(leaf);
    }

    ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder);

    @Override
    default void setUpComponent(final CmdLineParser parser, final String[] expandArgs) throws CmdLineException {
        // nothing
    }

    @Override
    default OptionParam createOptionParam(final Map<String, String> args) {
        return new OptionParam(this.getPrefix(), args);
    }

    class None implements ComparableDataSetParamOption {

        @Override
        public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
            return builder;
        }

    }

    class CompositeOption implements ComparableDataSetParamOption {
        private final ComparableDataSetParamOption[] leaf;

        public CompositeOption(final ComparableDataSetParamOption... leaf) {
            this.leaf = leaf;
        }

        @Override
        public CmdLineParser parseArgument(final String[] args) {
            CmdLineParser result = null;
            for (final ComparableDataSetParamOption delegate : this.leaf) {
                result = delegate.parseArgument(args);
            }
            return result;
        }

        @Override
        public OptionParam createOptionParam(final Map<String, String> args) {
            OptionParam result = null;
            for (final ComparableDataSetParamOption delegate : this.leaf) {
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
            for (final ComparableDataSetParamOption delegate : this.leaf) {
                builder = delegate.populate(builder);
            }
            return builder;
        }
    }
}
