package yo.dbunitcli.compare;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;

import java.util.List;

public class DiffTable extends DefaultTable {

    public static DiffTable from(ITableMetaData metaData, int columnLength) throws DataSetException {
        final Column addedColumn = new Column("$MODIFY", DataType.UNKNOWN);
        Column[] columns = Lists.asList(addedColumn, metaData.getColumns()).toArray(new Column[columnLength + 1]);
        Column[] primaryKeys = metaData.getPrimaryKeys();
        if (primaryKeys.length > 0) {
            primaryKeys = Lists.newArrayList(primaryKeys, addedColumn).toArray(new Column[primaryKeys.length + 1]);
        }
        DefaultTableMetaData newMetaData = new DefaultTableMetaData(metaData.getTableName() + "$MODIFY", columns, primaryKeys);
        return new DiffTable(newMetaData);
    }

    private DiffTable(ITableMetaData metaData) {
        super(metaData);
    }

    public void addRow(Object[] oldRow, Object[] newRow) throws DataSetException {
        this.addRow(Lists.asList("OLD", oldRow).toArray(new Object[oldRow.length + 1]));
        this.addRow(Lists.asList("NEW", newRow).toArray(new Object[oldRow.length + 1]));
    }

}
