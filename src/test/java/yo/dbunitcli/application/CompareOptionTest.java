package yo.dbunitcli.application;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.junit.Assert.assertEquals;

public class CompareOptionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private String baseDir;

    private CompareOption target;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.target = new CompareOption();
        this.baseDir = URLDecoder.decode(this.getClass().getResource(".").getPath(),"MS932");
    }

    @Test
    public void parseRequiredOldFileDir() throws Exception {
        this.expectedException.expect(Exception.class);
        this.expectedException.expectMessage("Option \"-old\" is required");
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredNewFileDir() throws Exception {
        this.expectedException.expect(Exception.class);
        this.expectedException.expectMessage("Option \"-new\" is required");
        this.target.parse(new String[]{"-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredSettingFile() throws Exception {
        this.expectedException.expect(Exception.class);
        this.expectedException.expectMessage("Option \"-setting\" is required");
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-old=" + this.baseDir + "/multidiff/old"});
    }

    @Test
    public void parseRequiredOldFileDirExists() throws Exception {
        this.expectedException.expect(Exception.class);
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-old=" + this.baseDir + "/notExists", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredNewFileDirExists() throws Exception {
        this.expectedException.expect(Exception.class);
        this.target.parse(new String[]{"-new=" + this.baseDir + "/notExists", "-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredSettingFileExists() throws Exception {
        this.expectedException.expect(Exception.class);
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/NotExists.json"});
    }

    @Test
    public void parseSettingTargetDirAndSettingFiles() throws Exception {
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/filter/setting.json"});
        assertEquals(new File(this.baseDir + "/multidiff", "new"), this.target.getNewDir());
        assertEquals(new File(this.baseDir + "/multidiff", "old"), this.target.getOldDir());
        assertEquals(2, this.target.getComparisonKeys().byNameSize());
        assertEquals(1, this.target.getComparisonKeys().getColumns("columnadd").size());
        assertEquals("key", this.target.getComparisonKeys().getColumns("columnadd").get(0));
        assertEquals(2, this.target.getExcludeColumns().byNameSize());
        assertEquals(2, this.target.getExcludeColumns().getColumns("columnadd").size());
        assertEquals("AddColumn", this.target.getExcludeColumns().getColumns("columnadd").get(0));
        assertEquals("ChangeColumn", this.target.getExcludeColumns().getColumns("columnadd").get(1));
    }

    @Test
    public void parseArgumentsLoadableFromParameterFile() throws Exception {
        this.target.parse(new String[]{"@" + this.baseDir + "/paramResultDiffValidExpected.txt"});
        assertEquals(new File(this.baseDir + "/multidiff", "new"), this.target.getNewDir().getAbsoluteFile());
        assertEquals(new File(this.baseDir + "/multidiff", "old"), this.target.getOldDir().getAbsoluteFile());
        assertEquals(1, this.target.getComparisonKeys().getColumns("columnadd").size());
        assertEquals("key", this.target.getComparisonKeys().getColumns("columnadd").get(0));
    }

    @Test
    public void parseDefaultResultDirEqualsCurrentDir() throws Exception {
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
        assertEquals(new File("").getAbsoluteFile(), this.target.getResultDir());
    }

    @Test
    public void parseResultDirChangeableCommandLineParameter() throws Exception {
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new"
                , "-old=" + this.baseDir + "/multidiff/old"
                , "-setting=" + this.baseDir + "/multidiff/setting.json"
                , "-result=" + this.baseDir + "/result"});
        assertEquals(new File(this.baseDir, "result"), this.target.getResultDir());
    }

    @Test
    public void parseDefaultEncodingIsSystemFileEncoding() throws Exception {
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
        assertEquals(System.getProperty("file.encoding"), this.target.getEncoding());
    }

    @Test
    public void parseEncodingChangeableCommandLineParameter() throws Exception {
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new"
                , "-old=" + this.baseDir + "/multidiff/old"
                , "-setting=" + this.baseDir + "/multidiff/setting.json"
                , "-encoding=windows-31j"});
        assertEquals("windows-31j", this.target.getEncoding());
    }
}