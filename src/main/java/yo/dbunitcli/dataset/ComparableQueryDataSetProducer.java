package yo.dbunitcli.dataset;

import com.google.common.io.Files;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.*;

public class ComparableQueryDataSetProducer extends ComparableDBDataSetProducer {
    private static final Logger logger = LoggerFactory.getLogger(ComparableQueryDataSetProducer.class);
    private File[] srcFiles;

    public ComparableQueryDataSetProducer(IDatabaseConnection connection, File srcDir, String encoding) throws DataSetException {
        super(connection, srcDir, encoding);
        if (!this.src.isDirectory()) {
            throw new DataSetException("'" + srcDir + "' should be a directory");
        }
        this.srcFiles = this.src.listFiles(File::isFile);
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
        ITable table = this.connection.createQueryTable(tableName, Files.asCharSource(aFile, Charset.forName(this.encoding)).read());
        this.executeTable(table);
    }

}
