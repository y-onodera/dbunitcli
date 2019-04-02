package yo.dbunitcli.dataset;

import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.filter.IColumnFilter;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ComparableXlsxDataSet extends AbstractComparableDataSet {
    private File src;

    public ComparableXlsxDataSet(File source) throws DataSetException {
        super(new ComparableXlsxDataSetProducer(source), Maps.newHashMap());
        this.src = source;
    }

    public ComparableXlsxDataSet(File aFile, Map<String,IColumnFilter> excludeColumns) throws DataSetException {
        super(new ComparableXlsxDataSetProducer(aFile), excludeColumns);
        this.src = aFile;
    }

    @Override
    public String getSrc() {
        return this.src.toString();
    }
}
