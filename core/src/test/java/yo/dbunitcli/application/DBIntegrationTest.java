package yo.dbunitcli.application;

import org.junit.jupiter.api.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Tag("IntegrationTest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DBIntegrationTest {

    private static String testResourcesDir;

    @BeforeAll
    public static void setUp() {
        final String baseDir = URLDecoder.decode(Objects.requireNonNull(DBIntegrationTest.class.getResource(".")).getPath(), StandardCharsets.UTF_8);
        DBIntegrationTest.testResourcesDir = baseDir.replace("target/test-classes", "src/test/resources");
        Run.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationRunCreateTable.txt"});
        Compare.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationCompare.txt", "-old.loadData=false"});
    }

    @AfterAll
    public static void tearDown() {
        Run.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationRunDropTable.txt"});
    }

    @Test
    @Order(1)
    public void cleanInsertToDB() {
        Convert.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationCleanInsert.txt"});
        Compare.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationCompare.txt"});
    }

    @Test
    @Order(2)
    public void deleteToDB() {
        Convert.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationDelete.txt"});
        Compare.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationCompare.txt", "-old.loadData=false"});
    }

    @Test
    @Order(3)
    public void insertToDB() {
        Convert.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationInsert.txt"});
        Compare.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationCompare.txt"});
    }

    @Test
    @Order(4)
    public void updateToDB() {
        Convert.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationUpdate.txt"});
    }

    @Test
    @Order(5)
    public void refreshToDB() {
        Convert.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationRefresh.txt"});
    }

    @Test
    @Order(6)
    public void exportFromDB() {
        Convert.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationExport.txt"});
    }

    @Test
    @Order(6)
    public void exportFromDBChangeHeaderName() {
        Convert.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationExportChangeHeaderName.txt"});
    }

    @Test
    @Order(6)
    public void exportQuery() {
        Convert.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationQueryExport.txt"});
    }

    @Test
    @Order(6)
    public void exportQueryChangeHeaderName() {
        Convert.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationQueryExportChangeHeaderName.txt"});
    }

    @Test
    @Order(7)
    public void runSql() {
        Run.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationRunDml.txt"});
    }

    @Test
    @Order(8)
    public void generateSetting() {
        Generate.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationGenerateSetting.txt"});
    }

    @Test
    @Order(9)
    public void generateSql() {
        Generate.main(new String[]{"@" + DBIntegrationTest.testResourcesDir + "/paramDBIntegrationGenerateSql.txt"});
    }

}
