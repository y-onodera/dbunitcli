package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.ComparableTableMappingContext;
import yo.dbunitcli.dataset.ComparableTableMappingTask;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComparableXlsxSchemaMetaDataProducerTest {

    @Test
    void reflectsRealPrimaryKeyMetadataFromDelegate() {
        final ComparableDataSetParam param = ComparableDataSetParam.builder()
                .setSrc(new File("."))
                .build();
        final Column idColumn = new Column("ID", org.dbunit.dataset.datatype.DataType.INTEGER);
        final Column nameColumn = new Column("NAME", org.dbunit.dataset.datatype.DataType.VARCHAR);
        final ComparableDataSetProducer delegate = new FixedMetaDataProducer(param, "PERSON",
                new Column[]{idColumn, nameColumn}, new Column[]{idColumn});

        final ComparableXlsxSchemaMetaDataProducer producer = new ComparableXlsxSchemaMetaDataProducer(delegate);
        final ComparableDataSet dataSet = producer.loadDataSet();
        final ComparableTable table = dataSet.getTable("PERSON");
        assertEquals(2, table.getRowCount());

        final var idRow = table.getRowToMap(0);
        assertEquals("ID", idRow.get("COLUMN_NAME"));
        assertEquals(Boolean.TRUE, idRow.get("IS_PK"));

        final var nameRow = table.getRowToMap(1);
        assertEquals("NAME", nameRow.get("COLUMN_NAME"));
        assertEquals(Boolean.FALSE, nameRow.get("IS_PK"));
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
        public void run(final ComparableTableMappingContext context) {
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
