package yo.dbunitcli.dataset.producer;

import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.nio.file.Path;
import java.util.function.Predicate;

public class ComparableDirectoryDataSetProducer extends ComparableFileDataSetProducer {

    public ComparableDirectoryDataSetProducer(ComparableDataSetParam param) {
        super(param);
    }

    @Override
    protected Predicate<Path> fileTypeFilter() {
        return path -> path.toFile().isDirectory();
    }
}
