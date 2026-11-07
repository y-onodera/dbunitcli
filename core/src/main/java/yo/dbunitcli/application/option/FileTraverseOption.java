package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;

import java.util.List;
import java.util.Optional;

/**
 * @param srcType -extension未指定時に絞り込む拡張子（{@link DataSourceType#getExtension()}）を決める
 */
public record FileTraverseOption(TraverseOption traverseOption
        , String extension
        , DataSourceType srcType) implements ComparableDataSetParamOption {

    public FileTraverseOption(final String prefix, final DataSourceType srcType, final DataSetLoadDto dto) {
        this(new TraverseOption(prefix, dto), dto.getExtension(), srcType);
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return this.traverseOption().toParametersBuilder()
                .putWithDefault("-extension", this.extension(), Optional.ofNullable(this.srcType.getExtension())
                        .orElse("(all files)"));
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
