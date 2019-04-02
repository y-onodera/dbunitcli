package yo.dbunitcli.dataset;

import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.filter.IColumnFilter;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ComparableXlsDataSet extends AbstractComparableDataSet {

    private final File src;

    public ComparableXlsDataSet(File src) throws DataSetException {
        super(new ComparableXlsDataSetProducer(src), Maps.newHashMap());
        this.src = src;
    }

    public ComparableXlsDataSet(File aDir, Map<String, IColumnFilter> excludeColumns) throws DataSetException {
        super(new ComparableXlsDataSetProducer(aDir), excludeColumns);
        this.src = aDir;
    }

    @Override
    public String getSrc() {
        return this.src.toString();
    }
}
