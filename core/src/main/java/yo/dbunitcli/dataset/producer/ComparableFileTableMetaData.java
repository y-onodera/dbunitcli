package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;

public class ComparableFileTableMetaData extends DefaultTableMetaData {
    public static Column PK = new Column("PATH", DataType.NVARCHAR);
    public static Column[] COLUMNS = new Column[]{PK
            , new Column("NAME", DataType.NVARCHAR)
            , new Column("DIR", DataType.NVARCHAR)
            , new Column("RELATIVE_PATH", DataType.NVARCHAR)
            , new Column("SIZE_KB", DataType.NUMERIC)
            , new Column("LAST_MODIFIED", DataType.NVARCHAR)
    };

    public ComparableFileTableMetaData(final String tableName) {
        super(tableName, COLUMNS, new Column[]{PK});
    }
}
