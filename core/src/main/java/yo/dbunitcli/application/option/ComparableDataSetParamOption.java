package yo.dbunitcli.application.option;

import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Arrays;

public interface ComparableDataSetParamOption extends Option {

    static ComparableDataSetParamOption join(final ComparableDataSetParamOption... leaf) {
        return new CompositeOption(leaf);
    }

    ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder);

    record CompositeOption(ComparableDataSetParamOption[] leaf) implements ComparableDataSetParamOption {

        @Override
        public ComparableDataSetParamOption[] leaf() {
            return Arrays.copyOf(this.leaf, this.leaf.length);
        }

        @Override
        public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
            for (final ComparableDataSetParamOption delegate : this.leaf) {
                builder = delegate.populate(builder);
            }
            return builder;
        }

        @Override
        public CommandLineArgsBuilder toCommandLineArgsBuilder() {
            CommandLineArgsBuilder result = null;
            for (final ComparableDataSetParamOption delegate : this.leaf) {
                if (result == null) {
                    result = delegate.toCommandLineArgsBuilder();
                } else {
                    result.putAll(delegate.toCommandLineArgs());
                }
            }
            return result;
        }

    }
}
