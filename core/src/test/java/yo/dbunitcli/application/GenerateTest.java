package yo.dbunitcli.application;

import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

public class GenerateTest {
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

    @Test
    public void testGenerateXlsx() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateXlsx.txt"});
        Compare.main(new String[]{"@" + this.testResourcesDir + "/generate/table/expect/xlsx/compareResult.txt"
                , "-old=" + this.testResourcesDir + "/generate/table/expect/xlsx/Test1"
                , "-new=" + this.baseDir + "/generate/table/result/xlsx/Test1.xlsx"
                , "-result=" + this.baseDir + "/generate/table/result/xlsx/Test1"
        });
    }

    @Test
    public void testGenerateTxt() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateTxt.txt"});
        this.subDirectory = "generate/row";
        assertGenerateFileEquals("SomeClassTest.txt", "MS932");
        assertGenerateFileEquals("OtherClassTest.txt", "MS932");
        assertGenerateFileEquals("AnotherClassTest.txt", "MS932");
    }

    @Test
    public void testGenerateTxtPerTable() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateTxtPerTable.txt"});
        this.subDirectory = "generate/table";
        assertGenerateFileEquals("Test1.txt", "UTF-8");
        assertGenerateFileEquals("Test2.txt", "UTF-8");
        assertGenerateFileEquals("Test3.txt", "UTF-8");
    }

    @Test
    public void testGenerateTxtWithMetaData() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateTxtWithMetaData.txt"});
        this.subDirectory = "generate/with_metadata";
        assertGenerateFileEquals("Test1.txt", "UTF-8");
        assertGenerateFileEquals("Test2.txt", "UTF-8");
        assertGenerateFileEquals("Test3.txt", "UTF-8");
    }

    @Test
    public void testGenerateSettings() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateSettings.txt"});
        this.subDirectory = "generate/settings";
        assertGenerateFileEquals("settings.json", "UTF-8");
    }

    @Test
    public void testGenerateInsert() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateInsert.txt"});
        this.subDirectory = "generate/sql/insert";
        assertGenerateFileEquals("Test1.sql", "UTF-8");
        assertGenerateFileEquals("Test2.sql", "UTF-8");
        assertGenerateFileEquals("Test3.sql", "UTF-8");
    }

    @Test
    public void testGenerateInsertNoCommit() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateInsertNoCommit.txt"});
        this.subDirectory = "generate/sql/no-commit";
        assertGenerateFileEquals("Insert_Test1_NoCommit.sql", "UTF-8");
        assertGenerateFileEquals("Insert_Test2_NoCommit.sql", "UTF-8");
        assertGenerateFileEquals("Insert_Test3_NoCommit.sql", "UTF-8");
    }

    @Test
    public void testGenerateDelete() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateDelete.txt"});
        this.subDirectory = "generate/sql/delete";
        assertGenerateFileEquals("Test1.sql", "UTF-8");
        assertGenerateFileEquals("Test2.sql", "UTF-8");
        assertGenerateFileEquals("Test3.sql", "UTF-8");
    }

    @Test
    public void testGenerateDeleteNoCommit() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateDeleteNoCommit.txt"});
        this.subDirectory = "generate/sql/no-commit";
        assertGenerateFileEquals("Delete_Test1_NoCommit.sql", "UTF-8");
        assertGenerateFileEquals("Delete_Test2_NoCommit.sql", "UTF-8");
        assertGenerateFileEquals("Delete_Test3_NoCommit.sql", "UTF-8");
    }

    @Test
    public void testGenerateCleanInsert() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateCleanInsert.txt"});
        this.subDirectory = "generate/sql/clean-insert";
        assertGenerateFileEquals("Test1.sql", "UTF-8");
        assertGenerateFileEquals("Test2.sql", "UTF-8");
        assertGenerateFileEquals("Test3.sql", "UTF-8");
    }

    @Test
    public void testGenerateRefresh() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateRefresh.txt"});
        this.subDirectory = "generate/sql/refresh";
        assertGenerateFileEquals("Test1.sql", "UTF-8");
        assertGenerateFileEquals("Test2.sql", "UTF-8");
        assertGenerateFileEquals("Test3.sql", "UTF-8");
    }

    @Test
    public void testGenerateUpdate() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateUpdate.txt"});
        this.subDirectory = "generate/sql/update";
        assertGenerateFileEquals("Test1.sql", "UTF-8");
        assertGenerateFileEquals("Test2.sql", "UTF-8");
        assertGenerateFileEquals("Test3.sql", "UTF-8");
    }

    private void assertGenerateFileEquals(String target, String encode) throws IOException {
        String expect = Files.asCharSource(new File(this.testResourcesDir + this.subDirectory + "/expect/txt", target), Charset.forName(encode)).read();
        String actual = Files.asCharSource(new File(this.baseDir + this.subDirectory + "/result", target), Charset.forName(encode)).read();
        Assert.assertEquals(expect, actual);
    }
}
