package yo.dbunitcli.application;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kohsuke.args4j.CmdLineException;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class CompareOptionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private String baseDir = this.getClass().getResource(".").getPath();

    private CompareOption target;

    @Before
    public void setUp() {
        this.target = new CompareOption();
    }

    @Test
    public void parseRequiredOldFileDir() throws CmdLineException {
        this.expectedException.expect(CmdLineException.class);
        this.expectedException.expectMessage("Option \"-old\" is required");
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredNewFileDir() throws CmdLineException {
        this.expectedException.expect(CmdLineException.class);
        this.expectedException.expectMessage("Option \"-new\" is required");
        this.target.parse(new String[]{"-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredSettingFile() throws CmdLineException {
        this.expectedException.expect(CmdLineException.class);
        this.expectedException.expectMessage("Option \"-setting\" is required");
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-old=" + this.baseDir + "/multidiff/old"});
    }

    @Test
    public void parseRequiredOldFileDirExists() throws CmdLineException {
        this.expectedException.expect(CmdLineException.class);
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-old=" + this.baseDir + "/notExists", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredNewFileDirExists() throws CmdLineException {
        this.expectedException.expect(CmdLineException.class);
        this.target.parse(new String[]{"-new=" + this.baseDir + "/notExists", "-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredSettingFileExists() throws CmdLineException {
        this.expectedException.expect(CmdLineException.class);
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/NotExists.json"});
    }

    @Test
    public void parseSettingTargetDirAndSettingFiles() throws CmdLineException {
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
    public void parseArgumentsLoadableFromParameterFile() throws CmdLineException {
        this.target.parse(new String[]{"@" + this.baseDir + "/paramResultDiffValidExpected.txt"});
        assertEquals(new File(this.baseDir + "/multidiff", "new"), this.target.getNewDir().getAbsoluteFile());
        assertEquals(new File(this.baseDir + "/multidiff", "old"), this.target.getOldDir().getAbsoluteFile());
        assertEquals(1, this.target.getComparisonKeys().getColumns("columnadd").size());
        assertEquals("key", this.target.getComparisonKeys().getColumns("columnadd").get(0));
    }

    @Test
    public void parseDefaultResultDirEqualsCurrentDir() throws CmdLineException {
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
        assertEquals(new File("").getAbsoluteFile(), this.target.getResultDir());
    }

    @Test
    public void parseResultDirChangeableCommandLineParameter() throws CmdLineException {
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new"
                , "-old=" + this.baseDir + "/multidiff/old"
                , "-setting=" + this.baseDir + "/multidiff/setting.json"
                , "-result=" + this.baseDir + "/result"});
        assertEquals(new File(this.baseDir, "result"), this.target.getResultDir());
    }

    @Test
    public void parseDefaultEncodingIsSystemFileEncoding() throws CmdLineException {
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
        assertEquals(System.getProperty("file.encoding"), this.target.getEncoding());
    }

    @Test
    public void parseEncodingChangeableCommandLineParameter() throws CmdLineException {
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new"
                , "-old=" + this.baseDir + "/multidiff/old"
                , "-setting=" + this.baseDir + "/multidiff/setting.json"
                , "-encoding=windows-31j"});
        assertEquals("windows-31j", this.target.getEncoding());
    }
}