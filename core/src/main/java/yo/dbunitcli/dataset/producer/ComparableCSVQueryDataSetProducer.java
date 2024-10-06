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

public class ComparableCSVQueryDataSetProducer implements ComparableDataSetProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableCSVQueryDataSetProducer.class);
    private static final String URL = "jdbc:h2:file:" + FileResources.datasetDir().getAbsolutePath() + ";MODE=Oracle";
    private final File[] src;
    private final Parameter parameter;
    private final ComparableDataSetParam param;
    private IDataSetConsumer consumer;

    public ComparableCSVQueryDataSetProducer(final ComparableDataSetParam param, final Parameter parameter) {
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.parameter = parameter;
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
                final Column[] columns = new Column[metaData.getColumnCount()];
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columns[i - 1] = new Column(metaData.getColumnName(i).trim(), DataType.UNKNOWN);
                }
                this.consumer.startTable(this.createMetaData(aFile, columns));
                if (this.param.loadData()) {
                    int readRows = 0;
                    while (rst.next()) {
                        final Object[] row = new Object[metaData.getColumnCount()];
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            if (rst.getString(i) != null) {
                                row[i - 1] = rst.getString(i);
                            } else {
                                row[i - 1] = "";
                            }
                        }
                        this.consumer.row(row);
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
