package yo.dbunitcli.dataset;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Predicate;

public class ComparableDirectoryDataSetProducer extends ComparableFileDataSetProducer {

    public ComparableDirectoryDataSetProducer(File src, String targetName) {
        super(src, targetName);
    }

    @Override
    protected Predicate<Path> fileTypeFilter() {
        return path -> path.toFile().isDirectory();
    }
}
