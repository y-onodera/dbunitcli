package yo.dbunitcli.application.command;

import org.junit.jupiter.api.*;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ScaffoldTest {

    private static final Properties backup = new Properties();

    @BeforeAll
    static void setUp() {
        ScaffoldTest.backup.putAll(System.getProperties());
    }

    abstract static class TestCase {

        @AfterAll
        static void restore() {
            System.setProperties(ScaffoldTest.backup);
        }

        protected String getResultBase() {
            return "target/test-temp/scaffold";
        }

        String[] args(final String subDir, final String... extra) {
            final String[] base = {"-result=" + Path.of("target/test-temp/scaffold", subDir)};
            final String[] result = new String[base.length + extra.length];
            System.arraycopy(base, 0, result, 0, base.length);
            System.arraycopy(extra, 0, result, base.length, extra.length);
            return result;
        }

        File resultFile(final String subDir, final String relativePath) {
            return Path.of(this.getResultBase(), subDir, relativePath).toFile();
        }

        @Nested
        class DdlTarget {

            @Test
            public void testAllFiles() throws Exception {
                TestCase.this.scaffold("ddl/all", "-target=ddl", "-setting=ddl", "-template=ddl", "-parameter=ddl");
                assertTrue(TestCase.this.resultFile("ddl/all", "resources/setting/ddl.json").exists());
                assertTrue(TestCase.this.resultFile("ddl/all", "resources/template/ddl.stg").exists());
                assertTrue(TestCase.this.resultFile("ddl/all", "resources/template/ddl.txt").exists());
                final File paramFile = TestCase.this.resultFile("ddl/all", "resources/param/ddl.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-template=resources/template/ddl.stg")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-setting=resources/setting/ddl.json")));
            }

            @Test
            public void testSettingOnly() {
                TestCase.this.scaffold("ddl/setting", "-target=ddl", "-setting=ddl");
                assertTrue(TestCase.this.resultFile("ddl/setting", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("ddl/setting", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("ddl/setting", "resources/template/ddl.txt").exists());
                assertFalse(TestCase.this.resultFile("ddl/setting", "resources/param/ddl.param").exists());
            }

            @Test
            public void testTemplateOnly() {
                TestCase.this.scaffold("ddl/template", "-target=ddl", "-template=ddl");
                assertFalse(TestCase.this.resultFile("ddl/template", "resources/setting/ddl.json").exists());
                assertTrue(TestCase.this.resultFile("ddl/template", "resources/template/ddl.stg").exists());
                assertTrue(TestCase.this.resultFile("ddl/template", "resources/template/ddl.txt").exists());
                assertFalse(TestCase.this.resultFile("ddl/template", "resources/param/ddl.param").exists());
            }

            @Test
            public void testParameterOnly() throws Exception {
                TestCase.this.scaffold("ddl/parameter", "-target=ddl", "-parameter=ddl");
                assertFalse(TestCase.this.resultFile("ddl/parameter", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("ddl/parameter", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("ddl/parameter", "resources/template/ddl.txt").exists());
                final File paramFile = TestCase.this.resultFile("ddl/parameter", "resources/param/ddl.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=ddl")));
                assertFalse(lines.stream().anyMatch(l -> l.contains("-template=")));
                assertFalse(lines.stream().anyMatch(l -> l.contains("-setting=")));
            }

            @Test
            public void testSettingAndTemplate() {
                TestCase.this.scaffold("ddl/setting-template", "-target=ddl", "-setting=ddl", "-template=ddl");
                assertTrue(TestCase.this.resultFile("ddl/setting-template", "resources/setting/ddl.json").exists());
                assertTrue(TestCase.this.resultFile("ddl/setting-template", "resources/template/ddl.stg").exists());
                assertTrue(TestCase.this.resultFile("ddl/setting-template", "resources/template/ddl.txt").exists());
                assertFalse(TestCase.this.resultFile("ddl/setting-template", "resources/param/ddl.param").exists());
            }

            @Test
            public void testCustomFileName() {
                TestCase.this.scaffold("ddl/custom", "-target=ddl", "-setting=myDdl", "-template=myDdl", "-parameter=myDdl");
                assertTrue(TestCase.this.resultFile("ddl/custom", "resources/setting/myDdl.json").exists());
                assertTrue(TestCase.this.resultFile("ddl/custom", "resources/template/myDdl.stg").exists());
                assertTrue(TestCase.this.resultFile("ddl/custom", "resources/template/myDdl.txt").exists());
                assertTrue(TestCase.this.resultFile("ddl/custom", "resources/param/myDdl.param").exists());
            }
        }

        @Nested
        class JavaBeanTarget {

            @Test
            public void testAllFiles() {
                TestCase.this.scaffold("javaBean/all", "-target=javaBean", "-setting=javaBean", "-template=javaBean", "-parameter=javaBean");
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/setting/javaBean.json").exists());
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/template/javaBean.stg").exists());
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/template/javaBean.txt").exists());
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/param/javaBean.param").exists());
            }

            @Test
            public void testCustomFileName() {
                TestCase.this.scaffold("javaBean/custom", "-target=javaBean", "-setting=myBean", "-template=myBean", "-parameter=myBean");
                assertTrue(TestCase.this.resultFile("javaBean/custom", "resources/setting/myBean.json").exists());
                assertTrue(TestCase.this.resultFile("javaBean/custom", "resources/template/myBean.stg").exists());
                assertTrue(TestCase.this.resultFile("javaBean/custom", "resources/template/myBean.txt").exists());
                assertTrue(TestCase.this.resultFile("javaBean/custom", "resources/param/myBean.param").exists());
            }
        }

        @Nested
        class NoOutput {

            @Test
            public void testNoOutputWhenNoParams() {
                TestCase.this.scaffold("nooutput/ddl", "-target=ddl");
                assertFalse(TestCase.this.resultFile("nooutput/ddl", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("nooutput/ddl", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("nooutput/ddl", "resources/param/ddl.param").exists());
            }
        }

        @Nested
        class ParameterTarget {

            @Test
            public void testParameterFileGenerated() throws Exception {
                TestCase.this.scaffold("parameter/generate", "-target=parameter", "-commandType=generate");
                final File paramFile = TestCase.this.resultFile("parameter/generate", "resources/param/generate.param");
                assertTrue(paramFile.exists());
                assertTrue(Files.size(paramFile.toPath()) > 0);
                assertFalse(TestCase.this.resultFile("parameter/generate", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("parameter/generate", "resources/setting/javaBean.json").exists());
            }

            @Test
            public void testNoOutputWhenNoTarget() {
                TestCase.this.scaffold("empty");
                assertFalse(TestCase.this.resultFile("empty", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("empty", "resources/setting/javaBean.json").exists());
                assertFalse(TestCase.this.resultFile("empty", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("empty", "resources/template/javaBean.stg").exists());
            }
        }

        private void scaffold(final String subDir, final String... extra) {
            Scaffold.main(this.args(subDir, extra));
        }
    }

    @Nested
    class NoSystemPropertyTest extends TestCase {
    }

    @Nested
    class ChangeResultBaseTest extends TestCase {

        private static final String TEMP = "target/test-temp/scaffold-sp/resultbase";

        @BeforeAll
        static void setup() {
            final Properties props = new Properties();
            props.putAll(ScaffoldTest.backup);
            props.put(FileResources.PROPERTY_RESULT_BASE, TEMP);
            System.setProperties(props);
        }

        @Override
        protected String getResultBase() {
            return TEMP + "/target/test-temp/scaffold";
        }
    }

    @Nested
    class ChangeWorkSpaceTest extends TestCase {

        private static final String TEMP = "target/test-temp/scaffold-sp/workspace";

        @BeforeAll
        static void setup() {
            final Properties props = new Properties();
            props.putAll(ScaffoldTest.backup);
            props.put(FileResources.PROPERTY_WORKSPACE, TEMP);
            System.setProperties(props);
        }

        @Override
        protected String getResultBase() {
            return TEMP + "/target/test-temp/scaffold";
        }
    }
}
