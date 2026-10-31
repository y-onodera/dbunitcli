package yo.dbunitcli.dataset.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTableMapper;
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

    private static String url() {
        return "jdbc:h2:file:" + FileResources.datasetDir().getAbsolutePath() + ";MODE=Oracle";
    }

    private static String[] columnNames(final ResultSetMetaData metaData) throws SQLException {
        final String[] result = new String[metaData.getColumnCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = metaData.getColumnName(i + 1);
        }
        return result;
    }

    /**
     * タスクごとにH2へ接続し、テンプレートもタスクごとに描画するため、タスク間で共有する状態は無い
     */
    @Override
    public boolean isParallelizableSource() {
        return true;
    }

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
                try (final Connection conn = DriverManager.getConnection(ComparableCSVQueryDataSetProducer.url());
                     final Statement stmt = conn.createStatement();
                     final ResultSet rst = stmt.executeQuery(query)
                ) {
                    final int resultColumnCount = rst.getMetaData().getColumnCount();
                    final TableMetaDataWithSource metaData = this.source.createMetaData(this.param.headerNames() != null
                            ? this.param.headerNames()
                            : ComparableCSVQueryDataSetProducer.columnNames(rst.getMetaData()));
                    final ComparableTableMapper mapper = context.createMapper(metaData);
                    mapper.startTable();
                    if (this.param.loadData() && mapper.hasOutput()) {
                        int readRows = 0;
                        while (rst.next()) {
                            mapper.addRow(metaData.toRow(IntStream.rangeClosed(1, resultColumnCount)
                                    .mapToObj(i -> {
                                        try {
                                            return Optional.ofNullable(rst.getString(i))
                                                    .orElse("");
                                        } catch (final SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .toArray()));
                            readRows++;
                        }
                        ComparableCSVQueryDataSetProducer.LOGGER.info("produce - rows={}", readRows);
                    }
                    mapper.endTable();
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
