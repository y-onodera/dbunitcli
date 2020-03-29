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

    private String baseDir;

    private String subDirectory;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(this.getClass().getResource(".").getPath(), "UTF-8");
    }

    @Test
    public void testGenerateXlsx() throws Exception {
        Generate.main(new String[]{"@" + this.baseDir + "/paramGenerateXlsx.txt"});
    }

    @Test
    public void testGenerateTxt() throws Exception {
        Generate.main(new String[]{"@" + this.baseDir + "/paramGenerateTxt.txt"});
        this.subDirectory = "generate/row";
        assertGenerateFileEquals("SomeClassTest.txt", "MS932");
        assertGenerateFileEquals("OtherClassTest.txt", "MS932");
        assertGenerateFileEquals("AnotherClassTest.txt", "MS932");
    }

    @Test
    public void testGenerateTxtPerTable() throws Exception {
        Generate.main(new String[]{"@" + this.baseDir + "/paramGenerateTxtPerTable.txt"});
        this.subDirectory = "generate/table";
        assertGenerateFileEquals("Test1.txt", "UTF-8");
        assertGenerateFileEquals("Test2.txt", "UTF-8");
        assertGenerateFileEquals("Test3.txt", "UTF-8");
    }

    @Test
    public void testGenerateTxtWithMetaData() throws Exception {
        Generate.main(new String[]{"@" + this.baseDir + "/paramGenerateTxtWithMetaData.txt"});
        this.subDirectory = "generate/with_metadata";
        assertGenerateFileEquals("Test1.txt", "UTF-8");
        assertGenerateFileEquals("Test2.txt", "UTF-8");
        assertGenerateFileEquals("Test3.txt", "UTF-8");
    }

    private void assertGenerateFileEquals(String target, String encode) throws IOException {
        String expect = Files.asCharSource(new File(this.baseDir + this.subDirectory + "/expect/txt", target), Charset.forName(encode)).read();
        String actual = Files.asCharSource(new File(this.baseDir + this.subDirectory + "/result", target), Charset.forName(encode)).read();
        Assert.assertEquals(expect, actual);
    }
}
