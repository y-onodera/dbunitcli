package yo.dbunitcli.fileprocessor;

import com.google.common.io.CharStreams;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.dbunit.dataset.DataSetException;
import org.stringtemplate.v4.STGroup;
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
    private final String templateParameterAttribute;
    private final STGroup sTGroup;

    public SqlRunner(DatabaseConnectionLoader connectionLoader
            , Map<String, Object> parameter
            , String encoding
            , STGroup sTGroup
            , String templateParameterAttribute) {
        this.connectionLoader = connectionLoader;
        this.parameter = parameter;
        this.encoding = encoding;
        this.sTGroup = sTGroup;
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
    public STGroup getSTGroup() {
        return this.sTGroup;
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
