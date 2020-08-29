package yo.dbunitcli.fileprocessor;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import yo.dbunitcli.dataset.DataSetWriterParam;
import yo.dbunitcli.dataset.IDataSetWriter;
import yo.dbunitcli.dataset.producer.ComparableFileTableMetaData;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Statement;
import java.util.List;

public class SqlRunner implements IDataSetWriter {
    private final IDatabaseConnection connection;

    public SqlRunner(DataSetWriterParam param) throws DataSetException {
        this.connection = param.getDatabaseConnectionLoader().loadConnection();
    }

    @Override
    public void cleanupDirectory() {
        // no implements
    }

    @Override
    public void write(ITable aTable) throws DataSetException {
        try {
            for (int row = 0, lastRow = aTable.getRowCount(); row < lastRow; row++) {
                String sqlFile = aTable.getValue(row, ComparableFileTableMetaData.PK.getColumnName()).toString();
                for (String sql : this.loadSqlList(sqlFile)) {
                    Statement statement = null;
                    try {
                        statement = this.connection.getConnection().createStatement();
                        statement.executeQuery(sql);
                    } finally {
                        if (statement != null) {
                            statement.close();
                        }
                    }
                }
            }
        } catch (Exception var30) {
            throw new DataSetException(var30);
        }
    }

    protected List<String> loadSqlList(String aFileName) throws IOException {
        return Lists.newArrayList(Files.asCharSource(new File(aFileName), Charset.forName("UTF-8"))
                .read()
                .split(";"));
    }
}
