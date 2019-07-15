package yo.dbunitcli.dataset;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class ComparableQueryDataSetProducer extends ComparableDBDataSetProducer implements QueryReader {
    private static final Logger logger = LoggerFactory.getLogger(ComparableQueryDataSetProducer.class);
    private File[] srcFiles;
    private final Map<String, Object> parameter;

    public ComparableQueryDataSetProducer(IDatabaseConnection connection, File srcDir, String encoding, Map<String, Object> parameter) throws DataSetException {
        super(connection, srcDir, encoding);
        if (!this.src.isDirectory()) {
            throw new DataSetException("'" + srcDir + "' should be a directory");
        }
        this.srcFiles = this.src.listFiles(File::isFile);
        this.parameter = parameter;
    }

    @Override
    public void produce() throws DataSetException {
        logger.debug("produce() - start");
        this.consumer.startDataSet();
        for (File file : this.srcFiles) {
            try {
                this.executeQuery(file);
            } catch (SQLException | IOException e) {
                throw new DataSetException(e);
            }
        }
        this.consumer.endDataSet();
    }

    protected void executeQuery(File aFile) throws SQLException, DataSetException, IOException {
        String tableName = aFile.getName().substring(0, aFile.getName().indexOf("."));
        ITable table = this.connection.createQueryTable(tableName, readQuery(aFile));
        this.executeTable(table);
    }

    @Override
    public Map<String, Object> getParameter() {
        return this.parameter;
    }

    @Override
    public String getEncoding() {
        return this.encoding;
    }
}
