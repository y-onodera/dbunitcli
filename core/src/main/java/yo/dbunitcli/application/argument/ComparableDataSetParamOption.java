package yo.dbunitcli.application.argument;

import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public interface ComparableDataSetParamOption extends ArgumentsParser {

    static ComparableDataSetParamOption join(final ComparableDataSetParamOption... leaf) {
        return new CompositeOption(leaf);
    }

    ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder);

    @Override
    default OptionParam createOptionParam(final Map<String, String> args) {
        return new OptionParam(this.getPrefix(), args);
    }

    @Override
    default void setUpComponent(final String[] expandArgs) {
        // nothing
    }

    class CompositeOption implements ComparableDataSetParamOption {
        private final ComparableDataSetParamOption[] leaf;

        public CompositeOption(final ComparableDataSetParamOption... leaf) {
            this.leaf = leaf;
        }

        @Override
        public void parseArgument(final String[] args) {
            for (final ComparableDataSetParamOption delegate : this.leaf) {
                delegate.parseArgument(args);
            }
        }

        @Override
        public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
            for (final ComparableDataSetParamOption delegate : this.leaf) {
                builder = delegate.populate(builder);
            }
            return builder;
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
    }
}
