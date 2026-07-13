package yo.dbunitcli.dataset.producer;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ComparableXlsxSchemaMetaDataProducer extends ComparableDataSetProducerWrapper {

    private static final int DATA_START_ROW = 1;

    private static final Column[] COLUMN_DEF_SCHEMA = {
            new Column("COLUMN_NAME", DataType.VARCHAR),
            new Column("SHEET_NAME", DataType.VARCHAR),
            new Column("DATA_START", DataType.VARCHAR),
            new Column("COLUMN_INDEX", DataType.VARCHAR),
            new Column("CELL_ADDRESS", DataType.VARCHAR),
            new Column("IS_PK", DataType.BOOLEAN)
    };

    public ComparableXlsxSchemaMetaDataProducer(final ComparableDataSetProducer delegate) {
        super(delegate);
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        final String tableName = metaData.getTableName();
        final Set<String> primaryKeyNames = Arrays.stream(metaData.getPrimaryKeys())
                .map(Column::getColumnName)
                .collect(Collectors.toSet());
        this.writeColumnRows(tableName, COLUMN_DEF_SCHEMA, metaData.getColumns(), (column, index) -> {
            final String columnName = column.getColumnName();
            return new Object[]{
                    columnName,
                    tableName,
                    String.valueOf(DATA_START_ROW),
                    String.valueOf(index),
                    new CellReference(DATA_START_ROW, index).formatAsString(),
                    primaryKeyNames.contains(columnName)
            };
        });
    }
}
