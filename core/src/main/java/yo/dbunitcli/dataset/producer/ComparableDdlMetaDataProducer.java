package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ComparableDdlMetaDataProducer extends ComparableDataSetProducerWrapper {

    private static final Column[] COLUMN_DEF_SCHEMA = {
            new Column("COLUMN_NAME", DataType.VARCHAR),
            new Column("TYPE_NAME", DataType.VARCHAR),
            new Column("COLUMN_SIZE", DataType.VARCHAR),
            new Column("DECIMAL_DIGITS", DataType.VARCHAR),
            new Column("NULLABLE", DataType.VARCHAR),
            new Column("IS_PK", DataType.VARCHAR),
            new Column("PK_NAME", DataType.VARCHAR),
            new Column("REMARKS", DataType.VARCHAR),
            new Column("TABLE_REMARKS", DataType.VARCHAR),
            new Column("TABLE_NAME", DataType.VARCHAR),
            new Column("PACKAGE", DataType.VARCHAR)
    };

    public ComparableDdlMetaDataProducer(final ComparableDataSetProducer delegate) {
        super(delegate);
    }

    public static Column[] outputSchema() {
        return COLUMN_DEF_SCHEMA.clone();
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        final String tableName = metaData.getTableName();
        final Set<String> primaryKeyNames = Arrays.stream(metaData.getPrimaryKeys())
                .map(Column::getColumnName)
                .collect(Collectors.toSet());
        this.writeColumnRows(tableName, COLUMN_DEF_SCHEMA, metaData.getColumns(), (column, index) -> new Object[]{
                column.getColumnName(),
                ComparableDdlMetaDataProducer.typeName(column),
                "",
                "",
                ComparableDdlMetaDataProducer.nullable(column),
                primaryKeyNames.isEmpty() ? "" : String.valueOf(primaryKeyNames.contains(column.getColumnName())),
                "",
                Optional.ofNullable(column.getRemarks()).orElse(""),
                "",
                tableName,
                ""
        });
    }

    // メタデータから確定できない値は空文字にする（COLUMN_SIZE/DECIMAL_DIGITS/PK_NAME/TABLE_REMARKS/PACKAGEは
    // dbunitのColumn/ITableMetaDataに存在しないため常に空）。生成された記述子は編集の出発点として使う
    private static String typeName(final Column column) {
        return column.getDataType() == DataType.UNKNOWN ? "" : column.getSqlTypeName();
    }

    private static String nullable(final Column column) {
        if (column.getNullable() == Column.NULLABLE_UNKNOWN || column.getNullable() == null) {
            return "";
        }
        return String.valueOf(column.getNullable() == Column.NULLABLE);
    }
}
