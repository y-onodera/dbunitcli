package yo.dbunitcli.fileprocessor;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;

import java.io.File;
import java.io.IOException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqlRunner implements QueryReader {
    private final IDatabaseConnection connection;
    private final Map<String, Object> parameter;
    private final String encoding;
    private final char templateVarStart;
    private final char templateVarStop;

    public SqlRunner(IDatabaseConnection connection, Map<String, Object> parameter, String encoding, char templateVarStart, char templateVarStop) {
        this.connection = connection;
        this.parameter = parameter;
        this.encoding = encoding;
        this.templateVarStart = templateVarStart;
        this.templateVarStop = templateVarStop;
    }

    @Override
    public Map<String, Object> getParameter() {
        return this.parameter;
    }

    @Override
    public String getEncoding() {
        return this.encoding;
    }

    @Override
    public char getTemplateVarStart() {
        return this.templateVarStart;
    }

    @Override
    public char getTemplateVarStop() {
        return this.templateVarStop;
    }

    public void run(List<File> targetFiles) throws DataSetException {
        for (File target : targetFiles) {
            this.run(target);
        }
    }

    public void run(File target) throws DataSetException {
        try {
            this.connection.getConnection().setAutoCommit(false);
            for (String sql : this.loadSqlList(target)) {
                Statement statement = null;
                try {
                    statement = this.connection.getConnection().createStatement();
                    statement.execute(sql);
                } catch (Throwable ex) {
                    this.connection.getConnection().rollback();
                    throw ex;
                } finally {
                    if (statement != null) {
                        statement.close();
                    }
                }
            }
            this.connection.getConnection().commit();
        } catch (Throwable var30) {
            throw new DataSetException(var30);
        }
    }

    protected List<String> loadSqlList(File aTargetFile) throws IOException {
        return Stream.of(this.readQuery(aTargetFile)
                .split(";"))
                .map(it -> it.replace("commit", ""))
                .filter(it -> it.trim().length() > 0)
                .collect(Collectors.toList());
    }

}
