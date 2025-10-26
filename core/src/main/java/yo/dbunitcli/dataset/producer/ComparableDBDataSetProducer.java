package yo.dbunitcli.dataset.producer;

import org.apache.commons.lang3.tuple.Pair;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.IResultSetTable;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableTableMappingContext;

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
    private final IDataSet databaseDataSet;

    public ComparableDBDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.connection = this.param.databaseConnectionLoader().loadConnection();
        try {
            this.databaseDataSet = this.param.useJdbcMetaData()
                    ? this.connection.createDataSet()
                    : null;
        } catch (final SQLException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public ComparableDataSetParam param() {
        return this.param;
    }

    @Override
    public Stream<Source> getSourceStream() {
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
    public Runnable createTableMappingTask(final Source source, final ComparableTableMappingContext context) {
        return new DBTableExecutor(source, context, this.param, this.connection, this.databaseDataSet);
    }

    static class DBTableExecutor implements Runnable {
        protected final Source source;
        protected final ComparableTableMappingContext context;
        protected final ComparableDataSetParam param;
        protected final IDatabaseConnection connection;
        protected final IDataSet databaseDataSet;

        DBTableExecutor(final Source source, final ComparableTableMappingContext context,
                        final ComparableDataSetParam param, final IDatabaseConnection connection,
                        final IDataSet databaseDataSet) {
            this.source = source;
            this.context = context;
            this.param = param;
            this.connection = connection;
            this.databaseDataSet = databaseDataSet;
        }

        @Override
        public void run() {
            this.executeTable(this.getTable(this.source.tableName()), this.source);
        }

        protected ITable getTable(final String tableName) {
            try {
                if (this.databaseDataSet != null) {
                    return this.databaseDataSet.getTable(tableName);
                }
                return this.connection.createTable(tableName);
            } catch (final DataSetException | SQLException e) {
                throw new AssertionError(e);
            }
        }

        protected void executeTable(final ITable table, final Source source) {
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
                    this.context.startTable(source.wrap(tableMetaData));
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
                                this.context.row(rows);
                            } catch (final RowOutOfBoundsException e) {
                                break;
                            }
                        }
                        ComparableDBDataSetProducer.LOGGER.info("produce - rows={}", row);
                    }
                    this.context.endTable();
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
