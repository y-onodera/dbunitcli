package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

import java.util.List;

public interface CompareResult {

    String RESULT_TABLE_NAME = "COMPARE_RESULT";

    default boolean existDiff() {
        return this.getDiffs().size() > 0;
    }

    List<CompareDiff> getDiffs();

    ITable toITable() throws DataSetException;

}
