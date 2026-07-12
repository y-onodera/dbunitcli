package yo.dbunitcli.application.command;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        private static final String SRC_DIR =
                "src/test/resources/yo/dbunitcli/application/command/scaffold/src";

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
                TestCase.this.scaffold("ddl/all", "-target=ddl", "-setting=ddl", "-unitSetting=ddlUnit",
                                       "-template=ddl", "-parameter=ddl");
                assertTrue(TestCase.this.resultFile("ddl/all", "resources/setting/ddl.json").exists());
                assertTrue(TestCase.this.resultFile("ddl/all", "resources/setting/ddlUnit.json").exists());
                assertFalse(TestCase.this.resultFile("ddl/all", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("ddl/all", "resources/template/ddl.txt").exists());
                final File paramFile = TestCase.this.resultFile("ddl/all", "option/ddl.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=ddl")));
                assertFalse(lines.stream().anyMatch(l -> l.contains("-template=")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-setting=resources/setting/ddl.json")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unitSetting=resources/setting/ddlUnit.json")));
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
            public void testUnitSettingOnly() {
                TestCase.this.scaffold("ddl/unitSetting", "-target=ddl", "-unitSetting=ddlUnit");
                assertTrue(TestCase.this.resultFile("ddl/unitSetting", "resources/setting/ddlUnit.json").exists());
                assertFalse(TestCase.this.resultFile("ddl/unitSetting", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("ddl/unitSetting", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("ddl/unitSetting", "option/ddl.param").exists());
            }

            @Test
            public void testTemplateOptionIsIgnored() {
                TestCase.this.scaffold("ddl/template", "-target=ddl", "-template=ddl");
                assertFalse(TestCase.this.resultFile("ddl/template", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("ddl/template", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("ddl/template", "resources/template/ddl.txt").exists());
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
                assertFalse(lines.stream().anyMatch(l -> l.contains("-unitSetting=")));
            }

            @Test
            public void testSettingAndTemplate() {
                TestCase.this.scaffold("ddl/setting-template", "-target=ddl", "-setting=ddl", "-template=ddl");
                assertTrue(TestCase.this.resultFile("ddl/setting-template", "resources/setting/ddl.json").exists());
                assertFalse(TestCase.this.resultFile("ddl/setting-template", "resources/template/ddl.stg").exists());
                assertFalse(TestCase.this.resultFile("ddl/setting-template", "resources/template/ddl.txt").exists());
                assertFalse(TestCase.this.resultFile("ddl/setting-template", "option/ddl.param").exists());
            }

            @Test
            public void testCustomFileName() {
                TestCase.this.scaffold("ddl/custom", "-target=ddl", "-setting=myDdl", "-template=myDdl",
                                       "-parameter=myDdl");
                assertTrue(TestCase.this.resultFile("ddl/custom", "resources/setting/myDdl.json").exists());
                assertFalse(TestCase.this.resultFile("ddl/custom", "resources/template/myDdl.stg").exists());
                assertFalse(TestCase.this.resultFile("ddl/custom", "resources/template/myDdl.txt").exists());
                assertTrue(TestCase.this.resultFile("ddl/custom", "option/myDdl.param").exists());
            }
        }

        @Nested
        class JavaBeanTarget {

            @Test
            public void testAllFiles() throws Exception {
                TestCase.this.scaffold("javaBean/all", "-target=javaBean", "-setting=javaBean",
                                       "-unitSetting=javaBeanUnit", "-template=javaBean", "-parameter=javaBean");
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/setting/javaBean.json").exists());
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/setting/javaBeanUnit.json").exists());
                assertFalse(TestCase.this.resultFile("javaBean/all", "resources/template/javaBean.stg").exists());
                assertFalse(TestCase.this.resultFile("javaBean/all", "resources/template/javaBean.txt").exists());
                final File paramFile = TestCase.this.resultFile("javaBean/all", "option/javaBean.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertFalse(lines.stream().anyMatch(l -> l.contains("-template=")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-setting=resources/setting/javaBean.json")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unitSetting=resources/setting/javaBeanUnit.json")));
            }

            @Test
            public void testCustomFileName() {
                TestCase.this.scaffold("javaBean/custom", "-target=javaBean", "-setting=myBean", "-template=myBean",
                                       "-parameter=myBean");
                assertTrue(TestCase.this.resultFile("javaBean/custom", "resources/setting/myBean.json").exists());
                assertFalse(TestCase.this.resultFile("javaBean/custom", "resources/template/myBean.stg").exists());
                assertFalse(TestCase.this.resultFile("javaBean/custom", "resources/template/myBean.txt").exists());
                assertTrue(TestCase.this.resultFile("javaBean/custom", "option/myBean.param").exists());
            }
        }

        @Nested
        class XlsxSchemaTarget {

            @Test
            public void testDatasetGeneratesSrcTemplateSettingAndParam() throws Exception {
                TestCase.this.scaffold("xlsxSchema/all", "-target=xlsxSchema", "-parameter=xlsxSchema",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File srcFile = TestCase.this.resultFile("xlsxSchema/all", "src/SAMPLE.csv");
                assertTrue(srcFile.exists());
                final List<String> srcLines = Files.readAllLines(srcFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(srcLines.stream().anyMatch(l -> l.contains("COLUMN_NAME")));
                assertTrue(srcLines.stream().anyMatch(l -> l.contains("id")));
                assertTrue(TestCase.this.resultFile("xlsxSchema/all", "resources/template/xlsxSchema.stg").exists());
                assertTrue(TestCase.this.resultFile("xlsxSchema/all", "resources/template/xlsxSchema.txt").exists());
                assertTrue(TestCase.this.resultFile("xlsxSchema/all", "resources/setting/xlsxSchema.json").exists());
                final File paramFile = TestCase.this.resultFile("xlsxSchema/all", "option/xlsxSchema.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unit=table")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-template=resources/template/xlsxSchema.txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains(
                        "templateGroup=resources/template/xlsxSchema.stg")));
                assertTrue(lines.stream().anyMatch(l -> l.contains(
                        "-unitSetting=resources/setting/xlsxSchema.json")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-resultPath=$param.tableName$.json")));
            }

            @Test
            public void testNoOutputWithoutDataset() {
                TestCase.this.scaffold("xlsxSchema/no-dataset", "-target=xlsxSchema", "-parameter=xlsxSchema");
                assertFalse(TestCase.this.resultFile("xlsxSchema/no-dataset", "src").exists());
                assertFalse(TestCase.this.resultFile("xlsxSchema/no-dataset", "resources/template/xlsxSchema.stg")
                                    .exists());
                assertFalse(TestCase.this.resultFile("xlsxSchema/no-dataset", "option/xlsxSchema.param").exists());
            }

            @Test
            public void testCustomUnitSettingName() throws Exception {
                TestCase.this.scaffold("xlsxSchema/unitSetting", "-target=xlsxSchema", "-parameter=xlsxSchema",
                                       "-unitSetting=xlsxSchemaUnit", "-dataset.src=" + SRC_DIR,
                                       "-dataset.srcType=csv");
                assertTrue(TestCase.this.resultFile("xlsxSchema/unitSetting", "resources/setting/xlsxSchemaUnit.json")
                                   .exists());
                final List<String> lines = Files.readAllLines(
                        TestCase.this.resultFile("xlsxSchema/unitSetting", "option/xlsxSchema.param").toPath(),
                        StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains(
                        "-unitSetting=resources/setting/xlsxSchemaUnit.json")));
            }
        }

        @Nested
        class FixedColumnDefTarget {

            @Test
            public void testDatasetGeneratesSrcTemplateSettingAndParam() throws Exception {
                TestCase.this.scaffold("fixedColumnDef/all", "-target=fixedColumnDef", "-parameter=fixedColumnDef",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File srcFile = TestCase.this.resultFile("fixedColumnDef/all", "src/SAMPLE.csv");
                assertTrue(srcFile.exists());
                final List<String> srcLines = Files.readAllLines(srcFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(srcLines.stream().anyMatch(l -> l.contains("COLUMN_NAME")));
                assertTrue(srcLines.stream().anyMatch(l -> l.contains("id")));
                assertTrue(TestCase.this.resultFile("fixedColumnDef/all", "resources/template/fixedColumnDef.stg")
                                   .exists());
                assertTrue(TestCase.this.resultFile("fixedColumnDef/all", "resources/template/fixedColumnDef.txt")
                                   .exists());
                assertTrue(TestCase.this.resultFile("fixedColumnDef/all", "resources/setting/fixedColumnDef.json")
                                   .exists());
                final File paramFile = TestCase.this.resultFile("fixedColumnDef/all", "option/fixedColumnDef.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unit=table")));
                assertTrue(lines.stream().anyMatch(l -> l.contains(
                        "-template=resources/template/fixedColumnDef.txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains(
                        "templateGroup=resources/template/fixedColumnDef.stg")));
                assertTrue(lines.stream().anyMatch(l -> l.contains(
                        "-unitSetting=resources/setting/fixedColumnDef.json")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-resultPath=$param.tableName$.json")));
            }

            @Test
            public void testNoOutputWithoutDataset() {
                TestCase.this.scaffold("fixedColumnDef/no-dataset", "-target=fixedColumnDef",
                                       "-parameter=fixedColumnDef");
                assertFalse(TestCase.this.resultFile("fixedColumnDef/no-dataset", "src").exists());
                assertFalse(TestCase.this.resultFile("fixedColumnDef/no-dataset",
                                                      "resources/template/fixedColumnDef.stg").exists());
                assertFalse(TestCase.this.resultFile("fixedColumnDef/no-dataset", "option/fixedColumnDef.param")
                                    .exists());
            }

            @Test
            public void testCustomUnitSettingName() {
                TestCase.this.scaffold("fixedColumnDef/unitSetting", "-target=fixedColumnDef",
                                       "-parameter=fixedColumnDef", "-unitSetting=fixedColumnDefUnit",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                assertTrue(TestCase.this.resultFile("fixedColumnDef/unitSetting",
                                                     "resources/setting/fixedColumnDefUnit.json").exists());
            }
        }

        @Nested
        class DatasetOption {

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
                assertTrue(lines.stream().anyMatch(l -> l.contains("-encoding=Shift_JIS")));
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

            @Test
            public void testDdlScaffoldToGenerate() throws Exception {
                this.assertScaffoldGenerates("ddl", "e2e/ddl", "ddl/SAMPLE.sql");
            }

            @Test
            public void testJavaBeanScaffoldToGenerate() throws Exception {
                this.assertScaffoldGenerates("javaBean", "e2e/javaBean", "javaBean/Sample.java");
            }

            @Test
            public void testXlsxSchemaScaffoldToGenerate() throws Exception {
                final String subDir = "e2e/xlsxSchema";
                TestCase.this.scaffold(subDir, "-target=xlsxSchema", "-parameter=xlsxSchema",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File paramFile = TestCase.this.resultFile(subDir, "option/xlsxSchema.param");
                final File resultFile = this.runGenerate(subDir, paramFile, "SAMPLE.json");
                try (final JsonReader reader = Json.createReader(new FileInputStream(resultFile))) {
                    final JsonObject json = reader.readObject();
                    final JsonObject row = json.getJsonArray("rows").getJsonObject(0);
                    assertEquals("SAMPLE", row.getString("tableName"));
                    assertEquals(3, row.getJsonArray("header").size());
                    assertEquals("id", row.getJsonArray("header").getString(0));
                    assertEquals("name", row.getJsonArray("header").getString(1));
                    assertEquals("email", row.getJsonArray("header").getString(2));
                    assertEquals("id", row.getJsonArray("breakKey").getString(0));
                }
            }

            @Test
            public void testFixedColumnDefScaffoldToGenerate() throws Exception {
                final String subDir = "e2e/fixedColumnDef";
                TestCase.this.scaffold(subDir, "-target=fixedColumnDef", "-parameter=fixedColumnDef",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File paramFile = TestCase.this.resultFile(subDir, "option/fixedColumnDef.param");
                final File resultFile = this.runGenerate(subDir, paramFile, "SAMPLE.json");
                try (final JsonReader reader = Json.createReader(new FileInputStream(resultFile))) {
                    final JsonObject json = reader.readObject();
                    assertEquals(3, json.getJsonArray("columns").size());
                    assertEquals("id", json.getJsonArray("columns").getJsonObject(0).getString("name"));
                    assertEquals(10, json.getJsonArray("columns").getJsonObject(0).getInt("length"));
                }
            }

            private File runGenerate(final String subDir, final File paramFile,
                                      final String expectedOutput) throws Exception {
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
                final File resultFile = new File(scaffoldDir, expectedOutput);
                assertTrue(resultFile.exists());
                return resultFile;
            }

            private void assertScaffoldGenerates(final String target, final String subDir,
                                                  final String expectedOutput) throws Exception {
                TestCase.this.scaffold(subDir, "-target=" + target, "-setting=" + target,
                                       "-unitSetting=" + target + "Unit", "-parameter=" + target,
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File paramFile = TestCase.this.resultFile(subDir, "option/" + target + ".param");
                assertTrue(paramFile.exists());
                assertTrue(TestCase.this.resultFile(subDir, "resources/setting/" + target + "Unit.json").exists());

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
