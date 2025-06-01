package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class ComparableCSVQueryDataSetProducer implements ComparableDataSetProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableCSVQueryDataSetProducer.class);
    private static final String URL = "jdbc:h2:file:" + FileResources.datasetDir().getAbsolutePath() + ";MODE=Oracle";
    private final File[] src;
    private final Parameter parameter;
    private final ComparableDataSetParam param;
    private final String[] headerNames;
    private final boolean loadData;
    private final boolean addFileInfo;
    private IDataSetConsumer consumer;

    public ComparableCSVQueryDataSetProducer(final ComparableDataSetParam param, final Parameter parameter) {
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.parameter = parameter;
        this.headerNames = this.param.headerNames();
        this.loadData = this.param.loadData();
        this.addFileInfo = this.param.addFileInfo();
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    public Map<String, Object> getParameter() {
        return this.parameter.getMap();
    }

    public TemplateRender getTemplateLoader() {
        return this.getParam().templateRender();
    }

    @Override
    public void setConsumer(final IDataSetConsumer iDataSetConsumer) {
        this.consumer = iDataSetConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        ComparableCSVQueryDataSetProducer.LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        Arrays.stream(this.src)
                .filter(it -> this.getParam().tableNameFilter().predicate(this.getTableName(it)))
                .forEach(this::produceFromFile);
        this.consumer.endDataSet();
        ComparableCSVQueryDataSetProducer.LOGGER.info("produce() - end");
    }

    protected void produceFromFile(final File aFile) {
        try {
            final String query = this.getTemplateLoader().render(aFile, this.getParameter());
            ComparableCSVQueryDataSetProducer.LOGGER.info("produce - start fileName={},query={}", aFile, query);
            try (final Connection conn = DriverManager.getConnection(ComparableCSVQueryDataSetProducer.URL);
                 final Statement stmt = conn.createStatement();
                 final ResultSet rst = stmt.executeQuery(query)
            ) {
                final ResultSetMetaData metaData = rst.getMetaData();
                final Column[] columns = IntStream.range(0, metaData.getColumnCount())
                        .mapToObj(i -> {
                            try {
                                return this.headerNames != null ? this.headerNames[i] : metaData.getColumnName(i + 1);
                            } catch (final SQLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .map(name -> new Column(name.trim(), DataType.UNKNOWN))
                        .toArray(Column[]::new);
                this.consumer.startTable(this.createMetaData(aFile, columns, this.addFileInfo));
                if (this.loadData) {
                    int readRows = 0;
                    while (rst.next()) {
                        this.consumer.row(IntStream.rangeClosed(1, metaData.getColumnCount())
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
                this.consumer.endTable();
                ComparableCSVQueryDataSetProducer.LOGGER.info("produce - end   fileName={}", aFile);
            }
        } catch (final SQLException | DataSetException e) {
            throw new AssertionError(e);
        }
    }
}
