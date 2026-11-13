package yo.dbunitcli.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

class SourceTest {

    @Test
    void getTableNameはテーブル名が無ければファイル名から拡張子を除く() {
        Assertions.assertEquals("users", new Source(new File("data/users.csv"), false).getTableName());
    }

    @Test
    void getTableNameは拡張子の無いファイル名をそのまま返す() {
        Assertions.assertEquals("users", new Source(new File("data/users"), false).getTableName());
    }

    @Test
    void getTableNameはファイルを持たない読込元なら空を返す() {
        Assertions.assertEquals("", Source.JOIN.getTableName());
    }
}
