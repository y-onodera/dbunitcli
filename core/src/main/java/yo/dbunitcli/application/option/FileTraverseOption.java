package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;

import java.util.List;

/**
 * @param srcType -extension未指定時に絞り込む拡張子（{@link DataSourceType#getExtension()}）を決める
 */
public record FileTraverseOption(TraverseOption traverseOption
        , String extension
        , DataSourceType srcType) implements ComparableDataSetParamOption {

    /** -extension未指定で拡張子を絞り込まない場合の説明（GUIのプレースホルダー） */
    private static final String ALL_FILES_DESCRIPTION = "(all files)";

    public FileTraverseOption(final String prefix, final DataSourceType srcType, final DataSetLoadDto dto) {
        this(new TraverseOption(prefix, dto), dto.getExtension(), srcType);
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return this.traverseOption().toParametersBuilder()
                .putWithDefault("-extension", this.extension(), this.srcType.getExtension() != null
                        ? this.srcType.getExtension() : ALL_FILES_DESCRIPTION);
    }

    @Override
    public List<Violation> validate() {
        return this.traverseOption().validate();
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return this.traverseOption().populate(builder).setExtension(Strings.orDefault(this.extension(), null));
    }


}
