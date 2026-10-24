package yo.dbunitcli.application.command;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import yo.dbunitcli.TestJdbc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DBを読み書きするコマンドが、張った接続を処理の終了時に閉じること
 */
public class DbConnectionCloseTest {

    private static final String URL = "jdbc:h2:mem:dbConnectionClose;DB_CLOSE_DELAY=-1";

    private static final List<String> TABLES = List.of("T1", "T2");

    @TempDir
    Path tempDir;

    private Connection admin;

    private Statement stmt;

    private File tableList;

    @BeforeEach
    public void setUp() throws SQLException, IOException {
        this.admin = DriverManager.getConnection(DbConnectionCloseTest.URL, "sa", "sa");
        this.stmt = this.admin.createStatement();
        for (final String table : DbConnectionCloseTest.TABLES) {
            this.stmt.execute("drop table if exists " + table);
            this.stmt.execute("create table " + table + " (ID varchar(10) primary key, VAL varchar(10))");
            this.stmt.execute("insert into " + table + " select '" + table + "-' || x, x from system_range(1, 10)");
        }
        this.tableList = this.tempDir.resolve("tables.txt").toFile();
        Files.writeString(this.tableList.toPath(), String.join("\n", DbConnectionCloseTest.TABLES), StandardCharsets.UTF_8);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        this.stmt.close();
        this.admin.close();
    }

    @ParameterizedTest
    @CsvSource({"record,$rowNumber$.txt,", "dataset,all.txt,", "table,$tableName$.txt,", "table,$tableName$.txt,1"})
    public void generateTxtClosesSourceConnection(final String unit, final String resultPath, final String parallel) throws Exception {
        final File template = this.tempDir.resolve("template.txt").toFile();
        Files.writeString(template.toPath(), "x", StandardCharsets.UTF_8);
        this.assertNoConnectionLeft(() -> Generate.main(this.dbSrcArgs(
                "-generateType=txt"
                , "-unit=" + unit
                , "-parallel=" + (parallel == null ? "" : parallel)
                , "-template=" + template.getAbsolutePath()
                , "-templateParameterAttribute="
                , "-result=" + this.tempDir.resolve("txt-" + unit).toFile().getAbsolutePath()
                , "-resultPath=" + resultPath)));
    }

    @ParameterizedTest
    @CsvSource({"table,true", "table,false", "dataset,true", "dataset,false"})
    public void generateXlsxClosesSourceConnection(final String unit, final boolean lazyLoad) throws Exception {
        final File template = TestJxlsTemplates.create(this.tempDir, "xlsx");
        this.assertNoConnectionLeft(() -> Generate.main(this.dbSrcArgs(
                "-generateType=xlsx"
                , "-unit=" + unit
                , "-lazyLoad=" + lazyLoad
                , "-template=" + template.getAbsolutePath()
                , "-templateParameterAttribute="
                , "-result=" + this.tempDir.resolve("xlsx-" + unit + lazyLoad).toFile().getAbsolutePath()
                , "-resultPath=" + ("table".equals(unit) ? "$tableName$.xlsx" : "all.xlsx"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sql", "ddl", "javaBean"})
    public void generateFixedTypeClosesSourceConnection(final String generateType) throws Exception {
        this.assertNoConnectionLeft(() -> Generate.main(this.dbSrcArgs(
                "-generateType=" + generateType
                , "-op=INSERT"
                , "-result=" + this.tempDir.resolve(generateType).toFile().getAbsolutePath()
                , "-resultPath=out")));
    }

    @Test
    public void convertClosesSourceAndResultConnection() throws Exception {
        final File csv = this.tempDir.resolve("csv").toFile();
        // DB→csv（読込側の接続）
        this.assertNoConnectionLeft(() -> Convert.main(this.dbSrcArgs(
                "-resultType=csv"
                , "-result=" + csv.getAbsolutePath())));
        Assertions.assertTrue(new File(csv, "T1.csv").exists());
        // csv→DB（出力側の接続）。並列時の作業スレッドの接続も残らない
        for (final String parallel : List.of("", "1")) {
            this.assertNoConnectionLeft(() -> Convert.main(new String[]{
                    "-srcType=csv"
                    , "-src=" + csv.getAbsolutePath()
                    , "-encoding=UTF-8"
                    , "-resultType=table"
                    , "-op=CLEAN_INSERT"
                    , "-parallel=" + parallel
                    , "-jdbcUrl=" + DbConnectionCloseTest.URL
                    , "-jdbcUser=sa"
                    , "-jdbcPass=sa"}));
        }
        Assertions.assertEquals(10, TestJdbc.count(this.stmt, "T2"));
    }

    @Test
    public void compareClosesConnection() throws Exception {
        final File csv = this.tempDir.resolve("csv").toFile();
        Convert.main(this.dbSrcArgs("-resultType=csv", "-result=" + csv.getAbsolutePath()));
        this.assertNoConnectionLeft(() -> Compare.main(new String[]{
                "-old.srcType=csv"
                , "-old.src=" + csv.getAbsolutePath()
                , "-old.encoding=UTF-8"
                , "-new.srcType=table"
                , "-new.src=" + this.tableList.getAbsolutePath()
                , "-jdbcUrl=" + DbConnectionCloseTest.URL
                , "-jdbcUser=sa"
                , "-jdbcPass=sa"
                , "-resultType=csv"
                , "-result=" + this.tempDir.resolve("compare").toFile().getAbsolutePath()}));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "2"})
    public void runSqlClosesConnection(final String parallel) throws Exception {
        final File sqlDir = this.tempDir.resolve("sql").toFile();
        Assertions.assertTrue(sqlDir.mkdirs());
        for (final String table : DbConnectionCloseTest.TABLES) {
            Files.writeString(new File(sqlDir, table + ".sql").toPath()
                    , "update " + table + " set VAL = 'run';", StandardCharsets.UTF_8);
        }
        this.assertNoConnectionLeft(() -> Run.main(new String[]{
                "-scriptType=sql"
                , "-src=" + sqlDir.getAbsolutePath()
                , "-parallel=" + parallel
                , "-jdbcUrl=" + DbConnectionCloseTest.URL
                , "-jdbcUser=sa"
                , "-jdbcPass=sa"}));
        for (final String table : DbConnectionCloseTest.TABLES) {
            Assertions.assertEquals(10, TestJdbc.count(this.stmt, table + " where VAL = 'run'"));
        }
    }

    private String[] dbSrcArgs(final String... args) {
        final List<String> result = new ArrayList<>(List.of(
                "-srcType=table"
                , "-src=" + this.tableList.getAbsolutePath()
                , "-jdbcUrl=" + DbConnectionCloseTest.URL
                , "-jdbcUser=sa"
                , "-jdbcPass=sa"));
        result.addAll(List.of(args));
        return result.toArray(new String[0]);
    }

    private void assertNoConnectionLeft(final ThrowingRunnable command) throws Exception {
        final int before = TestJdbc.sessions(this.stmt);
        command.run();
        Assertions.assertEquals(before, TestJdbc.sessions(this.stmt));
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
