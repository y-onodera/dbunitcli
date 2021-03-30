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
    private static final Pattern SQLPLUS_SET = Pattern.compile("SET\\s+(DEFINE|ECHO|PAUSE|TIMING|SERVEROUTPUT)\\s+(ON|OFF).*\\n", Pattern.CASE_INSENSITIVE);
    private static final Pattern SQLPLUS_SPOOL_OR_PROMPT = Pattern.compile("(SPOOL|PROMPT)\\s.*\\n", Pattern.CASE_INSENSITIVE);
    private static final Pattern SQLPLUS_EXIT_OR_COMMIT = Pattern.compile("(EXIT|COMMIT)(\\s|\\s*;)", Pattern.CASE_INSENSITIVE);
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
                statement = SQLPLUS_SET.matcher(statement).replaceAll("");
                statement = SQLPLUS_SPOOL_OR_PROMPT.matcher(statement).replaceAll("");
                statement = SQLPLUS_EXIT_OR_COMMIT.matcher(statement).replaceAll("");
                boolean dbmsOutput = statement.contains("dbms_output");
                try {
                    super.runStatements(new StringReader(statement), out);
                } finally {
                    if (dbmsOutput) {
                        printDbmsOutputResults(conn);
                    }
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
        StringBuilder sb = new StringBuilder();
        while (hasMore) {
            stmt.execute();
            hasMore = (stmt.getInt(2) == 0);
            if (hasMore) {
                sb.append(stmt.getString(1));
            }
        }
        System.err.println(sb.toString());
        stmt.close();
    }
}
