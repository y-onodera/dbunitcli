package yo.dbunitcli.application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.TableSeparators;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class CompareOptionTest {

    private String baseDir;

    private CompareOption target;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.target = new CompareOption();
        this.baseDir = URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources");
    }

    @Test
    public void parseRequiredOldFileDir() {
        Assert.assertThrows("Option \"-src\" is required", CommandLine.MissingParameterException.class,
                () -> this.target.parse(new String[]{"-new=" + this.baseDir + "/multidiff/new", "-setting=" + this.baseDir + "/multidiff/setting.json"}));
    }

    @Test
    public void parseRequiredNewFileDir() {
        Assert.assertThrows("Option \"-src\" is required", CommandLine.MissingParameterException.class,
                () -> this.target.parse(new String[]{"-old=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"})
        );
    }

    @Test
    public void parseRequiredOldFileDirExists() {
        Assert.assertThrows(AssertionError.class,
                () -> this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/notExists", "-setting=" + this.baseDir + "/multidiff/setting.json"})
        );
    }

    @Test
    public void parseRequiredNewFileDirExists() {
        Assert.assertThrows(AssertionError.class,
                () -> this.target.parse(new String[]{"-new.src=" + this.baseDir + "/notExists", "-old.src=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"})
        );
    }

    @Test
    public void parseRequiredSettingFileExists() {
        Assert.assertThrows(AssertionError.class,
                () -> this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/NotExists.json"})
        );
    }

    @Test
    public void parseSettingTargetDirAndSettingFiles() {
        this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/filter/setting.json"});
        assertEquals(new File(this.baseDir + "/multidiff", "new"), this.target.getNewData().getParam().getSrc());
        assertEquals(new File(this.baseDir + "/multidiff", "old"), this.target.getOldData().getParam().getSrc());
        assertEquals(2, this.target.getTableSeparators().settings().size());
        final ComparableTable columnadd = this.target.oldDataSet().getTable("columnadd");
        assertEquals(1, columnadd.getTableMetaData().getPrimaryKeys().length);
        assertEquals("key", columnadd.getTableMetaData().getPrimaryKeys()[0].getColumnName());
    }

    @Test
    public void parseArgumentsLoadableFromParameterFile() {
        this.target.parse(new String[]{"@" + this.baseDir + "/paramCompareResultDiffValidExpected.txt"});
        assertEquals(new File(this.baseDir + "/multidiff", "new"), this.target.getNewData().getParam().getSrc().getAbsoluteFile());
        assertEquals(new File(this.baseDir + "/multidiff", "old"), this.target.getOldData().getParam().getSrc().getAbsoluteFile());
        final ComparableTable columnadd = this.target.oldDataSet().getTable("columnadd");
        assertEquals(1, columnadd.getTableMetaData().getPrimaryKeys().length);
        assertEquals("key", columnadd.getTableMetaData().getPrimaryKeys()[0].getColumnName());
        final ComparableTable columndrop = this.target.oldDataSet().getTable("columndrop");
        assertEquals(1, columndrop.getTableMetaData().getPrimaryKeys().length);
        assertEquals("key", columndrop.getTableMetaData().getPrimaryKeys()[0].getColumnName());
    }

    @Test
    public void parseDefaultResultDirEqualsCurrentDir() {
        this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
        assertEquals(new File(".").getAbsoluteFile(), this.target.getConverterOption().getResultDir().getAbsoluteFile());
    }

    @Test
    public void parseResultDirChangeableCommandLineParameter() {
        this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new"
                , "-old.src=" + this.baseDir + "/multidiff/old"
                , "-setting=" + this.baseDir + "/multidiff/setting.json"
                , "-result=" + this.baseDir + "/result"});
        assertEquals(new File(this.baseDir, "result"), this.target.getConverterOption().getResultDir());
    }

    @Test
    public void parseNoSettingFile() {
        this.target.parse(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old"});
        assertEquals(TableSeparators.NONE, this.target.getTableSeparators());
    }


}