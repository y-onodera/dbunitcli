package yo.dbunitcli.application.option;

import yo.dbunitcli.dataset.ComparableDataSetParam;

public interface ComparableDataSetParamOption extends Option {

    static ComparableDataSetParamOption join(final ComparableDataSetParamOption... leaf) {
        return new CompositeOption(leaf);
    }

    ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder);

    class CompositeOption implements ComparableDataSetParamOption {
        private final ComparableDataSetParamOption[] leaf;

        public CompositeOption(final ComparableDataSetParamOption... leaf) {
            this.leaf = leaf;
        }

        @Override
        public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
            for (final ComparableDataSetParamOption delegate : this.leaf) {
                builder = delegate.populate(builder);
            }
            return builder;
        }

        @Override
        public CommandLineArgs toCommandLineArgs() {
            CommandLineArgs result = null;
            for (final ComparableDataSetParamOption delegate : this.leaf) {
                if (result == null) {
                    result = delegate.toCommandLineArgs();
                } else if (delegate instanceof TemplateRenderOption) {
                    result.addComponent("templateRender", delegate.toCommandLineArgs());
                } else {
                    result.putAll(delegate.toCommandLineArgs());
                }
            }
            return result;
        }

    }
}
