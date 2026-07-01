package yo.dbunitcli.application.command;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        protected String getResultBasePrefix() {
            return ".";
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

        private void scaffold(final String subDir, final String... extra) {
            Scaffold.main(this.args(subDir, extra));
        }

        @Nested
        class DdlTarget {

            @Test
            public void testAllFiles() throws Exception {
                TestCase.this.scaffold("ddl/all", "-target=ddl", "-setting=ddl", "-template=ddl", "-parameter=ddl");
                assertTrue(TestCase.this.resultFile("ddl/all", "resources/setting/ddl.json").exists());
                assertTrue(TestCase.this.resultFile("ddl/all", "resources/template/ddl.stg").exists());
                assertTrue(TestCase.this.resultFile("ddl/all", "resources/template/ddl.txt").exists());
                final File paramFile = TestCase.this.resultFile("ddl/all", "option/ddl.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=ddl")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-template=resources/template/ddl.txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("templateGroup=resources/template/ddl.stg")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-setting=resources/setting/ddl.json")));
            }

            @Test
            public void testSettingOnly() {
                TestCase.this.scaffold("ddl/setting", "-target=ddl", "-setting=ddl");
                assertTrue(TestCase.this.resultFile("ddl/setting", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("ddl/setting", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("ddl/setting", "resources/template/ddl.txt").exists());
                assertFalse(TestCase.this.resultFile("ddl/setting", "option/ddl.param").exists());
            }

            @Test
            public void testTemplateOnly() {
                TestCase.this.scaffold("ddl/template", "-target=ddl", "-template=ddl");
                assertFalse(TestCase.this.resultFile("ddl/template", "resources/setting/ddl.json").exists());
                assertTrue(TestCase.this.resultFile("ddl/template", "resources/template/ddl.stg").exists());
                assertTrue(TestCase.this.resultFile("ddl/template", "resources/template/ddl.txt").exists());
                assertFalse(TestCase.this.resultFile("ddl/template", "option/ddl.param").exists());
            }

            @Test
            public void testParameterOnly() throws Exception {
                TestCase.this.scaffold("ddl/parameter", "-target=ddl", "-parameter=ddl");
                assertFalse(TestCase.this.resultFile("ddl/parameter", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("ddl/parameter", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("ddl/parameter", "resources/template/ddl.txt").exists());
                final File paramFile = TestCase.this.resultFile("ddl/parameter", "option/ddl.param");
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
                assertFalse(TestCase.this.resultFile("ddl/setting-template", "option/ddl.param").exists());
            }

            @Test
            public void testCustomFileName() {
                TestCase.this.scaffold("ddl/custom", "-target=ddl", "-setting=myDdl", "-template=myDdl",
                                       "-parameter=myDdl");
                assertTrue(TestCase.this.resultFile("ddl/custom", "resources/setting/myDdl.json").exists());
                assertTrue(TestCase.this.resultFile("ddl/custom", "resources/template/myDdl.stg").exists());
                assertTrue(TestCase.this.resultFile("ddl/custom", "resources/template/myDdl.txt").exists());
                assertTrue(TestCase.this.resultFile("ddl/custom", "option/myDdl.param").exists());
            }
        }

        @Nested
        class JavaBeanTarget {

            @Test
            public void testAllFiles() {
                TestCase.this.scaffold("javaBean/all", "-target=javaBean", "-setting=javaBean", "-template=javaBean",
                                       "-parameter=javaBean");
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/setting/javaBean.json").exists());
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/template/javaBean.stg").exists());
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/template/javaBean.txt").exists());
                assertTrue(TestCase.this.resultFile("javaBean/all", "option/javaBean.param").exists());
            }

            @Test
            public void testCustomFileName() {
                TestCase.this.scaffold("javaBean/custom", "-target=javaBean", "-setting=myBean", "-template=myBean",
                                       "-parameter=myBean");
                assertTrue(TestCase.this.resultFile("javaBean/custom", "resources/setting/myBean.json").exists());
                assertTrue(TestCase.this.resultFile("javaBean/custom", "resources/template/myBean.stg").exists());
                assertTrue(TestCase.this.resultFile("javaBean/custom", "resources/template/myBean.txt").exists());
                assertTrue(TestCase.this.resultFile("javaBean/custom", "option/myBean.param").exists());
            }
        }

        @Nested
        class DatasetOption {

            private static final String SRC_DIR =
                    "src/test/resources/yo/dbunitcli/application/command/scaffold/src";

            @Test
            public void testDatasetCreatesSrcCsvForDdl() throws Exception {
                this.assertDatasetCreatesSrcCsv("ddl-csv", "ddl", "COLUMN_NAME", "id", "name", "email");
            }

            @Test
            public void testDatasetCreatesSrcCsvForJavaBean() throws Exception {
                this.assertDatasetCreatesSrcCsv("javaBean-csv", "javaBean", "COLUMN_NAME");
            }

            @Test
            public void testDatasetParamFileIncludesSrcInfo() throws Exception {
                TestCase.this.scaffold("dataset/ddl-param", "-target=ddl", "-parameter=ddl",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File paramFile = TestCase.this.resultFile("dataset/ddl-param", "option/ddl.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-src.src=src")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-src.srcType=csv")));
            }

            @Test
            public void testDatasetTypeXlsxCreatesSrcXlsx() {
                TestCase.this.scaffold("dataset/ddl-xlsx", "-target=ddl",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv",
                                       "-datasetType=xlsx");
                final File srcDir = TestCase.this.resultFile("dataset/ddl-xlsx", "src");
                assertTrue(srcDir.isDirectory());
                final File[] files = srcDir.listFiles();
                assertNotNull(files);
                assertTrue(Arrays.stream(files).anyMatch(f -> f.getName().endsWith(".xlsx")));
                assertFalse(Arrays.stream(files).anyMatch(f -> f.getName().endsWith(".csv")));
            }

            @Test
            public void testNoDatasetOptionNoSrcDir() {
                TestCase.this.scaffold("dataset/no-dataset", "-target=ddl", "-parameter=ddl");
                assertFalse(TestCase.this.resultFile("dataset/no-dataset", "src").exists());
            }

            @Test
            public void testDatasetEncodingInParamFile() throws Exception {
                TestCase.this.scaffold("dataset/ddl-encoding", "-target=ddl", "-parameter=ddl",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv",
                                       "-datasetEncoding=Shift_JIS");
                final List<String> lines = Files.readAllLines(
                        TestCase.this.resultFile("dataset/ddl-encoding", "option/ddl.param").toPath(),
                        StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-datasetEncoding=Shift_JIS")));
            }

            private void assertDatasetCreatesSrcCsv(final String dir, final String target,
                                                    final String... expectedColumns) throws Exception {
                TestCase.this.scaffold("dataset/" + dir, "-target=" + target, "-parameter=" + target,
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File srcFile = TestCase.this.resultFile("dataset/" + dir, "src/SAMPLE.csv");
                assertTrue(srcFile.exists());
                final List<String> lines = Files.readAllLines(srcFile.toPath(), StandardCharsets.UTF_8);
                for (final String col : expectedColumns) {
                    assertTrue(lines.stream().anyMatch(l -> l.contains(col)));
                }
            }
        }

        @Nested
        class ScaffoldToGenerate {

            private static final String SRC_DIR =
                    "src/test/resources/yo/dbunitcli/application/command/scaffold/src";

            @Test
            public void testDdlScaffoldToGenerate() throws Exception {
                this.assertScaffoldGenerates("ddl", "e2e/ddl", "ddl/SAMPLE.sql");
            }

            @Test
            public void testJavaBeanScaffoldToGenerate() throws Exception {
                this.assertScaffoldGenerates("javaBean", "e2e/javaBean", "javaBean/Sample.java");
            }

            private void assertScaffoldGenerates(final String target, final String subDir,
                                                  final String expectedOutput) throws Exception {
                TestCase.this.scaffold(subDir, "-target=" + target, "-setting=" + target, "-parameter=" + target,
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File paramFile = TestCase.this.resultFile(subDir, "option/" + target + ".param");
                assertTrue(paramFile.exists());

                final File scaffoldDir = Path.of(TestCase.this.getResultBase(), subDir).toFile();
                final Properties saved = (Properties) System.getProperties().clone();
                final Properties withWorkspace = new Properties();
                withWorkspace.putAll(saved);
                withWorkspace.put(FileResources.PROPERTY_WORKSPACE, scaffoldDir.getCanonicalPath());
                withWorkspace.put(FileResources.PROPERTY_RESULT_BASE, TestCase.this.getResultBasePrefix());
                System.setProperties(withWorkspace);
                try {
                    Generate.main(new String[]{"@" + paramFile.getAbsolutePath()});
                } finally {
                    System.setProperties(saved);
                }
                assertTrue(new File(scaffoldDir, expectedOutput).exists());
            }
        }

        @Nested
        class NoOutput {

            @Test
            public void testNoOutputWhenNoParams() {
                TestCase.this.scaffold("nooutput/ddl", "-target=ddl");
                assertFalse(TestCase.this.resultFile("nooutput/ddl", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("nooutput/ddl", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("nooutput/ddl", "option/ddl.param").exists());
            }
        }

        @Nested
        class ParameterTarget {

            @Test
            public void testParameterFileGenerated() throws Exception {
                TestCase.this.scaffold("parameter/generate", "-target=parameter", "-commandType=generate");
                final File paramFile = TestCase.this.resultFile("parameter/generate", "option/generate.param");
                this.assertParamFileExists(paramFile);
                assertFalse(TestCase.this.resultFile("parameter/generate", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("parameter/generate", "resources/setting/javaBean.json").exists());
            }

            @ParameterizedTest
            @ValueSource(strings = {"compare", "convert", "parameterize", "run"})
            public void testParameterFileGeneratedForCommandType(final String commandType) throws Exception {
                TestCase.this.scaffold("parameter/" + commandType, "-target=parameter", "-commandType=" + commandType);
                this.assertParamFileExists(TestCase.this.resultFile("parameter/" + commandType,
                                                                    "option/" + commandType + ".param"));
            }

            @ParameterizedTest
            @ValueSource(strings = {"txt", "ddl", "javaBean"})
            public void testGenerateWithGenerateType(final String generateType) throws Exception {
                final String subDir = "parameter/generate-type-" + generateType;
                TestCase.this.scaffold(subDir, "-target=parameter", "-commandType=generate",
                                       "-commandInput.generateType=" + generateType);
                this.assertParamFileContains(TestCase.this.resultFile(subDir, "option/generate.param"),
                                             "-generateType=" + generateType);
            }

            @ParameterizedTest
            @ValueSource(strings = {"csv", "xlsx"})
            public void testGenerateWithSrcType(final String srcType) throws Exception {
                final String subDir = "parameter/generate-src-" + srcType;
                TestCase.this.scaffold(subDir, "-target=parameter", "-commandType=generate",
                                       "-commandInput.src.srcType=" + srcType);
                this.assertParamFileContains(TestCase.this.resultFile(subDir, "option/generate.param"),
                                             "-src.srcType=" + srcType);
            }

            @ParameterizedTest
            @ValueSource(strings = {"data", "image"})
            public void testCompareWithTargetType(final String targetType) throws Exception {
                final String subDir = "parameter/compare-target-" + targetType;
                TestCase.this.scaffold(subDir, "-target=parameter", "-commandType=compare",
                                       "-commandInput.targetType=" + targetType);
                this.assertParamFileContains(TestCase.this.resultFile(subDir, "option/compare.param"),
                                             "-targetType=" + targetType);
            }

            @ParameterizedTest
            @ValueSource(strings = {"csv", "xlsx"})
            public void testConvertWithSrcType(final String srcType) throws Exception {
                final String subDir = "parameter/convert-src-" + srcType;
                TestCase.this.scaffold(subDir, "-target=parameter", "-commandType=convert",
                                       "-commandInput.src.srcType=" + srcType);
                this.assertParamFileContains(TestCase.this.resultFile(subDir, "option/convert.param"),
                                             "-src.srcType=" + srcType);
            }

            @ParameterizedTest
            @ValueSource(strings = {"sql", "ant"})
            public void testRunWithScriptType(final String scriptType) throws Exception {
                final String subDir = "parameter/run-script-" + scriptType;
                TestCase.this.scaffold(subDir, "-target=parameter", "-commandType=run",
                                       "-commandInput.scriptType=" + scriptType);
                this.assertParamFileContains(TestCase.this.resultFile(subDir, "option/run.param"),
                                             "-scriptType=" + scriptType);
            }

            @Test
            public void testNoOutputWhenNoTarget() {
                TestCase.this.scaffold("empty");
                assertFalse(TestCase.this.resultFile("empty", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("empty", "resources/setting/javaBean.json").exists());
                assertFalse(TestCase.this.resultFile("empty", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("empty", "resources/template/javaBean.stg").exists());
            }

            private void assertParamFileExists(final File paramFile) throws IOException {
                assertTrue(paramFile.exists());
                assertTrue(Files.size(paramFile.toPath()) > 0);
            }

            private void assertParamFileContains(final File paramFile, final String expected) throws IOException {
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains(expected)));
            }
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

        @Override
        protected String getResultBasePrefix() {
            return TEMP;
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

        @Override
        protected String getResultBasePrefix() {
            return TEMP;
        }
    }
}
