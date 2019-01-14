package example.y.onodera.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;

import java.util.List;


public class CompareResult {
    static private final Column[] COLUMNS = new Column[]{
            new Column("OLD_PATH", DataType.UNKNOWN)
            , new Column("NEW_PATH", DataType.UNKNOWN)
            , new Column("TARGET", DataType.UNKNOWN)
            , new Column("DIFF", DataType.UNKNOWN)
            , new Column("OLD_DEF", DataType.UNKNOWN)
            , new Column("NEW_DEF", DataType.UNKNOWN)
            , new Column("COLUMN_INDEX", DataType.UNKNOWN)
            , new Column("ROWS", DataType.UNKNOWN)
    };

    private List<CompareDiff> diffs = Lists.newArrayList();

    private final String oldDir;

    private final String newDir;

    public CompareResult(String aOldDir, String aNewDir, List<CompareDiff> results) {
        this.oldDir = aOldDir;
        this.newDir = aNewDir;
        this.diffs.addAll(results);
    }

    public  IDataSet toIDataSet() throws DataSetException {
        DefaultDataSet result = new DefaultDataSet();
        result.addTable(this.toITable());
        return result;
    }

    public ITable toITable() throws DataSetException {
        DefaultTable result = new DefaultTable("COMPARE_SCHEMA_RESULT", COLUMNS);
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
}
