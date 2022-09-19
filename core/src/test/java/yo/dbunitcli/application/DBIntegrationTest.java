package yo.dbunitcli.application;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.runner.OrderWith;
import org.junit.runner.manipulation.Alphanumeric;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Category(IntegrationTest.class)
@OrderWith(Alphanumeric.class)
public class DBIntegrationTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();

    private String testResourcesDir;

    private String baseDir;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(this.getClass().getResource(".").getPath(), "UTF-8");
        this.testResourcesDir = this.baseDir.replace("target/test-classes", "src/test/resources");
    }

    @Test
    public void test01_runSql() throws Exception {
        Run.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationRunDdl.txt"});
    }

    @Test
    public void test02_cleanInsertToDB() throws Exception {
        Convert.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationCleanInsert.txt"});
    }

    @Test
    public void test03_deleteToDB() throws Exception {
        Convert.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationDelete.txt"});
    }

    @Test
    public void test04_insertToDB() throws Exception {
        Convert.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationInsert.txt"});
    }

    @Test
    public void test05_updateToDB() throws Exception {
        Convert.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationUpdate.txt"});
    }

    @Test
    public void test06_refreshToDB() throws Exception {
        Convert.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationRefresh.txt"});
    }

    @Test
    public void test07_exportFromDB() throws Exception {
        Convert.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationExport.txt"});
    }

    @Test
    public void test08_runSql() throws Exception {
        Run.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationRunDml.txt"});
    }

    @Test
    public void test09_generateSetting() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationGenerateSetting.txt"});
    }

    @Test
    public void test10_generateSql() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationGenerateSql.txt"});
    }

}
