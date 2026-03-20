package yo.dbunitcli.application.command;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Tag("IntegrationTest")
@DisabledInNativeImage
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class H2IntegrationTest {

    private static String testResourcesDir;

    @BeforeAll
    public static void setUp() {
        final String baseDir = URLDecoder.decode(Objects.requireNonNull(H2IntegrationTest.class.getResource(".")).getPath(), StandardCharsets.UTF_8);
        H2IntegrationTest.testResourcesDir = baseDir.replace("target/test-classes", "src/test/resources");
        Run.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationRunCreateTable.txt"});
        Compare.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationCompare.txt", "-old.loadData=false"});
    }

    @AfterAll
    public static void tearDown() {
        Run.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationRunDropTable.txt"});
    }

    @Test
    @Order(1)
    public void cleanInsertToDB() {
        Convert.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationCleanInsert.txt"});
        Compare.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationCompare.txt"});
    }

    @Test
    @Order(2)
    public void deleteToDB() {
        Convert.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationDelete.txt"});
        Compare.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationCompare.txt", "-old.loadData=false"});
    }

    @Test
    @Order(3)
    public void insertToDB() {
        Convert.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationInsert.txt"});
        Compare.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationCompare.txt"});
    }

    @Test
    @Order(4)
    public void updateToDB() {
        Convert.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationUpdate.txt"});
    }

    @Test
    @Order(5)
    public void refreshToDB() {
        Convert.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationRefresh.txt"});
    }

    @Test
    @Order(6)
    public void exportFromDB() {
        Convert.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationExport.txt"});
    }

    @Test
    @Order(6)
    public void exportFromDBChangeHeaderName() {
        Convert.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationExportChangeHeaderName.txt"});
    }

    @Test
    @Order(6)
    public void exportQuery() {
        Convert.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationQueryExport.txt"});
    }

    @Test
    @Order(6)
    public void exportQueryChangeHeaderName() {
        Convert.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationQueryExportChangeHeaderName.txt"});
    }

    @Test
    @Order(7)
    public void runSql() {
        Run.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationRunDml.txt"});
    }

    @Test
    @Order(8)
    public void generateSetting() {
        Generate.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationGenerateSetting.txt"});
    }

    @Test
    @Order(9)
    public void generateSql() {
        Generate.main(new String[]{"@" + H2IntegrationTest.testResourcesDir + "/paramH2IntegrationGenerateSql.txt"});
    }

}
