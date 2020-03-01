package yo.dbunitcli.dataset.producer;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.QueryReader;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class ComparableQueryDataSetProducer extends ComparableDBDataSetProducer implements QueryReader {

    private static final Logger logger = LoggerFactory.getLogger(ComparableQueryDataSetProducer.class);
    private File[] srcFiles;
    private final Parameter parameter;

    public ComparableQueryDataSetProducer(IDatabaseConnection connection, ComparableDataSetParam param, Parameter parameter) {
        super(connection, param);
        if (this.getParam().getSrc().isDirectory()) {
            this.srcFiles = this.getParam().getSrc().listFiles(File::isFile);
        } else {
            this.srcFiles = new File[]{this.getParam().getSrc()};
        }
        this.parameter = parameter;
    }

    @Override
    public void produce() throws DataSetException {
        logger.info("produce() - start");
        this.consumer.startDataSet();
        for (File file : this.srcFiles) {
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
        String query = this.readQuery(aFile);
        logger.info("produceFromQuery(query={}) - start", query);
        String tableName = aFile.getName().substring(0, aFile.getName().indexOf("."));
        ITable table = this.connection.createQueryTable(tableName, query);
        this.executeTable(table);
    }

    @Override
    public Map<String, Object> getParameter() {
        return this.parameter.getMap();
    }

    @Override
    public String getEncoding() {
        return this.encoding;
    }
}
