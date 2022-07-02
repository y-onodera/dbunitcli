package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.resource.st4.TemplateRender;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.TableNameFilter;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

public class ComparableCSVQueryDataSetProducer implements ComparableDataSetProducer {

    private static final Logger logger = LoggerFactory.getLogger(ComparableCSVQueryDataSetProducer.class);
    private static final String URL = "jdbc:h2:mem:test;MODE=Oracle";
    private IDataSetConsumer consumer = new DefaultConsumer();
    private final File[] src;
    private final String encoding;
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
        this.encoding = param.getEncoding();
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
        logger.info("produce() - start");
        this.consumer.startDataSet();
        for (File file : this.src) {
            if (this.filter.predicate(file.getAbsolutePath())) {
                try {
                    this.executeQuery(file);
                } catch (SQLException | IOException e) {
                    throw new DataSetException(e);
                }
            }
        }
        this.consumer.endDataSet();
    }

    protected void executeQuery(File aFile) throws SQLException, DataSetException, IOException {
        String query = this.getTemplateLoader().render(aFile, this.getParameter());
        logger.info("produceFromQuery(query={}) - start", query);
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rst = stmt.executeQuery(query)
        ) {
            ResultSetMetaData metaData = rst.getMetaData();
            Column[] columns = new Column[metaData.getColumnCount()];
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                columnName = columnName.trim();
                columns[i - 1] = new Column(columnName, DataType.UNKNOWN);
            }
            String tableName = aFile.getName().substring(0, aFile.getName().indexOf("."));
            ITableMetaData tableMetaData = new DefaultTableMetaData(tableName, columns);
            this.consumer.startTable(tableMetaData);
            if (this.loadData) {
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
                }
            }
            this.consumer.endTable();
        }
    }
}
