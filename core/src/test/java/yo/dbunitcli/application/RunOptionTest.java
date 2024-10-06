package yo.dbunitcli.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.option.Option;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.Parameter;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RunOptionTest {

    RunDto src = new RunDto();

    @BeforeEach
    void setUp() {
        this.src.getSrcData().setSrcType(DataSourceType.file);
        this.src.getSrcData().setSrc("src");
        this.src.getSrcData().setExtension("sql");
        this.src.getSrcData().setIncludeMetaData("false");
        this.src.getSrcData().setLoadData("true");
        this.src.getTemplateOption().setEncoding("UTF-8");
        this.src.getJdbcOption().setJdbcProperties("test.properties");
        this.src.setBaseDir(".");
        this.src.setAntTarget("default");
    }

    private RunOption createTarget() {
        return new RunOption(this.src, Parameter.NONE);
    }

    @Nested
    class ScriptTypeCmd {

        @BeforeEach
        void setUp() {
            RunOptionTest.this.src.setScriptType(RunOption.ScriptType.cmd);
        }

        @Test
        void toCommandLineArgs() throws IOException {
            final RunOption target = RunOptionTest.this.createTarget();
            final Option.CommandLineArgs result = target.toCommandLineArgs();
            assertEquals(RunOptionTest.this.src.getScriptType().toString(), result.getArg("-scriptType").value());
            assertEquals(RunOptionTest.this.src.getSrcData().getSrc(), result.getArg("-src.src").value());
            assertNull(result.getArg("-src.srcType"));
            assertNull(result.getArg("-src.extension"));
            assertNull(result.getArg("-src.includeMetaData"));
            assertNull(result.getArg("-src.loadData"));
            assertNull(result.getArg("-template.encoding"));
            assertNull(result.getArg("-jdbc.jdbcProperties"));
            assertNull(result.getArg("-antTarget"));
            assertEquals(new File(RunOptionTest.this.src.getBaseDir()).getCanonicalPath(), result.getArg("-baseDir").value());
        }
    }

    @Nested
    class ScriptTypeAnt {

        @BeforeEach
        void setUp() {
            RunOptionTest.this.src.setScriptType(RunOption.ScriptType.ant);
        }

        @Test
        void toCommandLineArgs() throws IOException {
            final RunOption target = RunOptionTest.this.createTarget();
            final Option.CommandLineArgs result = target.toCommandLineArgs();
            assertEquals(RunOptionTest.this.src.getScriptType().toString(), result.getArg("-scriptType").value());
            assertEquals(RunOptionTest.this.src.getSrcData().getSrc(), result.getArg("-src.src").value());
            assertNull(result.getArg("-src.srcType"));
            assertNull(result.getArg("-src.extension"));
            assertNull(result.getArg("-src.includeMetaData"));
            assertNull(result.getArg("-src.loadData"));
            assertNull(result.getArg("-template.encoding"));
            assertNull(result.getArg("-jdbc.jdbcProperties"));
            assertEquals(RunOptionTest.this.src.getAntTarget(), result.getArg("-antTarget").value());
            assertEquals(new File(RunOptionTest.this.src.getBaseDir()).getCanonicalPath(), result.getArg("-baseDir").value());
        }
    }

    @Nested
    class ScriptTypeBat {

        @BeforeEach
        void setUp() {
            RunOptionTest.this.src.setScriptType(RunOption.ScriptType.bat);
        }

        @Test
        void toCommandLineArgs() throws IOException {
            final RunOption target = RunOptionTest.this.createTarget();
            final Option.CommandLineArgs result = target.toCommandLineArgs();
            assertEquals(RunOptionTest.this.src.getScriptType().toString(), result.getArg("-scriptType").value());
            assertEquals(RunOptionTest.this.src.getSrcData().getSrc(), result.getArg("-src.src").value());
            assertNull(result.getArg("-src.srcType"));
            assertNull(result.getArg("-src.extension"));
            assertNull(result.getArg("-src.includeMetaData"));
            assertNull(result.getArg("-src.loadData"));
            assertNull(result.getArg("-template.encoding"));
            assertNull(result.getArg("-jdbc.jdbcProperties"));
            assertNull(result.getArg("-antTarget"));
            assertEquals(new File(RunOptionTest.this.src.getBaseDir()).getCanonicalPath(), result.getArg("-baseDir").value());
        }
    }

    @Nested
    class ScriptTypeSql {

        @BeforeEach
        void setUp() {
            RunOptionTest.this.src.setScriptType(RunOption.ScriptType.sql);
        }

        @Test
        void toCommandLineArgs() {
            final RunOption target = RunOptionTest.this.createTarget();
            final Option.CommandLineArgs result = target.toCommandLineArgs();
            assertEquals(RunOptionTest.this.src.getScriptType().toString(), result.getArg("-scriptType").value());
            assertEquals(RunOptionTest.this.src.getSrcData().getSrc(), result.getArg("-src.src").value());
            assertNull(result.getArg("-src.srcType"));
            assertNull(result.getArg("-src.extension"));
            assertNull(result.getArg("-src.includeMetaData"));
            assertNull(result.getArg("-src.loadData"));
            assertEquals(RunOptionTest.this.src.getTemplateOption().getEncoding(), result.getArg("-template.encoding").value());
            assertEquals(RunOptionTest.this.src.getJdbcOption().getJdbcProperties(), result.getArg("-jdbc.jdbcProperties").value());
            assertNull(result.getArg("-antTarget"));
            assertNull(result.getArg("-baseDir"));
        }
    }
}