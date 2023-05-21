package yo.dbunitcli.application;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.OrderWith;
import org.junit.runner.manipulation.Alphanumeric;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Category(IntegrationTest.class)
@OrderWith(Alphanumeric.class)
public class DBIntegrationTest {

    private static String testResourcesDir;

    @BeforeClass
    public static void setUp() throws Exception {
        final String baseDir = URLDecoder.decode(Objects.requireNonNull(DBIntegrationTest.class.getResource(".")).getPath(), StandardCharsets.UTF_8);
        testResourcesDir = baseDir.replace("target/test-classes", "src/test/resources");
        Run.main(new String[]{"@" + testResourcesDir + "/paramDBIntegrationRunCreateTable.txt"});
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Run.main(new String[]{"@" + testResourcesDir + "/paramDBIntegrationRunDropTable.txt"});
    }

    @Test
    public void test02_cleanInsertToDB() throws Exception {
        Convert.main(new String[]{"@" + testResourcesDir + "/paramDBIntegrationCleanInsert.txt"});
    }

    @Test
    public void test03_deleteToDB() throws Exception {
        Convert.main(new String[]{"@" + testResourcesDir + "/paramDBIntegrationDelete.txt"});
    }

    @Test
    public void test04_insertToDB() throws Exception {
        Convert.main(new String[]{"@" + testResourcesDir + "/paramDBIntegrationInsert.txt"});
    }

    @Test
    public void test05_updateToDB() throws Exception {
        Convert.main(new String[]{"@" + testResourcesDir + "/paramDBIntegrationUpdate.txt"});
    }

    @Test
    public void test06_refreshToDB() throws Exception {
        Convert.main(new String[]{"@" + testResourcesDir + "/paramDBIntegrationRefresh.txt"});
    }

    @Test
    public void test07_exportFromDB() throws Exception {
        Convert.main(new String[]{"@" + testResourcesDir + "/paramDBIntegrationExport.txt"});
    }

    @Test
    public void test08_runSql() throws Exception {
        Run.main(new String[]{"@" + testResourcesDir + "/paramDBIntegrationRunDml.txt"});
    }

    @Test
    public void test09_generateSetting() throws Exception {
        Generate.main(new String[]{"@" + testResourcesDir + "/paramDBIntegrationGenerateSetting.txt"});
    }

    @Test
    public void test10_generateSql() throws Exception {
        Generate.main(new String[]{"@" + testResourcesDir + "/paramDBIntegrationGenerateSql.txt"});
    }

}
