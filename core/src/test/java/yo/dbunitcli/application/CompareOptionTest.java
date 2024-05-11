package yo.dbunitcli.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.TableSeparators;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CompareOptionTest {

    private String baseDir;

    private Compare target;

    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        this.target = new Compare();
        this.baseDir = URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources");
    }

    @Test
    public void parseSettingTargetDirAndSettingFiles() {
        final CompareOption option = this.target.parseOption(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/filter/setting.json"});
        assertEquals(new File(this.baseDir + "/multidiff", "new"), option.getNewData().getParam().getSrc());
        assertEquals(new File(this.baseDir + "/multidiff", "old"), option.getOldData().getParam().getSrc());
        assertEquals(2, option.getTableSeparators().settings().size());
        final ComparableTable columnadd = option.oldDataSet().getTable("columnadd");
        assertEquals(1, columnadd.getTableMetaData().getPrimaryKeys().length);
        assertEquals("key", columnadd.getTableMetaData().getPrimaryKeys()[0].getColumnName());
    }

    @Test
    public void parseArgumentsLoadableFromParameterFile() {
        final CompareOption option = this.target.parseOption(new String[]{"@" + this.baseDir + "/paramCompareResultDiffValidExpected.txt"});
        assertEquals(new File(this.baseDir + "/multidiff", "new"), option.getNewData().getParam().getSrc().getAbsoluteFile());
        assertEquals(new File(this.baseDir + "/multidiff", "old"), option.getOldData().getParam().getSrc().getAbsoluteFile());
        final ComparableTable columnadd = option.oldDataSet().getTable("columnadd");
        assertEquals(1, columnadd.getTableMetaData().getPrimaryKeys().length);
        assertEquals("key", columnadd.getTableMetaData().getPrimaryKeys()[0].getColumnName());
        final ComparableTable columndrop = option.oldDataSet().getTable("columndrop");
        assertEquals(1, columndrop.getTableMetaData().getPrimaryKeys().length);
        assertEquals("key", columndrop.getTableMetaData().getPrimaryKeys()[0].getColumnName());
    }

    @Test
    public void parseDefaultResultDirEqualsCurrentDir() {
        final CompareOption option = this.target.parseOption(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old", "-setting=" + this.baseDir + "/multidiff/setting.json"});
        assertEquals(new File(".").getAbsoluteFile(), option.getConverterOption().getResultDir().getAbsoluteFile());
    }

    @Test
    public void parseResultDirChangeableCommandLineParameter() {
        final CompareOption option = this.target.parseOption(new String[]{"-new.src=" + this.baseDir + "/multidiff/new"
                , "-old.src=" + this.baseDir + "/multidiff/old"
                , "-setting=" + this.baseDir + "/multidiff/setting.json"
                , "-result=" + this.baseDir + "/result"});
        assertEquals(new File(this.baseDir, "result"), option.getConverterOption().getResultDir());
    }

    @Test
    public void parseNoSettingFile() {
        final CompareOption option = this.target.parseOption(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old"});
        assertEquals(TableSeparators.NONE, option.getTableSeparators());
    }

}