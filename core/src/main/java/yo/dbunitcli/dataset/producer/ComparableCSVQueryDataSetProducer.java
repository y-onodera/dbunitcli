package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.sql.*;
import java.util.Optional;
import java.util.stream.IntStream;

public class ComparableCSVQueryDataSetProducer implements QueryDataSetProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableCSVQueryDataSetProducer.class);
    private static final String URL = "jdbc:h2:file:" + FileResources.datasetDir().getAbsolutePath() + ";MODE=Oracle";
    private final ComparableDataSetParam param;
    private final Parameter parameter;
    private ComparableDataSetConsumer consumer;

    public ComparableCSVQueryDataSetProducer(final ComparableDataSetParam param, final Parameter parameter) {
        this.param = param;
        this.parameter = parameter;
    }

    @Override
    public Parameter getParameter() {
        return this.parameter;
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
    public void executeTable(final Source source) {
        try {
            final String query = this.getTemplateLoader().render(new File(source.filePath()), this.getParameter());
            ComparableCSVQueryDataSetProducer.LOGGER.info("produce - start filePath={},query={}", source.filePath(), query);
            try (final Connection conn = DriverManager.getConnection(ComparableCSVQueryDataSetProducer.URL);
                 final Statement stmt = conn.createStatement();
                 final ResultSet rst = stmt.executeQuery(query)
            ) {
                final ResultSetMetaData metaData = rst.getMetaData();
                final Column[] columns = IntStream.range(0, metaData.getColumnCount())
                        .mapToObj(i -> {
                            try {
                                return this.getHeaderNames() != null ? this.getHeaderNames()[i] : metaData.getColumnName(i + 1);
                            } catch (final SQLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .map(name -> new Column(name.trim(), DataType.UNKNOWN))
                        .toArray(Column[]::new);
                this.getConsumer().startTable(this.createMetaData(source, columns));
                if (this.loadData()) {
                    int readRows = 0;
                    while (rst.next()) {
                        this.getConsumer().row(IntStream.rangeClosed(1, metaData.getColumnCount())
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
                this.getConsumer().endTable();
                ComparableCSVQueryDataSetProducer.LOGGER.info("produce - end   filePath={}", source.filePath());
            }
        } catch (final SQLException | DataSetException e) {
            throw new AssertionError(e);
        }
    }

}
