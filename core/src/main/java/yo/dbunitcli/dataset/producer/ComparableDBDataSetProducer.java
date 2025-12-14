package yo.dbunitcli.dataset.producer;

import org.apache.commons.lang3.tuple.Pair;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.IResultSetTable;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Stream;

public class ComparableDBDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableDBDataSetProducer.class);
    protected final IDatabaseConnection connection;
    protected final ComparableDataSetParam param;

    public ComparableDBDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.connection = this.param.databaseConnectionLoader().loadConnection();
    }

    @Override
    public ComparableDataSetParam param() {
        return this.param;
    }

    @Override
    public Stream<? extends Source> getSourceStream() {
        return this.getSrcFiles()
                .map(it -> {
                    try {
                        return Pair.of(
                                this.getSource(it)
                                , Files.readAllLines(it.toPath(), Charset.forName(this.param.encoding()))
                        );
                    } catch (final IOException e) {
                        throw new AssertionError(e);
                    }
                })
                .flatMap(it -> it.getRight()
                        .stream()
                        .map(tableName -> it.getLeft().tableName(tableName))
                )
                .filter(it -> this.param().tableNameFilter().predicate(it.tableName()));
    }

    @Override
    public ComparableTableMappingTask createTableMappingTask(final Source source) {
        return new DBTableExecutor(source, this.param, this.connection);
    }

    static class DBTableExecutor implements ComparableTableMappingTask {
        protected final Source source;
        protected final ComparableDataSetParam param;
        protected final IDatabaseConnection connection;

        DBTableExecutor(final Source source,
                        final ComparableDataSetParam param, final IDatabaseConnection connection) {
            this.source = source;
            this.param = param;
            this.connection = connection;
        }

        @Override
        public Source source() {
            return this.source;
        }

        @Override
        public ComparableDataSetParam param() {
            return this.param;
        }

        @Override
        public void run(final ComparableTableMappingContext context) {
            this.executeTable(this.getTable(this.source.tableName()), this.source, context);
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new DBTableExecutor(this.source, builder.build(), this.connection);
        }

        protected ITable getTable(final String tableName) {
            try {
                if (this.param.useJdbcMetaData()) {
                    return this.connection.createDataSet().getTable(tableName);
                }
                return this.connection.createTable(tableName);
            } catch (final DataSetException | SQLException e) {
                throw new AssertionError(e);
            }
        }

        protected void executeTable(final ITable table, final Source source, final ComparableTableMappingContext context) {
            try {
                try {
                    ComparableDBDataSetProducer.LOGGER.info("produce - start databaseTable={}", table.getTableMetaData().getTableName());
                    final ITableMetaData tableMetaData;
                    if (this.param.headerNames() != null) {
                        final ITableMetaData metaData = table.getTableMetaData();
                        final Column[] columns = Arrays.stream(this.param.headerNames(), 0, metaData.getColumns().length)
                                .map(name -> new Column(name.trim(), DataType.UNKNOWN))
                                .toArray(Column[]::new);
                        tableMetaData = new DefaultTableMetaData(table.getTableMetaData().getTableName()
                                , columns
                                , Arrays.stream(table.getTableMetaData().getPrimaryKeys())
                                .mapToInt(column -> {
                                    try {
                                        return metaData.getColumnIndex(column.getColumnName());
                                    } catch (final DataSetException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .mapToObj(i -> columns[i])
                                .toArray(Column[]::new));
                    } else {
                        tableMetaData = table.getTableMetaData();
                    }
                    final ComparableTableMapper mapper = context.createMapper(source.wrap(tableMetaData));
                    mapper.startTable();
                    if (this.param.loadData()) {
                        final Column[] columns = table.getTableMetaData().getColumns();
                        int row = 0;
                        for (; true; row++) {
                            try {
                                final Object[] rows = new Object[columns.length];
                                int columnIndex = 0;
                                for (final Column column : columns) {
                                    rows[columnIndex++] = table.getValue(row, column.getColumnName());
                                }
                                mapper.addRow(rows);
                            } catch (final RowOutOfBoundsException e) {
                                break;
                            }
                        }
                        ComparableDBDataSetProducer.LOGGER.info("produce - rows={}", row);
                    }
                    mapper.endTable();
                    ComparableDBDataSetProducer.LOGGER.info("produce - end   databaseTable={}", table.getTableMetaData().getTableName());
                } finally {
                    if (table instanceof final IResultSetTable resultSetTable) {
                        resultSetTable.close();
                    }
                }
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        }
    }
}
