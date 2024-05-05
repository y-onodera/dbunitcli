package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public interface ComparableDataSetParamOption extends OptionParser<DataSetLoadDto> {

    static ComparableDataSetParamOption join(final ComparableDataSetParamOption... leaf) {
        return new CompositeOption(leaf);
    }

    ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder);

    @Override
    default OptionParam createOptionParam(final Map<String, String> args) {
        return new OptionParam(this.getPrefix(), args);
    }

    @Override
    void setUpComponent(DataSetLoadDto dto);

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
        public void setUpComponent(final DataSetLoadDto dto) {
            for (final ComparableDataSetParamOption delegate : this.leaf) {
                delegate.setUpComponent(dto);
            }
        }
    }
}
