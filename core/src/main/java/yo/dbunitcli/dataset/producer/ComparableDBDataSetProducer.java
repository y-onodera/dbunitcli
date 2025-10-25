package yo.dbunitcli.dataset.producer;

import org.apache.commons.lang3.tuple.Pair;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.IResultSetTable;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

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
    protected ComparableDataSetConsumer consumer;
    private IDataSet databaseDataSet;

    public ComparableDBDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.connection = this.param.databaseConnectionLoader().loadConnection();
        if (this.param.useJdbcMetaData()) {
            try {
                this.databaseDataSet = this.connection.createDataSet();
            } catch (final SQLException e) {
                throw new AssertionError(e);
            }
        }
    }

    @Override
    public void setConsumer(final ComparableDataSetConsumer iDataSetConsumer) {
        this.consumer = iDataSetConsumer;
    }

    @Override
    public ComparableDataSetConsumer getConsumer() {
        return this.consumer;
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public Stream<Source> getSourceStream() {
        return Stream.of(this.getSrcFiles())
                .map(it -> {
                    try {
                        return Pair.of(
                                this.getSource(it)
                                , Files.readAllLines(it.toPath(), Charset.forName(this.getEncoding()))
                        );
                    } catch (final IOException e) {
                        throw new AssertionError(e);
                    }
                })
                .flatMap(it -> it.getRight()
                        .stream()
                        .map(tableName -> it.getLeft().tableName(tableName))
                )
                .filter(it -> this.getParam().tableNameFilter().predicate(it.tableName()));
    }

    @Override
    public void executeTable(final Source source) {
        this.executeTable(this.getTable(source.tableName()), source);
    }

    protected void executeTable(final ITable table, final Source source) {
        try {
            try {
                ComparableDBDataSetProducer.LOGGER.info("produce - start databaseTable={}", table.getTableMetaData().getTableName());
                final ITableMetaData tableMetaData;
                if (this.getHeaderNames() != null) {
                    final ITableMetaData metaData = table.getTableMetaData();
                    final Column[] columns = Arrays.stream(this.getHeaderNames(), 0, metaData.getColumns().length)
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
                this.getConsumer().startTable(source.wrap(tableMetaData));
                if (this.loadData()) {
                    final Column[] columns = table.getTableMetaData().getColumns();
                    int row = 0;
                    for (; true; row++) {
                        try {
                            final Object[] rows = new Object[columns.length];
                            int columnIndex = 0;
                            for (final Column column : columns) {
                                rows[columnIndex++] = table.getValue(row, column.getColumnName());
                            }
                            this.getConsumer().row(rows);
                        } catch (final RowOutOfBoundsException e) {
                            break;
                        }
                    }
                    ComparableDBDataSetProducer.LOGGER.info("produce - rows={}", row);
                }
                this.getConsumer().endTable();
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

    private ITable getTable(final String tableName) {
        try {
            if (this.databaseDataSet != null) {
                return this.databaseDataSet.getTable(tableName);
            }
            return this.connection.createTable(tableName);
        } catch (final DataSetException | SQLException e) {
            throw new AssertionError(e);
        }
    }
}
