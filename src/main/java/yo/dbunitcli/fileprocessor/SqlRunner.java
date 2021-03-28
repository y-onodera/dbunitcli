package yo.dbunitcli.fileprocessor;

import com.google.common.io.CharStreams;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.DatabaseConnectionLoader;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

public class SqlRunner implements Runner, QueryReader {
    private final DatabaseConnectionLoader connectionLoader;
    private final Map<String, Object> parameter;
    private final String encoding;
    private final char templateVarStart;
    private final char templateVarStop;
    private final String templateParameterAttribute;

    public SqlRunner(DatabaseConnectionLoader connectionLoader
            , Map<String, Object> parameter
            , String encoding
            , char templateVarStart, char templateVarStop
            , String templateParameterAttribute) {
        this.connectionLoader = connectionLoader;
        this.parameter = parameter;
        this.encoding = encoding;
        this.templateVarStart = templateVarStart;
        this.templateVarStop = templateVarStop;
        this.templateParameterAttribute = templateParameterAttribute;
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
    public String getTemplateParameterAttribute() {
        return this.templateParameterAttribute;
    }

    @Override
    public char getTemplateVarStart() {
        return this.templateVarStart;
    }

    @Override
    public char getTemplateVarStop() {
        return this.templateVarStop;
    }

    @Override
    public void runScript(Collection<File> targetFiles) throws DataSetException {
        try {
            for (File target : targetFiles) {
                Connection conn = this.getConnection();
                SQLExec exec = this.createExecInstance(conn);
                exec.setSrc(target);
                if (target.getName().endsWith("plsql")) {
                    SQLExec.DelimiterType delimiterType = new SQLExec.DelimiterType();
                    delimiterType.setValue("row");
                    exec.setDelimiterType(delimiterType);
                    exec.setDelimiter("/");
                    exec.setKeepformat(true);
                }
                exec.execute();
            }
        } catch (Throwable var30) {
            throw new DataSetException(var30);
        }
    }

    private SQLExec createExecInstance(Connection conn) {
        SQLExec exec = new SQLExec() {
            @Override
            protected Connection getConnection() {
                return conn;
            }

            @Override
            protected void runStatements(Reader reader, PrintStream out) throws SQLException, IOException {
                super.runStatements(new StringReader(applyParameter(CharStreams.toString(reader))), out);
            }
        };
        exec.setExpandProperties(false);
        exec.setEncoding(this.getEncoding());
        return exec;
    }

    private Connection getConnection() throws SQLException, DataSetException {
        Connection conn = this.connectionLoader.loadConnection().getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

}
