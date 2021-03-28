package yo.dbunitcli.fileprocessor;

import com.google.common.io.CharStreams;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.dbunit.dataset.DataSetException;
import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.dataset.DatabaseConnectionLoader;

import java.io.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

public class SqlRunner implements Runner, QueryReader {
    private static final Pattern SQLPlUS_SET = Pattern.compile("SET\\s+(DEFINE|ECHO|TIMING|SERVEROUTPUT)\\s+(ON|OFF).*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SQLPLUS_SPOOL = Pattern.compile("SPOOL\\s.*$", Pattern.CASE_INSENSITIVE);
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
                String statement = applyParameter(CharStreams.toString(reader));
                statement = SQLPlUS_SET.matcher(statement).replaceAll("");
                statement = SQLPLUS_SPOOL.matcher(statement).replaceAll("");
                boolean dbmsOutput = statement.contains("dbms_output");
                super.runStatements(new StringReader(statement), out);
                if (dbmsOutput) {
                    printDbmsOutputResults(conn);
                }
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

    private void printDbmsOutputResults(Connection conn) throws java.sql.SQLException {
        String getLineSql = "begin dbms_output.get_line(?,?); end;";
        CallableStatement stmt = conn.prepareCall(getLineSql);
        boolean hasMore = true;
        stmt.registerOutParameter(1, Types.VARCHAR);
        stmt.registerOutParameter(2, Types.INTEGER);

        while (hasMore) {
            boolean status = stmt.execute();
            hasMore = (stmt.getInt(2) == 0);

            if (hasMore) {
                System.err.println(stmt.getString(1));
            }
        }
        stmt.close();
    }
}
