package yo.dbunitcli.fileprocessor;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.taskdefs.SQLExec;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SqlRunner implements Runner {
    private static final Pattern SQLPLUS_SET = Pattern.compile("SET\\s+(DEFINE|ECHO|PAUSE|TIMING|SERVEROUTPUT)\\s+(ON|OFF).*\\n", Pattern.CASE_INSENSITIVE);
    private static final Pattern SQLPLUS_SPOOL_OR_PROMPT = Pattern.compile("(SPOOL|PROMPT)\\s.*\\n", Pattern.CASE_INSENSITIVE);
    private static final Pattern SQLPLUS_EXIT_OR_COMMIT = Pattern.compile("(EXIT|COMMIT)(\\s|\\s*;)", Pattern.CASE_INSENSITIVE);
    private final DatabaseConnectionLoader connectionLoader;
    private final Map<String, Object> parameter;
    private final TemplateRender templateRender;

    public SqlRunner(final DatabaseConnectionLoader connectionLoader
            , final Map<String, Object> parameter
            , final TemplateRender templateRender) {
        this.connectionLoader = connectionLoader;
        this.parameter = parameter;
        this.templateRender = templateRender;
    }

    public Map<String, Object> getParameter() {
        return this.parameter;
    }

    public TemplateRender getTemplateRender() {
        return this.templateRender;
    }

    @Override
    public void runScript(final Stream<File> targetFiles) {
        targetFiles.forEach(target -> {
            try {
                final Connection conn = this.getConnection();
                final SQLExec exec = this.createExecInstance(conn);
                exec.setSrc(target);
                if (target.getName().endsWith("plsql")) {
                    final SQLExec.DelimiterType delimiterType = new SQLExec.DelimiterType();
                    delimiterType.setValue("row");
                    exec.setDelimiterType(delimiterType);
                    exec.setDelimiter("/");
                    exec.setKeepformat(true);
                }
                exec.execute();
            } catch (final Throwable var30) {
                LOGGER.error("error " + target.getName(), var30);
                throw new AssertionError(var30);
            }
        });
    }

    private SQLExec createExecInstance(final Connection conn) {
        final SQLExec exec = new SQLExec() {
            @Override
            protected Connection getConnection() {
                return conn;
            }

            @Override
            protected void runStatements(final Reader reader, final PrintStream out) throws SQLException, IOException {
                String statement = SqlRunner.this.getTemplateRender().render(IOUtils.toString(reader), SqlRunner.this.getParameter());
                statement = SQLPLUS_SET.matcher(statement).replaceAll("");
                statement = SQLPLUS_SPOOL_OR_PROMPT.matcher(statement).replaceAll("");
                statement = SQLPLUS_EXIT_OR_COMMIT.matcher(statement).replaceAll("");
                final boolean dbmsOutput = statement.contains("dbms_output");
                try {
                    super.runStatements(new StringReader(statement), out);
                } finally {
                    if (dbmsOutput) {
                        SqlRunner.this.printDbmsOutputResults(conn);
                    }
                }
            }
        };
        exec.setExpandProperties(false);
        exec.setEncoding(this.getTemplateRender().getEncoding());
        return exec;
    }

    private Connection getConnection() throws SQLException {
        final Connection conn = this.connectionLoader.loadConnection().getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    private void printDbmsOutputResults(final Connection conn) throws java.sql.SQLException {
        final String getLineSql = "begin dbms_output.get_line(?,?); end;";
        final CallableStatement stmt = conn.prepareCall(getLineSql);
        boolean hasMore = true;
        stmt.registerOutParameter(1, Types.VARCHAR);
        stmt.registerOutParameter(2, Types.INTEGER);
        final StringBuilder sb = new StringBuilder();
        while (hasMore) {
            stmt.execute();
            hasMore = (stmt.getInt(2) == 0);
            if (hasMore) {
                sb.append(stmt.getString(1));
            }
        }
        System.err.println(sb);
        stmt.close();
    }
}
