package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.common.Source;

public interface ComparableDataSetConsumer {
    void startDataSet() throws DataSetException;

    ComparableTableMappingContext createMappingContext(Source source);

    void endDataSet() throws DataSetException;

}
