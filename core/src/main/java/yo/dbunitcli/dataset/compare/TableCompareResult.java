package yo.dbunitcli.dataset.compare;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;

import java.util.List;


public class TableCompareResult implements CompareResult {

    static private final Column[] COLUMNS = new Column[]{
            new Column("OLD_PATH", DataType.UNKNOWN)
            , new Column("NEW_PATH", DataType.UNKNOWN)
            , new Column("TARGET_NAME", DataType.UNKNOWN)
            , new Column("DIFF", DataType.UNKNOWN)
            , new Column("OLD_VALUE", DataType.UNKNOWN)
            , new Column("NEW_VALUE", DataType.UNKNOWN)
            , new Column("COLUMN_INDEX", DataType.NUMERIC)
            , new Column("DIFF_ROWS", DataType.NUMERIC)
    };

    static private final Column[] SORT_KEYS = new Column[]{
            new Column("TARGET_NAME", DataType.UNKNOWN)
            , new Column("DIFF", DataType.UNKNOWN)
            , new Column("COLUMN_INDEX", DataType.NUMERIC)
    };

    private final List<CompareDiff> diffs;

    private final String oldDir;

    private final String newDir;

    public TableCompareResult(String aOldDir, String aNewDir, List<CompareDiff> results) {
        this.oldDir = aOldDir;
        this.newDir = aNewDir;
        this.diffs = Lists.newArrayList(results);
    }

    public ITable toITable() throws DataSetException {
        DefaultTable result = new DefaultTable(RESULT_TABLE_NAME, COLUMNS);
        for (CompareDiff diff : getDiffs()) {
            result.addRow(new Object[]{oldDir
                    , newDir
                    , diff.getTargetName()
                    , diff.getDiff()
                    , diff.getOldDefine()
                    , diff.getNewDefine()
                    , diff.getColumnIndex()
                    , diff.getRows()
            });
        }
        return new SortedTable(result, SORT_KEYS);
    }

    public List<CompareDiff> getDiffs() {
        return this.diffs;
    }


}
