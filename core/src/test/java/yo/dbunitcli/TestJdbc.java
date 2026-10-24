package yo.dbunitcli;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * テスト用のJDBC操作
 */
public final class TestJdbc {

    private TestJdbc() {
    }

    public static int count(final Statement stmt, final String from) throws SQLException {
        try (final ResultSet rs = stmt.executeQuery("select count(*) from " + from)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * H2で開いている接続（セッション）の数
     */
    public static int sessions(final Statement stmt) throws SQLException {
        return TestJdbc.count(stmt, "information_schema.sessions");
    }
}
