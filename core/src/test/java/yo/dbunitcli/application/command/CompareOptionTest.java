package yo.dbunitcli.application.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.TableSeparators;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CompareOptionTest {

    private static final String PARAMETER_DIR = "src/test/resources/yo/dbunitcli/application/command";

    private String baseDir;

    private Compare target;

    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        this.target = new Compare();
        this.baseDir = URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8)
                                 .replace("target/test-classes", "src/test/resources")
                                 .replace("/command", "");
    }

    @Test
    public void parseSettingTargetDirAndSettingFiles() {
        final CompareOption option = this.target.parseOption(new String[]{
                "-new.src=" + this.baseDir + "/src/compare" + "/multidiff/new"
                , "-old.src=" + this.baseDir + "/src/compare" + "/multidiff/old"
                , "-setting=" + this.baseDir + "/settings/filter" + "/setting.json"
        });
        assertEquals(new File(this.baseDir + "/src/compare/multidiff", "new"), option.newData().getParam().getSrc());
        assertEquals(new File(this.baseDir + "/src/compare/multidiff", "old"), option.oldData().getParam().getSrc());
        assertEquals(2, option.getTableSeparators().settings().size());
        final ComparableTable columnadd = option.oldDataSet().getTable("columnadd");
        assertEquals(1, columnadd.getTableMetaData().getPrimaryKeys().length);
        assertEquals("key", columnadd.getTableMetaData().getPrimaryKeys()[0].getColumnName());
    }

    @Test
    public void parseArgumentsLoadableFromParameterFile() {
        final CompareOption option = this.target.parseOption(new String[]{"@" + PARAMETER_DIR + "/paramCompareResultDiffValidExpected.txt"});
        assertEquals(new File(this.baseDir + "/src/compare/multidiff", "new"),
                     option.newData().getParam().getSrc().getAbsoluteFile());
        assertEquals(new File(this.baseDir + "/src/compare/multidiff", "old"), option.oldData().getParam().getSrc().getAbsoluteFile());
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
        assertEquals(new File(".").getAbsoluteFile(), option.result().convertResult().getResultDir().getAbsoluteFile());
    }

    @Test
    public void parseResultDirChangeableCommandLineParameter() {
        final CompareOption option = this.target.parseOption(new String[]{"-new.src=" + this.baseDir + "/multidiff/new"
                , "-old.src=" + this.baseDir + "/multidiff/old"
                , "-setting=" + this.baseDir + "/multidiff/setting.json"
                , "-result=" + this.baseDir + "/result"});
        assertEquals(new File(this.baseDir, "result"), option.result().convertResult().getResultDir());
    }

    @Test
    public void parseNoSettingFile() {
        final CompareOption option = this.target.parseOption(new String[]{"-new.src=" + this.baseDir + "/multidiff/new", "-old.src=" + this.baseDir + "/multidiff/old"});
        assertEquals(TableSeparators.NONE, option.getTableSeparators());
    }

    @Test
    public void generateCorrectionSqlWhenRowDiffFound() throws IOException {
        final File resultDir = new File("target/test-temp/compareoption/correctionsql");
        final CompareOption option = this.target.parseOption(new String[]{
                "-new.src=" + this.baseDir + "/src/compare/correctionsql/new"
                , "-old.src=" + this.baseDir + "/src/compare/correctionsql/old"
                , "-setting=" + this.baseDir + "/settings/correctionsql/setting.json"
                , "-correctionSql=true"
                , "-result=" + resultDir.getPath()
        });
        option.compare();
        final File sqlFile = new File(resultDir, "USERS$CORRECTION.sql");
        assertTrue(sqlFile.exists());
        final String content = Files.readString(sqlFile.toPath(), StandardCharsets.UTF_8);
        assertEquals("update USERS set name = 'Alice' ,age = '21'  where id = '1' ;\n"
                + "insert into USERS (id ,name ,age ) values ('4' ,'Dave' ,'40' );\n"
                + "delete from USERS where id = '2' ;\n", content);
    }

    @Test
    public void skipCorrectionSqlWhenOptionDisabled() {
        final File resultDir = new File("target/test-temp/compareoption/correctionsqldisabled");
        final CompareOption option = this.target.parseOption(new String[]{
                "-new.src=" + this.baseDir + "/src/compare/correctionsql/new"
                , "-old.src=" + this.baseDir + "/src/compare/correctionsql/old"
                , "-setting=" + this.baseDir + "/settings/correctionsql/setting.json"
                , "-result=" + resultDir.getPath()
        });
        option.compare();
        assertTrue(!new File(resultDir, "USERS$CORRECTION.sql").exists());
    }

}