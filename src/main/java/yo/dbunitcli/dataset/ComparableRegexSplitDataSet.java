package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.compare.ColumnSetting;

import java.io.File;

public class ComparableRegexSplitDataSet extends AbstractComparableDataSet {
    private final File src;

    public ComparableRegexSplitDataSet(String headerRegex, String regex, File aSrc, String aEncoding, ColumnSetting excludeColumns) throws DataSetException {
        super(new ComparableRegexSplitDataSetProducer(headerRegex, regex, aSrc, aEncoding), excludeColumns);
        this.src = aSrc;
    }

    @Override
    public String getSrc() {
        return src.toString();
    }
}