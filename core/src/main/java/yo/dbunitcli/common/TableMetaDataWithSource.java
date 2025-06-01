package yo.dbunitcli.common;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.io.File;
import java.util.List;

public class TableMetaDataWithSource extends DefaultTableMetaData {

    public static Column[] OPTION_COLUMNS = new Column[]{
            new Column("$FILE_PATH", DataType.NVARCHAR)
            , new Column("$FILE_NAME", DataType.NVARCHAR)
            , new Column("$SHEET_NAME", DataType.NVARCHAR)
    };

    private final Source source;

    public static Source fileInfo(final File sourceFile, final boolean addFileInfo) {
        return new Source(sourceFile, addFileInfo);
    }

    public TableMetaDataWithSource(final ITableMetaData tableMetaData, final Source source) throws DataSetException {
        super(tableMetaData.getTableName(), source.getColumns(tableMetaData), tableMetaData.getPrimaryKeys());
        this.source = source;
    }

    public Source source() {
        return this.source;
    }

    public String[] toArray(final List<String> rowValues) {
        return this.source.toArray(rowValues);
    }

}
