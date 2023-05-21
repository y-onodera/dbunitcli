package yo.dbunitcli.application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class GenerateTest {
    private String testResourcesDir;

    private String baseDir;

    private String subDirectory;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8);
        this.testResourcesDir = this.baseDir.replace("target/test-classes", "src/test/resources");
    }

    @Test
    public void testGenerateXlsx() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateXlsx.txt"});
        Compare.main(new String[]{"@" + this.testResourcesDir + "/generate/table/expect/xlsx/compareResult.txt"
                , "-old.src=" + this.testResourcesDir + "/generate/table/expect/Test1"
                , "-new.src=" + this.baseDir + "/generate/table/result/xlsx/Test1.xlsx"
                , "-result=" + this.baseDir + "/generate/table/result/xlsx/Test1"
        });
    }

    @Test
    public void testGenerateXlsxStreaming() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateXlsxStreaming.txt"});
        Compare.main(new String[]{"@" + this.testResourcesDir + "/generate/table/expect/streamxlsx/compareResult.txt"
                , "-old.src=" + this.testResourcesDir + "/generate/table/expect/Test1"
                , "-new.src=" + this.baseDir + "/generate/table/result/xlsx/Test1_Stream.xlsx"
                , "-result=" + this.baseDir + "/generate/table/result/xlsx/Test1Stream"
        });
    }

    @Test
    public void testGenerateXls() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateXls.txt"});
        Compare.main(new String[]{"@" + this.testResourcesDir + "/generate/table/expect/xls/compareResult.txt"
                , "-old.src=" + this.testResourcesDir + "/generate/table/expect/Test1"
                , "-new.src=" + this.baseDir + "/generate/table/result/xls/Test1.xls"
                , "-result=" + this.baseDir + "/generate/table/result/xls/Test1"
        });
    }

    @Test
    public void testGenerateTxt() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateTxt.txt"});
        this.subDirectory = "generate/row";
        this.assertGenerateFileEquals("SomeClassTest.txt", "MS932");
        this.assertGenerateFileEquals("OtherClassTest.txt", "MS932");
        this.assertGenerateFileEquals("AnotherClassTest.txt", "MS932");
    }

    @Test
    public void testGenerateTxtPerTable() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateTxtPerTable.txt"});
        this.subDirectory = "generate/table";
        this.assertGenerateFileEquals("Test1.txt", "UTF-8");
        this.assertGenerateFileEquals("Test2.txt", "UTF-8");
        this.assertGenerateFileEquals("Test3.txt", "UTF-8");
    }

    @Test
    public void testGenerateTxtWithMetaData() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateTxtWithMetaData.txt"});
        this.subDirectory = "generate/with_metadata";
        this.assertGenerateFileEquals("Test1.txt", "UTF-8");
        this.assertGenerateFileEquals("Test2.txt", "UTF-8");
        this.assertGenerateFileEquals("Test3.txt", "UTF-8");
    }

    @Test
    public void testGenerateSettings() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateSettings.txt"});
        this.subDirectory = "generate/settings";
        this.assertGenerateFileEquals("settings.json", "UTF-8");
    }

    @Test
    public void testGenerateInsert() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateInsert.txt"});
        this.subDirectory = "generate/sql/insert";
        this.assertGenerateFileEquals("Test1.sql", "UTF-8");
        this.assertGenerateFileEquals("Test2.sql", "UTF-8");
        this.assertGenerateFileEquals("Test3.sql", "UTF-8");
    }

    @Test
    public void testGenerateInsertNoCommit() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateInsertNoCommit.txt"});
        this.subDirectory = "generate/sql/no-commit";
        this.assertGenerateFileEquals("Insert_Test1_NoCommit.sql", "UTF-8");
        this.assertGenerateFileEquals("Insert_Test2_NoCommit.sql", "UTF-8");
        this.assertGenerateFileEquals("Insert_Test3_NoCommit.sql", "UTF-8");
    }

    @Test
    public void testGenerateDelete() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateDelete.txt"});
        this.subDirectory = "generate/sql/delete";
        this.assertGenerateFileEquals("Test1.sql", "UTF-8");
        this.assertGenerateFileEquals("Test2.sql", "UTF-8");
        this.assertGenerateFileEquals("Test3.sql", "UTF-8");
    }

    @Test
    public void testGenerateDeleteNoCommit() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateDeleteNoCommit.txt"});
        this.subDirectory = "generate/sql/no-commit";
        this.assertGenerateFileEquals("Delete_Test1_NoCommit.sql", "UTF-8");
        this.assertGenerateFileEquals("Delete_Test2_NoCommit.sql", "UTF-8");
        this.assertGenerateFileEquals("Delete_Test3_NoCommit.sql", "UTF-8");
    }

    @Test
    public void testGenerateCleanInsert() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateCleanInsert.txt"});
        this.subDirectory = "generate/sql/clean-insert";
        this.assertGenerateFileEquals("Test1.sql", "UTF-8");
        this.assertGenerateFileEquals("Test2.sql", "UTF-8");
        this.assertGenerateFileEquals("Test3.sql", "UTF-8");
    }

    @Test
    public void testGenerateRefresh() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateRefresh.txt"});
        this.subDirectory = "generate/sql/refresh";
        this.assertGenerateFileEquals("Test1.sql", "UTF-8");
        this.assertGenerateFileEquals("Test2.sql", "UTF-8");
        this.assertGenerateFileEquals("Test3.sql", "UTF-8");
    }

    @Test
    public void testGenerateUpdate() throws Exception {
        Generate.main(new String[]{"@" + this.testResourcesDir + "/paramGenerateUpdate.txt"});
        this.subDirectory = "generate/sql/update";
        this.assertGenerateFileEquals("Test1.sql", "UTF-8");
        this.assertGenerateFileEquals("Test2.sql", "UTF-8");
        this.assertGenerateFileEquals("Test3.sql", "UTF-8");
    }

    private void assertGenerateFileEquals(final String target, final String encode) throws IOException {
        final String expect = Files.readString(new File(this.testResourcesDir + this.subDirectory + "/expect/txt", target).toPath(), Charset.forName(encode));
        final String actual = Files.readString(new File(this.baseDir + this.subDirectory + "/result", target).toPath(), Charset.forName(encode));
        Assert.assertEquals(expect, actual);
    }
}
