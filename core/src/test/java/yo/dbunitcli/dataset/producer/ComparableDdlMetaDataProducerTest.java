package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.ComparableTableMappingTask;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComparableDdlMetaDataProducerTest {

    @Test
    void reflectsRealMetadataFromDelegate() {
        final ComparableDataSetParam param = ComparableDataSetParam.builder()
                .setSrc(new File("."))
                .build();
        final Column idColumn = new Column("EMPLOYEE_ID", DataType.INTEGER, "INTEGER", Column.NO_NULLS, null, "社員ID", null);
        final Column nameColumn = new Column("EMPLOYEE_NAME", DataType.VARCHAR, "VARCHAR", Column.NULLABLE, null, null, null);
        final ComparableDataSetProducer delegate = new FixedMetaDataProducer(param, "EMPLOYEE",
                new Column[]{idColumn, nameColumn}, new Column[]{idColumn});

        final ComparableDdlMetaDataProducer producer = new ComparableDdlMetaDataProducer(delegate);
        final ComparableDataSet dataSet = producer.loadDataSet();
        final ComparableTable table = dataSet.getTable("EMPLOYEE");
        assertEquals(2, table.getRowCount());

        final var idRow = table.getRowToMap(0);
        assertEquals("EMPLOYEE_ID", idRow.get("COLUMN_NAME"));
        assertEquals("INTEGER", idRow.get("TYPE_NAME"));
        assertEquals("false", idRow.get("NULLABLE"));
        assertEquals("true", idRow.get("IS_PK"));
        assertEquals("社員ID", idRow.get("REMARKS"));
        assertEquals("EMPLOYEE", idRow.get("TABLE_NAME"));

        final var nameRow = table.getRowToMap(1);
        assertEquals("EMPLOYEE_NAME", nameRow.get("COLUMN_NAME"));
        assertEquals("VARCHAR", nameRow.get("TYPE_NAME"));
        assertEquals("true", nameRow.get("NULLABLE"));
        assertEquals("false", nameRow.get("IS_PK"));
        assertEquals("", nameRow.get("REMARKS"));
    }

    @Test
    void unknownMetadataMapsToEmptyString() {
        final ComparableDataSetParam param = ComparableDataSetParam.builder()
                .setSrc(new File("."))
                .build();
        final Column plainColumn = new Column("id", DataType.UNKNOWN);
        final ComparableDataSetProducer delegate = new FixedMetaDataProducer(param, "SAMPLE",
                new Column[]{plainColumn}, new Column[0]);

        final ComparableDdlMetaDataProducer producer = new ComparableDdlMetaDataProducer(delegate);
        final ComparableTable table = producer.loadDataSet().getTable("SAMPLE");
        assertEquals(1, table.getRowCount());

        // CSV等メタデータの無いソースでは、従来のScaffoldダミー行と同じ「列名とテーブル名以外すべて空」になる
        final var row = table.getRowToMap(0);
        assertEquals("id", row.get("COLUMN_NAME"));
        assertEquals("SAMPLE", row.get("TABLE_NAME"));
        for (final String emptyColumn : new String[]{"TYPE_NAME", "COLUMN_SIZE", "DECIMAL_DIGITS", "NULLABLE",
                "IS_PK", "PK_NAME", "REMARKS", "TABLE_REMARKS", "PACKAGE"}) {
            assertEquals("", row.get(emptyColumn), emptyColumn);
        }
    }

    private record FixedMetaDataProducer(ComparableDataSetParam param, String tableName,
                                         Column[] columns, Column[] primaryKeys) implements ComparableDataSetProducer {

        @Override
        public Stream<Source> getSourceStream() {
            return Stream.of(Source.NONE.tableName(this.tableName));
        }

        @Override
        public ComparableTableMappingTask createTableMappingTask(final Source source) {
            return new FixedMetaDataTask(source, this.param, this.tableName, this.columns, this.primaryKeys);
        }
    }

    private record FixedMetaDataTask(Source source, ComparableDataSetParam param, String tableName,
                                     Column[] columns, Column[] primaryKeys) implements ComparableTableMappingTask {

        @Override
        public void run(final yo.dbunitcli.dataset.ComparableTableMappingContext context) {
            final var metaData = new DefaultTableMetaData(this.tableName, this.columns, this.primaryKeys);
            final var mapper = context.createMapper(this.source.wrap(metaData));
            mapper.startTable();
            mapper.endTable();
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new FixedMetaDataTask(this.source, builder.build(), this.tableName, this.columns, this.primaryKeys);
        }
    }
}
