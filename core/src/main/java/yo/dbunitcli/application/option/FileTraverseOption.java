package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.List;

public record FileTraverseOption(TraverseOption traverseOption
        , String extension) implements ComparableDataSetParamOption {

    public FileTraverseOption(final String prefix, final DataSetLoadDto dto) {
        this(new TraverseOption(prefix, dto), dto.getExtension());
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return this.traverseOption().toParametersBuilder()
                .put("-extension", this.extension());
    }

    @Override
    public List<Violation> validate() {
        return this.traverseOption().validate();
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return this.traverseOption().populate(builder).setExtension(this.extension());
    }


}
