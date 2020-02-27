package yo.dbunitcli.dataset;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Predicate;

public class ComparableDirectoryDataSetProducer extends ComparableFileDataSetProducer {

    public ComparableDirectoryDataSetProducer(ComparableDataSetLoaderParam param) {
        super(param);
    }

    @Override
    protected Predicate<Path> fileTypeFilter() {
        return path -> path.toFile().isDirectory();
    }
}
