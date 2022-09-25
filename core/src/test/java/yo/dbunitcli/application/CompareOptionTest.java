package yo.dbunitcli.application;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import yo.dbunitcli.dataset.AddSettingColumns;

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
        this.baseDir = URLDecoder.decode(this.getClass().getResource(".").getPath(), "UTF-8")
                .replace("target/test-classes","src/test/resources");
    }

    @Test
    public void parseRequiredOldFileDir() throws Exception {
        this.expectedException.expect(Exception.class);
        this.expectedException.expectMessage("Option \"-src\" is required");
        this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredNewFileDir() throws Exception {
        this.expectedException.expect(Exception.class);
        this.expectedException.expectMessage("Option \"-src\" is required");
        this.target.parse(new String[]{"-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredOldFileDirExists() throws Exception {
        this.expectedException.expect(Exception.class);
        this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/notExists", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredNewFileDirExists() throws Exception {
        this.expectedException.expect(Exception.class);
        this.target.parse(new String[]{"-new.src=" + this.baseDir + "/notExists", "-old.src=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
    }

    @Test
    public void parseRequiredSettingFileExists() throws Exception {
        this.expectedException.expect(Exception.class);
        this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/NotExists.json"});
    }

    @Test
    public void parseSettingTargetDirAndSettingFiles() throws Exception {
        this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/filter/setting.json"});
        assertEquals(new File(this.baseDir + "/multidiff", "new"), this.target.getNewData().getParam().getSrc());
        assertEquals(new File(this.baseDir + "/multidiff", "old"), this.target.getOldData().getParam().getSrc());
        assertEquals(2, this.target.getComparisonKeys().byNameSize());
        assertEquals(1, this.target.getComparisonKeys().getColumns("columnadd").size());
        assertEquals("key", this.target.getComparisonKeys().getColumns("columnadd").get(0));
        assertEquals(2, this.target.getColumnSettings().getExcludeColumns().byNameSize());
        assertEquals(2, this.target.getColumnSettings().getExcludeColumns().getColumns("columnadd").size());
        assertEquals("AddColumn", this.target.getColumnSettings().getExcludeColumns().getColumns("columnadd").get(0));
        assertEquals("ChangeColumn", this.target.getColumnSettings().getExcludeColumns().getColumns("columnadd").get(1));
    }

    @Test
    public void parseArgumentsLoadableFromParameterFile() throws Exception {
        this.target.parse(new String[]{"@" + this.baseDir + "/paramCompareResultDiffValidExpected.txt"});
        assertEquals(new File(this.baseDir + "/multidiff", "new"), this.target.getNewData().getParam().getSrc().getAbsoluteFile());
        assertEquals(new File(this.baseDir + "/multidiff", "old"), this.target.getOldData().getParam().getSrc().getAbsoluteFile());
        assertEquals(1, this.target.getComparisonKeys().getColumns("columnadd").size());
        assertEquals("key", this.target.getComparisonKeys().getColumns("columnadd").get(0));
    }

    @Test
    public void parseDefaultResultDirEqualsCurrentDir() throws Exception {
        this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
        assertEquals(new File(".").getAbsoluteFile(), this.target.getConverterOption().getResultDir().getAbsoluteFile());
    }

    @Test
    public void parseResultDirChangeableCommandLineParameter() throws Exception {
        this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new"
                , "-old.src=" + this.baseDir + "/multidiff/old"
                , "-setting=" + this.baseDir + "/multidiff/setting.json"
                , "-result=" + this.baseDir + "/result"});
        assertEquals(new File(this.baseDir, "result"), this.target.getConverterOption().getResultDir());
    }

    @Test
    public void parseNoSettingFile() throws Exception {
        this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old"});
        assertEquals(AddSettingColumns.NONE, this.target.getComparisonKeys());
        assertEquals(AddSettingColumns.NONE, this.target.getColumnSettings().getExcludeColumns());
        assertEquals(AddSettingColumns.NONE, this.target.getColumnSettings().getOrderColumns());
    }


}