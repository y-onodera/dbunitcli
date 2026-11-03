package yo.dbunitcli.dataset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.producer.ComparableCSVQueryDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableQueryDataSetProducer;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class InlineQueryTest {

    private static final String CSV = new File("src/test/resources/yo/dbunitcli/dataset/producer/csvquery/csv/header.csv")
            .getAbsolutePath().replace('\\', '/');

    @Test
    public void csvqはsrcのファイルの代わりにinlineQueryをテーブル名のテーブルとして読む() {
        final ComparableDataSet actual = new ComparableCSVQueryDataSetProducer(this.param(DataSourceType.csvq)
                .setInlineQuery(new InlineQuery("inline", "SELECT KEYCOLUMN FROM CSVREAD('" + CSV
                        + "',null,'charset=UTF-8') WHERE KEYCOLUMN = '$param.key$'"))
                .build(), Parameter.none().addAll(Map.of("key", "1"))).loadDataSet();

        Assertions.assertArrayEquals(new String[]{"inline"}, actual.getTableNames());
        final ComparableTable table = actual.getTable("inline");
        Assertions.assertEquals(1, table.getRowCount());
        Assertions.assertEquals("1", table.getValue(0, "KEYCOLUMN"));
    }

    @Test
    public void csvqのinlineQueryもテーブル名で絞り込む() {
        final ComparableDataSet actual = new ComparableCSVQueryDataSetProducer(this.param(DataSourceType.csvq)
                .setRegTableExclude("^inline$")
                .setInlineQuery(new InlineQuery("inline", "SELECT * FROM CSVREAD('" + CSV + "',null,'charset=UTF-8')"))
                .build(), Parameter.none()).loadDataSet();

        Assertions.assertEquals(0, actual.getTableNames().length);
    }

    @Test
    public void sqlはsrcのファイルの代わりにinlineQueryをテーブル名のテーブルとして読む() throws SQLException {
        final String url = "jdbc:h2:mem:inlineQuery;DB_CLOSE_DELAY=-1";
        try (final Connection conn = DriverManager.getConnection(url, "sa", "");
             final Statement stmt = conn.createStatement()) {
            stmt.execute("create table t1 (ID varchar(10) primary key, VAL varchar(10))");
            stmt.execute("insert into t1 values ('a', '1'), ('b', '2')");
            final ComparableDataSet actual = new ComparableQueryDataSetProducer(this.param(DataSourceType.sql)
                    .setDatabaseConnectionLoader(this.connectionLoader(url))
                    .setInlineQuery(new InlineQuery("inline", "select VAL from t1 where ID = '$param.id$';"))
                    .build(), Parameter.none().addAll(Map.of("id", "b"))).loadDataSet();

            Assertions.assertArrayEquals(new String[]{"inline"}, actual.getTableNames());
            final ComparableTable table = actual.getTable("inline");
            Assertions.assertEquals(List.of("VAL"), List.of(table.getTableMetaData().getColumns()).stream()
                                                            .map(it -> it.getColumnName().toUpperCase()).toList());
            Assertions.assertEquals(1, table.getRowCount());
            Assertions.assertEquals("2", table.getValue(0, "VAL"));
        }
    }

    // srcはinlineQueryがあれば読まない。存在しないパスにして読まないことも確かめる
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
