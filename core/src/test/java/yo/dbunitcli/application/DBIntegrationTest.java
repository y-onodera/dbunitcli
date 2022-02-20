package yo.dbunitcli.application;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class DBIntegrationTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();

    private String testResourcesDir;

    private String baseDir;

    private String subDirectory;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(this.getClass().getResource(".").getPath(), "UTF-8");
        this.testResourcesDir = this.baseDir.replace("target/test-classes", "src/test/resources");
    }

    public void runSql() throws Exception {
        Run.main(new String[]{"@" + this.testResourcesDir + "/paramRunSql.txt"});
    }

    public void exportFromDB() throws Exception {
        Convert.main(new String[]{"@" + this.testResourcesDir + "/paramExportFromDB.txt"});
    }

    public void importToDB() throws Exception {
        Convert.main(new String[]{"@" + this.testResourcesDir + "/paramImportToDB.txt"});
    }

}
