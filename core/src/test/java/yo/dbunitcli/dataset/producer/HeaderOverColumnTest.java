package yo.dbunitcli.dataset.producer;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * ヘッダより多いデータ行の列は無視して読み込むこと
 */
public class HeaderOverColumnTest {

    @TempDir
    Path tempDir;

    @Test
    public void regexSplitIgnoresColumnsOverHeader() throws IOException, DataSetException {
        final File src = this.write("reg.txt", "ID,VAL\n1,a,x,y\n2,b\n");
        this.assertRows(new ComparableRegexSplitDataSetProducer(this.builder(src, DataSourceType.reg)
                        .setHeaderSplitPattern(",")
                        .setDataSplitPattern(",")
                        .build()).loadDataSet().getTable("reg")
                , List.of("ID", "VAL"), List.of("1", "a"), List.of("2", "b"));
    }

    @Test
    public void regexSplitIgnoresColumnsOverHeaderName() throws IOException, DataSetException {
        final File src = this.write("reg.txt", "1,a,x\n2,b,y\n");
        this.assertRows(new ComparableRegexSplitDataSetProducer(this.builder(src, DataSourceType.reg)
                        .setDataSplitPattern(",")
                        .setHeaderName("ID,VAL")
                        .build()).loadDataSet().getTable("reg")
                , List.of("ID", "VAL"), List.of("1", "a"), List.of("2", "b"));
    }

    @Test
    public void csvIgnoresColumnsOverHeaderName() throws IOException, DataSetException {
        final File src = this.write("csv.csv", "1,a,x\n2,b,y\n");
        this.assertRows(new ComparableCsvDataSetProducer(this.builder(src, DataSourceType.csv)
                        .setHeaderName("ID,VAL")
                        .build()).loadDataSet().getTable("csv")
                , List.of("ID", "VAL"), List.of("1", "a"), List.of("2", "b"));
    }

    @Test
    public void fixedIgnoresColumnsOverHeaderName() throws IOException, DataSetException {
        final File src = this.write("fixed.txt", "1ax\n2by\n");
        this.assertRows(new ComparableFixedFileDataSetProducer(this.builder(src, DataSourceType.fixed)
                        .setFixedLength("1,1,1")
                        .setHeaderName("ID,VAL")
                        .build()).loadDataSet().getTable("fixed")
                , List.of("ID", "VAL"), List.of("1", "a"), List.of("2", "b"));
    }

    @Test
    public void xlsIgnoresColumnsOverHeader() throws IOException, DataSetException {
        final File src = this.writeExcel(new HSSFWorkbook(), "book.xls");
        this.assertRows(new ComparableXlsDataSetProducer(this.builder(src, DataSourceType.xls)
                        .build()).loadDataSet().getTable("s1")
                , List.of("ID", "VAL"), List.of("1", "a"), List.of("2", "b"));
    }

    @Test
    public void xlsIgnoresColumnsOverHeaderName() throws IOException, DataSetException {
        final File src = this.writeExcel(new HSSFWorkbook(), "book.xls");
        this.assertRows(new ComparableXlsDataSetProducer(this.builder(src, DataSourceType.xls)
                        .setHeaderName("ID")
                        .build()).loadDataSet().getTable("s1")
                , List.of("ID"), List.of("ID"), List.of("1"), List.of("2"));
    }

    @Test
    public void xlsxIgnoresColumnsOverHeader() throws IOException, DataSetException {
        final File src = this.writeExcel(new XSSFWorkbook(), "book.xlsx");
        this.assertRows(new ComparableXlsxDataSetProducer(this.builder(src, DataSourceType.xlsx)
                        .build()).loadDataSet().getTable("s1")
                , List.of("ID", "VAL"), List.of("1", "a"), List.of("2", "b"));
    }

    @Test
    public void xlsxIgnoresColumnsOverHeaderName() throws IOException, DataSetException {
        final File src = this.writeExcel(new XSSFWorkbook(), "book.xlsx");
        this.assertRows(new ComparableXlsxDataSetProducer(this.builder(src, DataSourceType.xlsx)
                        .setHeaderName("ID")
                        .build()).loadDataSet().getTable("s1")
                , List.of("ID"), List.of("ID"), List.of("1"), List.of("2"));
    }

    @Test
    public void csvqIgnoresColumnsOverHeaderName() throws IOException, DataSetException {
        final File data = this.write("data.csv", "C1,C2,C3\n1,a,x\n2,b,y\n");
        final File queryDir = Files.createDirectories(this.tempDir.resolve("csvq")).toFile();
        Files.writeString(new File(queryDir, "q.sql").toPath()
                , "SELECT * FROM CSVREAD('" + data.getAbsolutePath().replace("\\", "/") + "', null, 'charset=UTF-8') ORDER BY C1"
                , StandardCharsets.UTF_8);
        this.assertRows(new ComparableCSVQueryDataSetProducer(this.builder(queryDir, DataSourceType.csvq)
                        .setHeaderName("ID,VAL")
                        .build(), Parameter.none()).loadDataSet().getTable("q")
                , List.of("ID", "VAL"), List.of("1", "a"), List.of("2", "b"));
    }

    @Test
    public void tableIgnoresColumnsOverHeaderName() throws IOException, SQLException, DataSetException {
        final String url = "jdbc:h2:mem:headerOverColumnTable;DB_CLOSE_DELAY=-1";
        this.createTable(url);
        final File listFile = this.write("tables.txt", "T1");
        final ComparableTable table = new ComparableDBDataSetProducer(this.builder(listFile, DataSourceType.table)
                .setDatabaseConnectionLoader(this.connectionLoader(url))
                .setHeaderName("ID,VAL")
                .build()).loadDataSet().getTable("T1");
        this.assertRows(table, List.of("ID", "VAL"), List.of("1", "a"), List.of("2", "b"));
    }

    @Test
    public void sqlIgnoresColumnsOverHeaderName() throws IOException, SQLException, DataSetException {
        final String url = "jdbc:h2:mem:headerOverColumnSql;DB_CLOSE_DELAY=-1";
        this.createTable(url);
        final File queryDir = Files.createDirectories(this.tempDir.resolve("sql")).toFile();
        Files.writeString(new File(queryDir, "q.sql").toPath(), "select * from T1 order by C1", StandardCharsets.UTF_8);
        this.assertRows(new ComparableQueryDataSetProducer(this.builder(queryDir, DataSourceType.sql)
                        .setDatabaseConnectionLoader(this.connectionLoader(url))
                        .setHeaderName("ID,VAL")
                        .build(), Parameter.none()).loadDataSet().getTable("q")
                , List.of("ID", "VAL"), List.of("1", "a"), List.of("2", "b"));
    }

    private void createTable(final String url) throws SQLException {
        try (final Connection conn = DriverManager.getConnection(url, "sa", "");
             final Statement stmt = conn.createStatement()) {
            stmt.execute("create table T1 (C1 varchar(10) primary key, C2 varchar(10), C3 varchar(10))");
            stmt.execute("insert into T1 values ('1', 'a', 'x'), ('2', 'b', 'y')");
        }
    }

    private ComparableDataSetParam.Builder builder(final File src, final DataSourceType type) {
        return ComparableDataSetParam.builder()
                .setSrc(src)
                .setSource(type)
                .setEncoding("UTF-8");
    }

    private DatabaseConnectionLoader connectionLoader(final String url) {
        final Properties prop = new Properties();
        prop.put("url", url);
        prop.put("user", "sa");
        prop.put("pass", "");
        return new DatabaseConnectionLoader(prop);
    }

    private File write(final String name, final String content) throws IOException {
        final File file = this.tempDir.resolve(name).toFile();
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        return file;
    }

    /**
     * シートs1に ID,VAL のヘッダ行と、ヘッダより列が多いデータ行を書き込む
     */
    private File writeExcel(final Workbook workbook, final String name) throws IOException {
        final File file = this.tempDir.resolve(name).toFile();
        try (workbook; final FileOutputStream out = new FileOutputStream(file)) {
            final Sheet sheet = workbook.createSheet("s1");
            this.writeRow(sheet.createRow(0), "ID", "VAL");
            this.writeRow(sheet.createRow(1), "1", "a", "x", "y");
            this.writeRow(sheet.createRow(2), "2", "b");
            workbook.write(out);
        }
        return file;
    }

    private void writeRow(final Row row, final String... values) {
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }

    private List<String> columnNames(final ComparableTable table) {
        return Arrays.stream(table.getTableMetaData().getColumns()).map(Column::getColumnName).toList();
    }

    @SafeVarargs
    private void assertRows(final ComparableTable table, final List<String> columns, final List<String>... rows) throws DataSetException {
        Assertions.assertEquals(columns, this.columnNames(table));
        final List<List<String>> actual = new ArrayList<>();
        for (int row = 0; row < table.getRowCount(); row++) {
            final List<String> values = new ArrayList<>();
            for (final String column : columns) {
                values.add(String.valueOf(table.getValue(row, column)));
            }
            actual.add(values);
        }
        Assertions.assertEquals(List.of(rows), actual);
    }
}
