package yo.dbunitcli.dataset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.producer.ComparableCSVQueryDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableDBDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableQueryDataSetProducer;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class InlineSourceTest {

    private static final String CSV = new File("src/test/resources/yo/dbunitcli/dataset/producer/csvquery/csv/header.csv")
            .getAbsolutePath().replace('\\', '/');

    @Test
    public void csvqはsrcのファイルの代わりにinlineSourceをテーブル名のテーブルとして読む() {
        final ComparableDataSet actual = new ComparableCSVQueryDataSetProducer(this.param(DataSourceType.csvq)
                .setInlineSource(new InlineSource("inline", "SELECT KEYCOLUMN FROM CSVREAD('" + CSV
                        + "',null,'charset=UTF-8') WHERE KEYCOLUMN = '$param.key$'"))
                .build(), Parameter.none().addAll(Map.of("key", "1"))).loadDataSet();

        Assertions.assertArrayEquals(new String[]{"inline"}, actual.getTableNames());
        final ComparableTable table = actual.getTable("inline");
        Assertions.assertEquals(1, table.getRowCount());
        Assertions.assertEquals("1", table.getValue(0, "KEYCOLUMN"));
    }

    @Test
    public void csvqのinlineSourceもテーブル名で絞り込む() {
        final ComparableDataSet actual = new ComparableCSVQueryDataSetProducer(this.param(DataSourceType.csvq)
                .setRegTableExclude("^inline$")
                .setInlineSource(new InlineSource("inline", "SELECT * FROM CSVREAD('" + CSV + "',null,'charset=UTF-8')"))
                .build(), Parameter.none()).loadDataSet();

        Assertions.assertEquals(0, actual.getTableNames().length);
    }

    @Test
    public void sqlはsrcのファイルの代わりにinlineSourceをテーブル名のテーブルとして読む() throws SQLException {
        final String url = "jdbc:h2:mem:inlineSource;DB_CLOSE_DELAY=-1";
        try (final Connection conn = DriverManager.getConnection(url, "sa", "");
             final Statement stmt = conn.createStatement()) {
            stmt.execute("create table t1 (ID varchar(10) primary key, VAL varchar(10))");
            stmt.execute("insert into t1 values ('a', '1'), ('b', '2')");
            final ComparableDataSet actual = new ComparableQueryDataSetProducer(this.param(DataSourceType.sql)
                    .setDatabaseConnectionLoader(this.connectionLoader(url))
                    .setInlineSource(new InlineSource("inline", "select VAL from t1 where ID = '$param.id$';"))
                    .build(), Parameter.none().addAll(Map.of("id", "b"))).loadDataSet();

            Assertions.assertArrayEquals(new String[]{"inline"}, actual.getTableNames());
            final ComparableTable table = actual.getTable("inline");
            Assertions.assertEquals(List.of("VAL"), List.of(table.getTableMetaData().getColumns()).stream()
                                                            .map(it -> it.getColumnName().toUpperCase()).toList());
            Assertions.assertEquals(1, table.getRowCount());
            Assertions.assertEquals("2", table.getValue(0, "VAL"));
        }
    }

    @Test
    public void tableはsrcのファイルの代わりにinlineSourceの各行をテーブル名として読み空行は飛ばす() throws SQLException {
        final String url = "jdbc:h2:mem:inlineTable;DB_CLOSE_DELAY=-1";
        try (final Connection conn = DriverManager.getConnection(url, "sa", "");
             final Statement stmt = conn.createStatement()) {
            stmt.execute("create table T1 (ID varchar(10) primary key)");
            stmt.execute("create table T2 (ID varchar(10) primary key)");
            stmt.execute("insert into T1 values ('a'), ('b')");
            stmt.execute("insert into T2 values ('c')");
            final ComparableDataSet actual = new ComparableDBDataSetProducer(this.param(DataSourceType.table)
                    .setDatabaseConnectionLoader(this.connectionLoader(url))
                    .setInlineSource(new InlineSource("inline", "T1\n\nT2\n"))
                    .build()).loadDataSet();

            Assertions.assertArrayEquals(new String[]{"T1", "T2"}, actual.getTableNames());
            Assertions.assertEquals(2, actual.getTable("T1").getRowCount());
            Assertions.assertEquals(1, actual.getTable("T2").getRowCount());
        }
    }

    @Test
    public void inlineSourceを読めない形式ではbuildできない() {
        Assertions.assertEquals(List.of(DataSourceType.table, DataSourceType.sql, DataSourceType.csvq),
                Arrays.stream(DataSourceType.values()).filter(DataSourceType::supportsInlineSource).toList());
        final ComparableDataSetParam.Builder builder = this.param(DataSourceType.csv)
                .setInlineSource(new InlineSource("inline", "SELECT 1"));
        Assertions.assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void inlineSourceを読んだデータセットのsrcは空() {
        final ComparableDataSet actual = new ComparableCSVQueryDataSetProducer(this.param(DataSourceType.csvq)
                .setInlineSource(new InlineSource("inline", "SELECT 1 AS ONE"))
                .build(), Parameter.none()).loadDataSet();

        Assertions.assertEquals("", actual.src());
        Assertions.assertArrayEquals(new String[]{"inline"}, actual.getTableNames());
    }

    // srcはinlineSourceがあれば読まない。存在しないパスにして読まないことも確かめる
    private ComparableDataSetParam.Builder param(final DataSourceType type) {
        return ComparableDataSetParam.builder()
                                     .setSrc(new File("notExists"))
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
}
