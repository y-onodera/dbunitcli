package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTableMappingContext;
import yo.dbunitcli.dataset.ComparableTableMappingTask;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.sql.*;
import java.util.Optional;
import java.util.stream.IntStream;

public record ComparableCSVQueryDataSetProducer(ComparableDataSetParam param,
                                                Parameter parameter) implements QueryDataSetProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableCSVQueryDataSetProducer.class);
    private static final String URL = "jdbc:h2:file:" + FileResources.datasetDir().getAbsolutePath() + ";MODE=Oracle";

    @Override
    public ComparableTableMappingTask createTableMappingTask(final Source source) {
        return new CsvQueryTableExecutor(source, this.param, this.parameter);
    }

    private record CsvQueryTableExecutor(Source source, ComparableDataSetParam param,
                                         Parameter parameter) implements ComparableTableMappingTask {

        @Override
        public void run(final ComparableTableMappingContext context) {
            try {
                final String query = this.param.templateRender().render(new File(this.source.filePath()), this.parameter);
                ComparableCSVQueryDataSetProducer.LOGGER.info("produce - start filePath={},query={}", this.source.filePath(), query);
                try (final Connection conn = DriverManager.getConnection(ComparableCSVQueryDataSetProducer.URL);
                     final Statement stmt = conn.createStatement();
                     final ResultSet rst = stmt.executeQuery(query)
                ) {
                    final ResultSetMetaData metaData = rst.getMetaData();
                    final Column[] columns = IntStream.range(0, metaData.getColumnCount())
                            .mapToObj(i -> {
                                try {
                                    return Optional.ofNullable(this.param.headerNames())
                                            .map(it -> it[i])
                                            .orElse(metaData.getColumnName(i + 1));
                                } catch (final SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .map(name -> new Column(name.trim(), DataType.UNKNOWN))
                            .toArray(Column[]::new);
                    context.startTable(this.source.createMetaData(columns));
                    if (this.param.loadData()) {
                        int readRows = 0;
                        while (rst.next()) {
                            context.row(IntStream.rangeClosed(1, metaData.getColumnCount())
                                    .mapToObj(i -> {
                                        try {
                                            return Optional.ofNullable(rst.getString(i))
                                                    .orElse("");
                                        } catch (final SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .toArray());
                            readRows++;
                        }
                        ComparableCSVQueryDataSetProducer.LOGGER.info("produce - rows={}", readRows);
                    }
                    context.endTable();
                    ComparableCSVQueryDataSetProducer.LOGGER.info("produce - end   filePath={}", this.source.filePath());
                }
            } catch (final SQLException e) {
                throw new AssertionError(e);
            }
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new CsvQueryTableExecutor(this.source, builder.build(), this.parameter);
        }

    }
}
