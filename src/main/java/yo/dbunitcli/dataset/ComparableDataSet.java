package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.stream.IDataSetConsumer;

public interface ComparableDataSet extends IDataSet, IDataSetConsumer {

    String getSrc();

    @Override
    ComparableTable getTable(String tableName) throws DataSetException;

    boolean contains(String tableName);

}
