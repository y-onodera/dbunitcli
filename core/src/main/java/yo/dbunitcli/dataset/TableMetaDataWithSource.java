package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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

    public static TableMetaDataWithSource fromJoin(final String tableName, final Column[] columns, final Column[] primaryKeys) {
        return new TableMetaDataWithSource(tableName, columns, primaryKeys, Source.JOIN);
    }

    public TableMetaDataWithSource(final ITableMetaData tableMetaData, final Source source) throws DataSetException {
        this(tableMetaData.getTableName(), source.getColumns(tableMetaData), tableMetaData.getPrimaryKeys(), source);
    }

    public TableMetaDataWithSource(final String tableName, final Column[] columns, final Column[] primaryKeys, final Source source) {
        super(tableName, columns, primaryKeys);
        this.source = source;
    }

    public Source source() {
        return this.source;
    }

    public String[] defaultColumnValues() {
        return this.source.defaultColumnValues(this.getColumnLength());
    }

    public String[] withDefaultValuesToArray(final List<String> rowValues) {
        final List<String> results = new ArrayList<>(rowValues);
        if (results.size() < this.getColumnLength()) {
            final String[] defaultValues = this.defaultColumnValues();
            IntStream.range(results.size(), this.getColumnLength())
                    .forEach(i -> results.add(defaultValues[i]));
        }
        return results.toArray(new String[0]);
    }

    public int getColumnLength() {
        return this.getColumns().length;
    }

}
