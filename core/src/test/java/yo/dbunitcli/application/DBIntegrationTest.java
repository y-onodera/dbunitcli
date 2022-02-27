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
    public void test1_runSql() throws Exception {
        Run.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationRunDdl.txt"});
    }

    @Test
    public void test2_importToDB() throws Exception {
        Convert.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationImport.txt"});
    }

    @Test
    public void test3_exportFromDB() throws Exception {
        Convert.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationExport.txt"});
    }

    @Test
    public void test4_runSql() throws Exception {
        Run.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationRunDml.txt"});
    }

    @Test
    public void test5_generateSetting() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationGenerateSetting.txt"});
    }

    @Test
    public void test6_generateSql() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramDBIntegrationGenerateSql.txt"});
    }

}
