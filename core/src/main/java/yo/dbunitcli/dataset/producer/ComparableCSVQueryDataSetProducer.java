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

import java.sql.*;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public record ComparableCSVQueryDataSetProducer(ComparableDataSetParam param,
                                                Parameter parameter) implements QueryDataSetProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableCSVQueryDataSetProducer.class);

    // CSVREADの第1引数が文字列リテラルのもの（'' は ' のエスケープ）
    private static final Pattern CSVREAD_FILE = Pattern.compile("(?i)(\\bCSVREAD\\s*\\(\\s*)'((?:[^']|'')*)'");

    private static String url() {
        return "jdbc:h2:file:" + FileResources.datasetDir().getAbsolutePath() + ";MODE=Oracle";
    }

    /**
     * H2は相対パスを起動時のカレントディレクトリから解決するため、
     * CSVREADの相対パスをsrcの他のファイルと同じ探索順（{@link FileResources#searchDatasetBase}）で絶対パスにする
     */
    static String resolveCsvReadPaths(final String query) {
        final Matcher matcher = CSVREAD_FILE.matcher(query);
        final StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            final String path = matcher.group(2).replace("''", "'");
            final String resolved = FileResources.searchDatasetBase(path).getAbsolutePath().replace('\\', '/');
            matcher.appendReplacement(result, Matcher.quoteReplacement(
                    matcher.group(1) + "'" + resolved.replace("'", "''") + "'"));
        }
        matcher.appendTail(result);
        return result.toString();
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
                final String query = ComparableCSVQueryDataSetProducer.resolveCsvReadPaths(
                        QueryDataSetProducer.renderQuery(this.param, this.source, this.parameter));
                ComparableCSVQueryDataSetProducer.LOGGER.info("produce - start filePath={},query={}", this.source.filePath(), query);
                try (final Connection conn = DriverManager.getConnection(ComparableCSVQueryDataSetProducer.url());
                     final PreparedStatement stmt = conn.prepareStatement(query)
                ) {
                    // 列は文の準備で決まり、CSVREADは見出し行だけを読む。行を読まないなら実行しない
                    final ResultSetMetaData resultMetaData = stmt.getMetaData();
                    final int resultColumnCount = resultMetaData.getColumnCount();
                    final TableMetaDataWithSource metaData = this.source.createMetaData(this.param.headerNames() != null
                            ? this.param.headerNames()
                            : ComparableCSVQueryDataSetProducer.columnNames(resultMetaData));
                    final ComparableTableMapper mapper = context.createMapper(metaData);
                    mapper.startTable();
                    if (this.param.loadData() && mapper.hasOutput()) {
                        try (final ResultSet rst = stmt.executeQuery()) {
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
