package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public class FileTraverseOption extends TraverseOption {

    private final String extension;

    public FileTraverseOption(final String prefix, final DataSetLoadDto dto) {
        super(prefix, dto);
        this.extension = dto.getExtension();
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = super.toCommandLineArgs();
        result.put("-extension", this.extension);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return super.populate(builder).setExtension(this.extension);
    }


}
