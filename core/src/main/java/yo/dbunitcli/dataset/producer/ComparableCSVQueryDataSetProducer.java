package yo.dbunitcli.dataset.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.IDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.TableNameFilter;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.Map;

public class ComparableCSVQueryDataSetProducer implements ComparableDataSetProducer {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String URL = "jdbc:h2:mem:test;MODE=Oracle";
    private IDataSetConsumer consumer;
    private final File[] src;
    private final Parameter parameter;
    private final TableNameFilter filter;
    private final ComparableDataSetParam param;
    private final boolean loadData;

    public ComparableCSVQueryDataSetProducer(final ComparableDataSetParam param, final Parameter parameter) {
        this.param = param;
        if (this.param.src().isDirectory()) {
            this.src = this.param.src().listFiles(File::isFile);
        } else {
            this.src = new File[]{this.param.src()};
        }
        this.filter = param.tableNameFilter();
        this.parameter = parameter;
        this.loadData = this.param.loadData();
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
        LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        Arrays.stream(this.src)
                .filter(file -> this.filter.predicate(file.getAbsolutePath()) && file.length() > 0)
                .forEach(this::produceFromFile);
        this.consumer.endDataSet();
        LOGGER.info("produce() - end");
    }

    protected void produceFromFile(final File aFile) {
        try {
            final String query = this.getTemplateLoader().render(aFile, this.getParameter());
            LOGGER.info("produce - start fileName={},query={}", aFile, query);
            try (final Connection conn = DriverManager.getConnection(URL);
                 final Statement stmt = conn.createStatement();
                 final ResultSet rst = stmt.executeQuery(query)
            ) {
                final ResultSetMetaData metaData = rst.getMetaData();
                final Column[] columns = new Column[metaData.getColumnCount()];
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columns[i - 1] = new Column(metaData.getColumnName(i).trim(), DataType.UNKNOWN);
                }
                this.consumer.startTable(this.createMetaData(aFile, columns));
                if (this.loadData) {
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
                    LOGGER.info("produce - rows={}", readRows);
                }
                this.consumer.endTable();
                LOGGER.info("produce - end   fileName={}", aFile);
            }
        } catch (final SQLException | DataSetException e) {
            throw new AssertionError(e);
        }
    }
}
