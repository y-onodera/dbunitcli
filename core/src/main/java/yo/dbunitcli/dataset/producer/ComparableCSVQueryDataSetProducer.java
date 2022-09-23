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
import java.io.IOException;
import java.sql.*;
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

    public ComparableCSVQueryDataSetProducer(ComparableDataSetParam param, Parameter parameter) {
        this.param = param;
        if (this.param.getSrc().isDirectory()) {
            this.src = this.param.getSrc().listFiles(File::isFile);
        } else {
            this.src = new File[]{this.param.getSrc()};
        }
        this.filter = param.getTableNameFilter();
        this.parameter = parameter;
        this.loadData = this.param.isLoadData();
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    public Map<String, Object> getParameter() {
        return this.parameter.getMap();
    }

    public TemplateRender getTemplateLoader() {
        return this.getParam().getStTemplateLoader();
    }

    @Override
    public void setConsumer(IDataSetConsumer iDataSetConsumer) {
        this.consumer = iDataSetConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        for (File file : this.src) {
            if (this.filter.predicate(file.getAbsolutePath()) && file.length() > 0) {
                try {
                    this.executeQuery(file);
                } catch (SQLException | IOException e) {
                    throw new DataSetException(e);
                }
            }
        }
        this.consumer.endDataSet();
        LOGGER.info("produce() - end");
    }

    protected void executeQuery(File aFile) throws SQLException, DataSetException, IOException {
        String query = this.getTemplateLoader().render(aFile, this.getParameter());
        LOGGER.info("produce - start fileName={},query={}", aFile, query);
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rst = stmt.executeQuery(query)
        ) {
            ResultSetMetaData metaData = rst.getMetaData();
            Column[] columns = new Column[metaData.getColumnCount()];
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                columns[i - 1] = new Column(metaData.getColumnName(i).trim(), DataType.UNKNOWN);
            }
            this.consumer.startTable(this.createMetaData(aFile, columns));
            if (this.loadData) {
                int readRows = 0;
                while (rst.next()) {
                    Object[] row = new Object[metaData.getColumnCount()];
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
    }
}
