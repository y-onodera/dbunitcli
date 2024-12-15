package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public record FileTraverseOption(TraverseOption traverseOption
        , String extension) implements ComparableDataSetParamOption {

    public FileTraverseOption(final String prefix, final DataSetLoadDto dto) {
        this(new TraverseOption(prefix, dto), dto.getExtension());
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return this.traverseOption().toCommandLineArgsBuilder()
                .put("-extension", this.extension());
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return this.traverseOption().populate(builder).setExtension(this.extension());
    }


}
