package yo.dbunitcli.compare;

import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;

import java.util.List;


public class CompareResult {
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

    private List<CompareDiff> diffs = Lists.newArrayList();

    private final String oldDir;

    private final String newDir;

    public CompareResult(String aOldDir, String aNewDir, List<CompareDiff> results) {
        this.oldDir = aOldDir;
        this.newDir = aNewDir;
        this.diffs.addAll(results);
    }

    public ITable toITable(String tableName) throws DataSetException {
        DefaultTable result = new DefaultTable(tableName, COLUMNS);
        for (CompareDiff diff : diffs) {
            result.addRow(new Object[]{oldDir
                    , newDir
                    , diff.getTargetName()
                    , diff.getDiff()
                    , diff.getOldDef()
                    , diff.getNewDef()
                    , diff.getColumnIndex()
                    , diff.getRows()
            });
        }
        return result;
    }

    public boolean existDiff() {
        return diffs.size() > 0;
    }

}
