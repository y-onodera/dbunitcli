package yo.dbunitcli.fileprocessor;

import org.apache.tools.ant.taskdefs.SQLExec;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.DatabaseConnectionLoader;

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SqlRunner implements QueryReader {
    private final DatabaseConnectionLoader connectionLoader;
    private final Map<String, Object> parameter;
    private final String encoding;
    private final char templateVarStart;
    private final char templateVarStop;

    public SqlRunner(DatabaseConnectionLoader connectionLoader, Map<String, Object> parameter, String encoding, char templateVarStart, char templateVarStop) {
        this.connectionLoader = connectionLoader;
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
        try {
            for (File target : targetFiles) {
                Connection conn = connectionLoader.loadConnection().getConnection();
                conn.setAutoCommit(false);
                SQLExec exec = new SQLExec() {
                    @Override
                    protected Connection getConnection() {
                        return conn;
                    }

                    @Override
                    protected void execSQL(String sql, PrintStream out) throws SQLException {
                        super.execSQL(applyParameter(sql), out);
                    }
                };
                exec.setExpandProperties(false);
                exec.setEncoding(this.getEncoding());
                exec.setSrc(target);
                if(target.getName().endsWith("plsql")){
                    exec.setDelimiter(SQLExec.DelimiterType.ROW);
                    exec.setKeepformat(true);
                }
                exec.execute();
            }
        } catch (Throwable var30) {
            throw new DataSetException(var30);
        }
    }

}
