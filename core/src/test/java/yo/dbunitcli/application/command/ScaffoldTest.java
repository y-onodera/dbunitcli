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
                TestCase.this.scaffold("ddl/all", "-target=ddl", "-unitSetting=ddlUnit",
                                       "-template=ddl", "-parameter=ddl");
                assertTrue(TestCase.this.resultFile("ddl/all", "resources/setting/ddlUnit.json").exists());
                assertTrue(TestCase.this.resultFile("ddl/all", "resources/template/ddl.stg").exists());
                assertTrue(TestCase.this.resultFile("ddl/all", "resources/template/ddl.txt").exists());
                final File paramFile = TestCase.this.resultFile("ddl/all", "option/ddl.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unit=table")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-template=resources/template/ddl.txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("templateGroup=resources/template/ddl.stg")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unitSetting=resources/setting/ddlUnit.json")));
                assertFalse(lines.stream().anyMatch(l -> l.startsWith("-setting=")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-resultPath=$param.tableName$.sql")));
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
                assertFalse(lines.stream().anyMatch(l -> l.contains("-unitSetting=")));
                assertFalse(lines.stream().anyMatch(l -> l.contains("-resultPath=")));
            }

            @Test
            public void testCustomFileName() throws Exception {
                TestCase.this.scaffold("ddl/custom", "-target=ddl", "-unitSetting=myDdl", "-template=myDdl",
                                       "-parameter=myDdl");
                assertTrue(TestCase.this.resultFile("ddl/custom", "resources/setting/myDdl.json").exists());
                assertTrue(TestCase.this.resultFile("ddl/custom", "resources/template/myDdl.stg").exists());
                assertTrue(TestCase.this.resultFile("ddl/custom", "resources/template/myDdl.txt").exists());
                final File paramFile = TestCase.this.resultFile("ddl/custom", "option/myDdl.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unit=table")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-template=resources/template/myDdl.txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("templateGroup=resources/template/myDdl.stg")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unitSetting=resources/setting/myDdl.json")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-resultPath=$param.tableName$.sql")));
            }
        }

        @Nested
        class JavaBeanTarget {

            @Test
            public void testAllFiles() throws Exception {
                TestCase.this.scaffold("javaBean/all", "-target=javaBean",
                                       "-unitSetting=javaBeanUnit", "-template=javaBean", "-parameter=javaBean");
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/setting/javaBeanUnit.json").exists());
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/template/javaBean.stg").exists());
                assertTrue(TestCase.this.resultFile("javaBean/all", "resources/template/javaBean.txt").exists());
                final File paramFile = TestCase.this.resultFile("javaBean/all", "option/javaBean.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unit=table")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-template=resources/template/javaBean.txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("templateGroup=resources/template/javaBean.stg")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unitSetting=resources/setting/javaBeanUnit.json")));
                assertTrue(lines.stream()
                                   .anyMatch(l -> l.contains(
                                           "-resultPath=$param.tableName; format=\"snakeToUpperCamel\"$.java")));
            }

            @Test
            public void testCustomFileName() throws Exception {
                TestCase.this.scaffold("javaBean/custom", "-target=javaBean", "-unitSetting=myBean", "-template=myBean",
                                       "-parameter=myBean");
                assertTrue(TestCase.this.resultFile("javaBean/custom", "resources/setting/myBean.json").exists());
                assertTrue(TestCase.this.resultFile("javaBean/custom", "resources/template/myBean.stg").exists());
                assertTrue(TestCase.this.resultFile("javaBean/custom", "resources/template/myBean.txt").exists());
                final File paramFile = TestCase.this.resultFile("javaBean/custom", "option/myBean.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unit=table")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-template=resources/template/myBean.txt")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("templateGroup=resources/template/myBean.stg")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-unitSetting=resources/setting/myBean.json")));
                assertTrue(lines.stream()
                                   .anyMatch(l -> l.contains(
                                           "-resultPath=$param.tableName; format=\"snakeToUpperCamel\"$.java")));
            }
        }

        @Nested
        class XlsxSchemaTarget {

            @Test
            public void testAllFiles() throws Exception {
                TestCase.this.scaffold("xlsxSchema/all", "-target=xlsxSchema", "-unitSetting=xlsxSchema",
                                       "-template=xlsxSchema", "-parameter=xlsxSchema",
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
                assertTrue(lines.stream().anyMatch(l -> l.equals("-src.src=src")));
            }

            @Test
            public void testUnitSettingOnly() {
                TestCase.this.scaffold("xlsxSchema/unitSetting-only", "-target=xlsxSchema",
                                       "-unitSetting=xlsxSchema");
                assertTrue(TestCase.this.resultFile("xlsxSchema/unitSetting-only",
                                                    "resources/setting/xlsxSchema.json").exists());
                assertFalse(TestCase.this.resultFile("xlsxSchema/unitSetting-only",
                                                     "resources/template/xlsxSchema.stg").exists());
                assertFalse(TestCase.this.resultFile("xlsxSchema/unitSetting-only", "src").exists());
                assertFalse(TestCase.this.resultFile("xlsxSchema/unitSetting-only", "option/xlsxSchema.param")
                                    .exists());
            }

            @Test
            public void testTemplateOnly() {
                TestCase.this.scaffold("xlsxSchema/template-only", "-target=xlsxSchema", "-template=xlsxSchema");
                assertTrue(TestCase.this.resultFile("xlsxSchema/template-only",
                                                    "resources/template/xlsxSchema.stg").exists());
                assertTrue(TestCase.this.resultFile("xlsxSchema/template-only",
                                                    "resources/template/xlsxSchema.txt").exists());
                assertFalse(TestCase.this.resultFile("xlsxSchema/template-only",
                                                     "resources/setting/xlsxSchema.json").exists());
                assertFalse(TestCase.this.resultFile("xlsxSchema/template-only", "option/xlsxSchema.param")
                                    .exists());
            }

            @Test
            public void testDatasetOnly() {
                TestCase.this.scaffold("xlsxSchema/dataset-only", "-target=xlsxSchema",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                assertTrue(TestCase.this.resultFile("xlsxSchema/dataset-only", "src/SAMPLE.csv").exists());
                assertFalse(TestCase.this.resultFile("xlsxSchema/dataset-only",
                                                     "resources/template/xlsxSchema.stg").exists());
                assertFalse(TestCase.this.resultFile("xlsxSchema/dataset-only",
                                                     "resources/setting/xlsxSchema.json").exists());
                assertFalse(TestCase.this.resultFile("xlsxSchema/dataset-only", "option/xlsxSchema.param").exists());
            }

            @Test
            public void testBuiltinParamReferencesOriginalDataset() throws Exception {
                TestCase.this.scaffold("xlsxSchema/builtin-param", "-target=xlsxSchema", "-parameter=xlsxSchema",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File paramFile = TestCase.this.resultFile("xlsxSchema/builtin-param", "option/xlsxSchema.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=xlsxSchema")));
                assertFalse(lines.stream().anyMatch(l -> l.startsWith("-template=")));
                assertFalse(lines.stream().anyMatch(l -> l.startsWith("-resultPath=")));
                // 組み込みgenerateTypeは元ソースから記述子行を合成するため、元datasetの絶対パスを参照する
                assertTrue(lines.stream().anyMatch(l -> l.startsWith("-src.src=")
                        && l.replace('\\', '/').endsWith("scaffold/src")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-src.srcType=csv")));
            }

            @Test
            public void testParameterWithoutDataset() throws Exception {
                TestCase.this.scaffold("xlsxSchema/no-dataset", "-target=xlsxSchema", "-parameter=xlsxSchema");
                assertFalse(TestCase.this.resultFile("xlsxSchema/no-dataset", "src").exists());
                assertFalse(TestCase.this.resultFile("xlsxSchema/no-dataset", "resources/template/xlsxSchema.stg")
                                    .exists());
                final File paramFile = TestCase.this.resultFile("xlsxSchema/no-dataset", "option/xlsxSchema.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=xlsxSchema")));
                assertFalse(lines.stream().anyMatch(l -> l.startsWith("-src.")));
            }

            @Test
            public void testCustomUnitSettingName() throws Exception {
                TestCase.this.scaffold("xlsxSchema/unitSetting", "-target=xlsxSchema", "-parameter=xlsxSchema",
                                       "-template=xlsxSchema", "-unitSetting=xlsxSchemaUnit",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
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
            public void testAllFiles() throws Exception {
                TestCase.this.scaffold("fixedColumnDef/all", "-target=fixedColumnDef",
                                       "-unitSetting=fixedColumnDef", "-template=fixedColumnDef",
                                       "-parameter=fixedColumnDef",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File srcFile = TestCase.this.resultFile("fixedColumnDef/all", "src/SAMPLE.csv");
                assertTrue(srcFile.exists());
                final List<String> srcLines = Files.readAllLines(srcFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(srcLines.stream().anyMatch(l -> l.contains("\"name\",\"length\",\"align\",\"pad\"")));
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
            public void testBuiltinParamReferencesOriginalDataset() throws Exception {
                TestCase.this.scaffold("fixedColumnDef/builtin-param", "-target=fixedColumnDef",
                                       "-parameter=fixedColumnDef",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File paramFile = TestCase.this.resultFile("fixedColumnDef/builtin-param",
                                                                "option/fixedColumnDef.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=fixedColumnDef")));
                assertTrue(lines.stream().anyMatch(l -> l.startsWith("-src.src=")
                        && l.replace('\\', '/').endsWith("scaffold/src")));
            }

            @Test
            public void testParameterWithoutDataset() throws Exception {
                TestCase.this.scaffold("fixedColumnDef/no-dataset", "-target=fixedColumnDef",
                                       "-parameter=fixedColumnDef");
                assertFalse(TestCase.this.resultFile("fixedColumnDef/no-dataset", "src").exists());
                assertFalse(TestCase.this.resultFile("fixedColumnDef/no-dataset",
                                                      "resources/template/fixedColumnDef.stg").exists());
                final File paramFile = TestCase.this.resultFile("fixedColumnDef/no-dataset",
                                                                "option/fixedColumnDef.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.contains("-generateType=fixedColumnDef")));
                assertFalse(lines.stream().anyMatch(l -> l.startsWith("-src.")));
            }

            @Test
            public void testCustomUnitSettingName() {
                TestCase.this.scaffold("fixedColumnDef/unitSetting", "-target=fixedColumnDef",
                                       "-parameter=fixedColumnDef", "-unitSetting=fixedColumnDefUnit",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                assertTrue(TestCase.this.resultFile("fixedColumnDef/unitSetting",
                                                     "resources/setting/fixedColumnDefUnit.json").exists());
            }

            @Test
            public void testLengthAndAlignOptions() throws Exception {
                TestCase.this.scaffold("fixedColumnDef/length", "-target=fixedColumnDef",
                                       "-parameter=fixedColumnDef", "-fixedLength=5",
                                       "-defaultLength=20", "-align=right",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File srcFile = TestCase.this.resultFile("fixedColumnDef/length", "src/SAMPLE.csv");
                assertTrue(srcFile.exists());
                final List<String> srcLines = Files.readAllLines(srcFile.toPath(), StandardCharsets.UTF_8);
                // 先頭列(id)は-fixedLengthの5、以降の列は-defaultLengthの20
                assertTrue(srcLines.stream().anyMatch(l -> l.contains("\"id\",\"5\",\"right\"")));
                assertTrue(srcLines.stream().anyMatch(l -> l.contains("\"name\",\"20\",\"right\"")));
                final List<String> lines = Files.readAllLines(
                        TestCase.this.resultFile("fixedColumnDef/length", "option/fixedColumnDef.param").toPath(),
                        StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.equals("-fixedLength=5")));
                assertTrue(lines.stream().anyMatch(l -> l.equals("-defaultLength=20")));
                assertTrue(lines.stream().anyMatch(l -> l.equals("-align=right")));
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
                // 組み込みgenerateTypeは元ソースから記述子行を合成するため、元datasetの絶対パスを参照する
                assertTrue(lines.stream().anyMatch(l -> l.startsWith("-src.src=")
                        && l.replace('\\', '/').endsWith("scaffold/src")));
                assertTrue(lines.stream().anyMatch(l -> l.contains("-src.srcType=csv")));
            }

            @Test
            public void testDatasetParamFileWithTemplateReferencesScaffoldSrc() throws Exception {
                TestCase.this.scaffold("dataset/ddl-param-template", "-target=ddl", "-parameter=ddl",
                                       "-template=ddl", "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File paramFile = TestCase.this.resultFile("dataset/ddl-param-template", "option/ddl.param");
                assertTrue(paramFile.exists());
                final List<String> lines = Files.readAllLines(paramFile.toPath(), StandardCharsets.UTF_8);
                assertTrue(lines.stream().anyMatch(l -> l.equals("-src.src=src")));
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
            public void testDatasetTypeFormatRejected() {
                final org.junit.jupiter.api.function.Executable scaffold = () ->
                        TestCase.this.scaffold("dataset/ddl-format", "-target=ddl",
                                               "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv",
                                               "-datasetType=format");
                final AssertionError error = org.junit.jupiter.api.Assertions.assertThrows(AssertionError.class, scaffold);
                assertTrue(error.getMessage().contains("format"));
            }

            @Test
            public void testNoDatasetOptionNoSrcDir() {
                TestCase.this.scaffold("dataset/no-dataset", "-target=ddl", "-parameter=ddl");
                assertFalse(TestCase.this.resultFile("dataset/no-dataset", "src").exists());
            }

            @Test
            public void testDatasetEncodingInParamFile() throws Exception {
                TestCase.this.scaffold("dataset/ddl-encoding", "-target=ddl", "-parameter=ddl", "-template=ddl",
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
            public void testDdlScaffoldWithTemplateMatchesBuiltIn() throws Exception {
                this.assertScaffoldWithTemplateMatchesBuiltIn("ddl", "e2e/ddlTemplate", "ddl/SAMPLE.sql",
                                                               "SAMPLE.sql");
            }

            @Test
            public void testJavaBeanScaffoldWithTemplateMatchesBuiltIn() throws Exception {
                this.assertScaffoldWithTemplateMatchesBuiltIn("javaBean", "e2e/javaBeanTemplate",
                                                               "javaBean/Sample.java", "Sample.java");
            }

            @Test
            public void testXlsxSchemaScaffoldWithTemplateMatchesBuiltIn() throws Exception {
                this.assertScaffoldWithTemplateMatchesBuiltIn("xlsxSchema", "e2e/xlsxSchemaTemplate",
                                                               "xlsxSchema/SAMPLE.json", "SAMPLE.json");
            }

            @Test
            public void testFixedColumnDefScaffoldWithTemplateMatchesBuiltIn() throws Exception {
                this.assertScaffoldWithTemplateMatchesBuiltIn("fixedColumnDef", "e2e/fixedColumnDefTemplate",
                                                               "fixedColumnDef/SAMPLE.json", "SAMPLE.json");
            }

            @Test
            public void testXlsxSchemaScaffoldToGenerate() throws Exception {
                final String subDir = "e2e/xlsxSchema";
                TestCase.this.scaffold(subDir, "-target=xlsxSchema", "-parameter=xlsxSchema",
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File paramFile = TestCase.this.resultFile(subDir, "option/xlsxSchema.param");
                final File resultFile = this.runGenerate(subDir, paramFile, "xlsxSchema/SAMPLE.json");
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
                final File resultFile = this.runGenerate(subDir, paramFile, "fixedColumnDef/SAMPLE.json");
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

            private void assertScaffoldWithTemplateMatchesBuiltIn(final String target, final String subDir,
                                                                   final String builtInOutput,
                                                                   final String templateOutput) throws Exception {
                final String builtInDir = subDir + "/builtin";
                final String templateDir = subDir + "/template";
                TestCase.this.scaffold(builtInDir, "-target=" + target, "-unitSetting=" + target + "Unit",
                                       "-parameter=" + target, "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                TestCase.this.scaffold(templateDir, "-target=" + target, "-unitSetting=" + target + "Unit",
                                       "-template=" + target, "-parameter=" + target,
                                       "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
                final File builtInParam = TestCase.this.resultFile(builtInDir, "option/" + target + ".param");
                final File templateParam = TestCase.this.resultFile(templateDir, "option/" + target + ".param");
                final File builtInResult = this.runGenerate(builtInDir, builtInParam, builtInOutput);
                final File templateResult = this.runGenerate(templateDir, templateParam, templateOutput);
                assertEquals(Files.readString(builtInResult.toPath()), Files.readString(templateResult.toPath()));
            }

            private void assertScaffoldGenerates(final String target, final String subDir,
                                                  final String expectedOutput) throws Exception {
                TestCase.this.scaffold(subDir, "-target=" + target,
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
    class TxtDrivenGenerateParamsTest {

        private static final String SRC_DIR =
                "src/test/resources/yo/dbunitcli/application/command/scaffold/src";

        private List<String> txtDrivenParams(final String... args) {
            return new Scaffold().parseOption(args).txtDrivenGenerateParams().toList(false);
        }

        @Test
        public void testDdlWithCsvDataset() {
            final List<String> lines = this.txtDrivenParams("-target=ddl", "-template=myDdl",
                                                            "-unitSetting=myUnit",
                                                            "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv");
            assertEquals(List.of("-generateType=txt"
                    , "-unit=table"
                    , "-template=resources/template/myDdl.txt"
                    , "-template.templateGroup=resources/template/myDdl.stg"
                    , "-resultPath=$param.tableName$.sql"
                    , "-unitSetting=resources/setting/myUnit.json"
                    , "-src.src=src"
                    , "-src.srcType=csv"
                    , "-encoding=UTF-8"), lines);
        }

        @Test
        public void testFixedColumnDefWithFixedDatasetAddsHeaderName() {
            final List<String> lines = this.txtDrivenParams("-target=fixedColumnDef", "-template=myFixed",
                                                            "-dataset.src=" + SRC_DIR, "-dataset.srcType=csv",
                                                            "-datasetType=fixed", "-datasetEncoding=Shift_JIS");
            assertTrue(lines.contains("-src.srcType=fixed"));
            assertTrue(lines.contains("-encoding=Shift_JIS"));
            assertTrue(lines.contains("-headerName=name,length,align,pad"));
            assertTrue(lines.contains("-resultPath=$param.tableName$.json"));
        }

        @Test
        public void testWithoutUnitSettingAndDataset() {
            final List<String> lines = this.txtDrivenParams("-target=ddl", "-template=myDdl");
            assertEquals(List.of("-generateType=txt"
                    , "-unit=table"
                    , "-template=resources/template/myDdl.txt"
                    , "-template.templateGroup=resources/template/myDdl.stg"
                    , "-resultPath=$param.tableName$.sql"), lines);
        }

        @Test
        public void testResultDirNotIncluded() {
            final List<String> lines = this.txtDrivenParams("-target=ddl", "-template=myDdl",
                                                            "-result=target/test-temp/scaffold/txt-params");
            assertFalse(lines.stream().anyMatch(l -> l.startsWith("-result=")));
        }

        @Test
        public void testTemplateNameRequired() {
            org.junit.jupiter.api.Assertions.assertThrows(AssertionError.class,
                                                          () -> this.txtDrivenParams("-target=ddl"));
        }

        @Test
        public void testParameterTargetRejected() {
            org.junit.jupiter.api.Assertions.assertThrows(AssertionError.class,
                                                          () -> this.txtDrivenParams("-target=parameter",
                                                                                     "-template=myDdl"));
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
