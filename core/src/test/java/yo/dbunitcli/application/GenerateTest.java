package yo.dbunitcli.application;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

public class GenerateTest {
    private static final Project PROJECT = new Project();
    private static final Properties backup = new Properties();
    private static final String RESOURCES_DIR = "src/test/resources/yo/dbunitcli/application";
    private static final String TEMP_DIR = "target/test-temp/generate";
    private static String testResourcesDir;
    private static String baseDir;
    private static String subDirectory;

    private static void copy(final String from, final String to) {
        final Copy copy = new Copy();
        copy.setProject(GenerateTest.PROJECT);
        final FileSet src = new FileSet();
        src.setDir(new File(from));
        copy.addFileset(src);
        copy.setTodir(new File(to));
        copy.execute();
    }

    private static void clean(final String target) {
        final Delete delete = new Delete();
        delete.setProject(GenerateTest.PROJECT);
        delete.setDir(new File(target));
        delete.execute();
    }

    @BeforeAll
    public static void setUp() throws UnsupportedEncodingException {
        GenerateTest.baseDir = URLDecoder.decode(Objects.requireNonNull(GenerateTest.class.getResource(".")).getPath(), StandardCharsets.UTF_8);
        GenerateTest.testResourcesDir = GenerateTest.baseDir.replace("target/test-classes", "src/test/resources");
        GenerateTest.PROJECT.setName("generateTest");
        GenerateTest.PROJECT.setBaseDir(new File("."));
        GenerateTest.PROJECT.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        GenerateTest.backup.putAll(System.getProperties());
    }

    abstract static class TestCase {

        @AfterAll
        static void restore() {
            System.setProperties(backup);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void testGenerateXlsx(boolean formulaProcess) throws Exception {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";
            String compareResultSuffix = formulaProcess ? "" : "WithoutFormulaProcess";

            if (formulaProcess) {
                Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateXlsx.txt"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/table/result/xlsx/$param.tableName$.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "@" + GenerateTest.testResourcesDir + "/paramGenerateXlsx.txt"
                        , "-formulaProcess=false"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/table/result/xlsx/$param.tableName$_withoutFormulaProcess.xlsx"
                });
            }
            Compare.main(new String[]{"@" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/xlsx/compareResult.txt"
                    , "-old.src=" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/Test1"
                    , "-new.src=" + this.getBaseDir() + "/generate/table/result/xlsx/Test1" + resultSuffix + ".xlsx"
                    , "-result=" + this.getBaseDir() + "/generate/table/result/xlsx/Test1" + compareResultSuffix
            });
            this.assertConditionalFormat("/generate/table/result/xlsx/Test1" + resultSuffix + ".xlsx");
        }

        @Test
        public void testGenerateXls() throws IOException {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateXls.txt"});
            Compare.main(new String[]{"@" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/xls/compareResult.txt"
                    , "-old.src=" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/Test1"
                    , "-new.src=" + this.getBaseDir() + "/generate/table/result/xls/Test1.xls"
                    , "-result=" + this.getBaseDir() + "/generate/table/result/xls/Test1"
            });
            this.assertConditionalFormat("/generate/table/result/xls/Test1.xls");
        }

        @Test
        public void testGenerateTxt() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateTxt.txt"});
            GenerateTest.subDirectory = "generate/row";
            this.assertGenerateFileEquals("SomeClassTest.txt", "MS932");
            this.assertGenerateFileEquals("OtherClassTest.txt", "MS932");
            this.assertGenerateFileEquals("AnotherClassTest.txt", "MS932");
        }

        @Test
        public void testGenerateTxtPerTable() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateTxtPerTable.txt"});
            GenerateTest.subDirectory = "generate/table";
            this.assertGenerateFileEquals("Test1.txt", "UTF-8");
            this.assertGenerateFileEquals("Test2.txt", "UTF-8");
            this.assertGenerateFileEquals("Test3.txt", "UTF-8");
        }

        @Test
        public void testGenerateTxtWithMetaData() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateTxtWithMetaData.txt"});
            GenerateTest.subDirectory = "generate/with_metadata";
            this.assertGenerateFileEquals("Test1.txt", "UTF-8");
            this.assertGenerateFileEquals("Test2.txt", "UTF-8");
            this.assertGenerateFileEquals("Test3.txt", "UTF-8");
        }

        @Test
        public void testGenerateSettings() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateSettings.txt"});
            GenerateTest.subDirectory = "generate/settings";
            this.assertGenerateFileEquals("settings.json", "UTF-8");
        }

        @Test
        public void testGenerateSettingsWithIncludeAllColumns() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateSettingsWithIncludeAllColumns.txt"});
            GenerateTest.subDirectory = "generate/settings";
            this.assertGenerateFileEquals("settings_with_include.json", "UTF-8");
        }

        @Test
        public void testGenerateSettingsIfNoKeyUseAllColumns() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateSettingsNoKeys.txt"});
            GenerateTest.subDirectory = "generate/settings";
            this.assertGenerateFileEquals("settings_no_keys.json", "UTF-8");
        }

        @Test
        public void testGenerateInsert() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateInsert.txt"});
            GenerateTest.subDirectory = "generate/sql/insert";
            this.assertGenerateFileEquals("Test1.sql", "UTF-8");
            this.assertGenerateFileEquals("Test2.sql", "UTF-8");
            this.assertGenerateFileEquals("Test3.sql", "UTF-8");
        }

        @Test
        public void testGenerateInsertNoCommit() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateInsertNoCommit.txt"});
            GenerateTest.subDirectory = "generate/sql/no-commit";
            this.assertGenerateFileEquals("Insert_Test1_NoCommit.sql", "UTF-8");
            this.assertGenerateFileEquals("Insert_Test2_NoCommit.sql", "UTF-8");
            this.assertGenerateFileEquals("Insert_Test3_NoCommit.sql", "UTF-8");
        }

        @Test
        public void testGenerateDelete() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateDelete.txt"});
            GenerateTest.subDirectory = "generate/sql/delete";
            this.assertGenerateFileEquals("Test1.sql", "UTF-8");
            this.assertGenerateFileEquals("Test2.sql", "UTF-8");
            this.assertGenerateFileEquals("Test3.sql", "UTF-8");
        }

        @Test
        public void testGenerateDeleteNoCommit() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateDeleteNoCommit.txt"});
            GenerateTest.subDirectory = "generate/sql/no-commit";
            this.assertGenerateFileEquals("Delete_Test1_NoCommit.sql", "UTF-8");
            this.assertGenerateFileEquals("Delete_Test2_NoCommit.sql", "UTF-8");
            this.assertGenerateFileEquals("Delete_Test3_NoCommit.sql", "UTF-8");
        }

        @Test
        public void testGenerateCleanInsert() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateCleanInsert.txt"});
            GenerateTest.subDirectory = "generate/sql/clean-insert";
            this.assertGenerateFileEquals("Test1.sql", "UTF-8");
            this.assertGenerateFileEquals("Test2.sql", "UTF-8");
            this.assertGenerateFileEquals("Test3.sql", "UTF-8");
        }

        @Test
        public void testGenerateRefresh() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateRefresh.txt"});
            GenerateTest.subDirectory = "generate/sql/refresh";
            this.assertGenerateFileEquals("Test1.sql", "UTF-8");
            this.assertGenerateFileEquals("Test2.sql", "UTF-8");
            this.assertGenerateFileEquals("Test3.sql", "UTF-8");
        }

        @Test
        public void testGenerateUpdate() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateUpdate.txt"});
            GenerateTest.subDirectory = "generate/sql/update";
            this.assertGenerateFileEquals("Test1.sql", "UTF-8");
            this.assertGenerateFileEquals("Test2.sql", "UTF-8");
            this.assertGenerateFileEquals("Test3.sql", "UTF-8");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void testGenerateFromJoinTable(boolean formulaProcess) {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";
            String compareResultSuffix = formulaProcess ? "" : "WithoutFormulaProcess";

            if (formulaProcess) {
                Generate.main(new String[]{
                        "-srcType=xlsx"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/xlsxwithschema/ComplexLayout.xlsx"
                        , "-xlsxSchema=src/test/resources/yo/dbunitcli/application/settings/xlsxwithschema/ComplexSchemaAddFileInfo.json"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/xlsxwithschema/setting_fileInfo_join.json"
                        , "-settingEncoding=MS932"
                        , "-addFileInfo=true"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/join_table.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_join_table.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "-srcType=xlsx"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/xlsxwithschema/ComplexLayout.xlsx"
                        , "-xlsxSchema=src/test/resources/yo/dbunitcli/application/settings/xlsxwithschema/ComplexSchemaAddFileInfo.json"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/xlsxwithschema/setting_fileInfo_join.json"
                        , "-settingEncoding=MS932"
                        , "-addFileInfo=true"
                        , "-formulaProcess=false"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/join_table.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_join_table_withoutFormulaProcess.xlsx"
                });
            }
            Compare.main(new String[]{
                    "-src=src/test/resources/yo/dbunitcli/application/expect/generate/with_metadata/join.xlsx"
                    , "-old.srcType=xlsx"
                    , "-new.src=" + this.getBaseDir() + "generate/with_metadata/result/generated_join_table" + resultSuffix + ".xlsx"
                    , "-new.srcType=xlsx"
                    , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/compare_merge.json"
                    , "-settingEncoding=UTF-8"
                    , "-result=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/compare_join" + compareResultSuffix
            });
        }

        @ParameterizedTest
        @ValueSource(booleans = {true})
        public void testGenerateFromJoinTableDirectionRight(boolean formulaProcess) {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";
            String compareResultSuffix = formulaProcess ? "" : "WithoutFormulaProcess";

            if (formulaProcess) {
                Generate.main(new String[]{
                        "-srcType=xlsx"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/xlsxwithschema/ComplexLayout.xlsx"
                        , "-xlsxSchema=src/test/resources/yo/dbunitcli/application/settings/xlsxwithschema/ComplexSchemaAddFileInfo.json"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/xlsxwithschema/setting_fileInfo_join.json"
                        , "-settingEncoding=MS932"
                        , "-addFileInfo=true"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/join_table_direction_right.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_join_table_direction_right.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "-srcType=xlsx"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/xlsxwithschema/ComplexLayout.xlsx"
                        , "-xlsxSchema=src/test/resources/yo/dbunitcli/application/settings/xlsxwithschema/ComplexSchemaAddFileInfo.json"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/xlsxwithschema/setting_fileInfo_join.json"
                        , "-settingEncoding=MS932"
                        , "-addFileInfo=true"
                        , "-formulaProcess=false"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/join_table_direction_right.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_join_table_direction_right_withoutFormulaProcess.xlsx"
                });
            }
            Compare.main(new String[]{
                    "-src=src/test/resources/yo/dbunitcli/application/expect/generate/with_metadata/direction_right/join.csv"
                    , "-headerName=header,row1,row2,row3,row4,row5"
                    , "-startRow=1"
                    , "-old.srcType=csv"
                    , "-old.encoding=UTF-8"
                    , "-new.src=" + this.getBaseDir() + "generate/with_metadata/result/generated_join_table_direction_right" + resultSuffix + ".xlsx"
                    , "-new.srcType=xlsx"
                    , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/compare_direction_right.json"
                    , "-settingEncoding=UTF-8"
                    , "-result=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/compare_direction_right" + compareResultSuffix
            });
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void testGenerateFromMergeTable(boolean formulaProcess) {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";
            String compareResultSuffix = formulaProcess ? "" : "WithoutFormulaProcess";

            if (formulaProcess) {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/table/source/csv"
                        , "-encoding=MS932"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_table_with_separate.json"
                        , "-addFileInfo=true"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_table.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_merge_table.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/table/source/csv"
                        , "-encoding=MS932"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_table_with_separate.json"
                        , "-addFileInfo=true"
                        , "-formulaProcess=false"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_table.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_merge_table_withoutFormulaProcess.xlsx"
                });
            }
            Compare.main(new String[]{
                    "-src=src/test/resources/yo/dbunitcli/application/expect/generate/with_metadata/separate"
                    , "-old.srcType=csv"
                    , "-old.encoding=UTF-8"
                    , "-new.src=" + this.getBaseDir() + "generate/with_metadata/result/generated_merge_table" + resultSuffix + ".xlsx"
                    , "-new.srcType=xlsx"
                    , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/compare_merge.json"
                    , "-result=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/merge_compare" + compareResultSuffix
            });
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void testGenerateFromMergeTableWithUnitTable(boolean formulaProcess) {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";
            String compareResultSuffix = formulaProcess ? "" : "WithoutFormulaProcess";

            if (formulaProcess) {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/table/source/csv"
                        , "-encoding=MS932"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_table.json"
                        , "-addFileInfo=true"
                        , "-generateType=xlsx"
                        , "-unit=table"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_table_unit_table.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_merge_table_unit_table.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/table/source/csv"
                        , "-encoding=MS932"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_table.json"
                        , "-addFileInfo=true"
                        , "-formulaProcess=false"
                        , "-generateType=xlsx"
                        , "-unit=table"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_table_unit_table.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_merge_table_unit_table_withoutFormulaProcess.xlsx"
                });
            }
            Compare.main(new String[]{
                    "-src=src/test/resources/yo/dbunitcli/application/expect/generate/with_metadata/merge"
                    , "-old.srcType=csv"
                    , "-old.encoding=UTF-8"
                    , "-new.src=" + this.getBaseDir() + "generate/with_metadata/result/generated_merge_table_unit_table" + resultSuffix + ".xlsx"
                    , "-new.srcType=xlsx"
                    , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/compare_merge.json"
                    , "-result=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/compare_unit_table" + compareResultSuffix
            });
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void testGenerateFromMergeAfterJoin(boolean formulaProcess) {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";
            String compareResultSuffix = formulaProcess ? "" : "WithoutFormulaProcess";

            if (formulaProcess) {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/table/source/csv"
                        , "-encoding=MS932"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_after_join.json"
                        , "-addFileInfo=true"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_after_join.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_merge_after_join.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/table/source/csv"
                        , "-encoding=MS932"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_after_join.json"
                        , "-addFileInfo=true"
                        , "-formulaProcess=false"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_after_join.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_merge_after_join_withoutFormulaProcess.xlsx"
                });
            }
            Compare.main(new String[]{
                    "-src=src/test/resources/yo/dbunitcli/application/expect/generate/with_metadata/merge_after_join"
                    , "-old.srcType=csv"
                    , "-old.encoding=UTF-8"
                    , "-new.src=" + this.getBaseDir() + "generate/with_metadata/result/generated_merge_after_join" + resultSuffix + ".xlsx"
                    , "-new.srcType=xlsx"
                    , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/compare_merge_after_join.json"
                    , "-result=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/compare_merge_after_join" + compareResultSuffix
            });
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void testGenerateFromSplitAfterJoin(boolean formulaProcess) {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";
            String compareResultSuffix = formulaProcess ? "" : "WithoutFormulaProcess";

            if (formulaProcess) {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/table/source/csv"
                        , "-encoding=MS932"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/split_after_join.json"
                        , "-addFileInfo=true"
                        , "-generateType=xlsx"
                        , "-unit=table"
                        , "-lazyLoad=false"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/split_after_join.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_split_after_join/$param.tableName$.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/table/source/csv"
                        , "-encoding=MS932"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/split_after_join.json"
                        , "-addFileInfo=true"
                        , "-formulaProcess=false"
                        , "-generateType=xlsx"
                        , "-unit=table"
                        , "-lazyLoad=false"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/split_after_join.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_split_after_join_withoutFormulaProcess/$param.tableName$.xlsx"
                });
            }
            Compare.main(new String[]{
                    "-old.src=src/test/resources/yo/dbunitcli/application/expect/generate/with_metadata/split_after_join"
                    , "-srcType=xlsx"
                    , "-new.src=" + this.getBaseDir() + "generate/with_metadata/result/generated_split_after_join" + resultSuffix
                    , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/compare_split_after_join.json"
                    , "-result=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/compare_split_after_join" + compareResultSuffix
            });
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void testGenerateFromMetaData(boolean formulaProcess) {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";
            String compareResultSuffix = formulaProcess ? "" : "WithoutFormulaProcess";

            if (formulaProcess) {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/table/source/csv"
                        , "-encoding=MS932"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_table_with_separate.json"
                        , "-addFileInfo=true"
                        , "-generateType=xlsx"
                        , "-unit=table"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/metadata.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_from_metadata/$param.tableName$.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/table/source/csv"
                        , "-encoding=MS932"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/merge_table_with_separate.json"
                        , "-addFileInfo=true"
                        , "-formulaProcess=false"
                        , "-generateType=xlsx"
                        , "-unit=table"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/metadata.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/generated_from_metadata_withoutFormulaProcess/$param.tableName$.xlsx"
                });
            }
            Compare.main(new String[]{
                    "-old.src=src/test/resources/yo/dbunitcli/application/expect/generate/with_metadata/generated_from_metadata/tablename.csv"
                    , "-old.srcType=csv"
                    , "-old.encoding=UTF-8"
                    , "-new.src=" + this.getBaseDir() + "generate/with_metadata/result/generated_from_metadata" + resultSuffix
                    , "-new.srcType=xlsx"
                    , "-new.xlsxSchema=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/FromMedadataSchema.json"
                    , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/compare_from_metadata.json"
                    , "-result=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/compare_generated_from_metadata" + compareResultSuffix + "_1"
            });
            Compare.main(new String[]{
                    "-old.src=src/test/resources/yo/dbunitcli/application/expect/generate/with_metadata/generated_from_metadata"
                    , "-old.srcType=csv"
                    , "-old.encoding=UTF-8"
                    , "-old.regExclude=table"
                    , "-new.src=" + this.getBaseDir() + "generate/with_metadata/result/generated_from_metadata" + resultSuffix
                    , "-new.srcType=xlsx"
                    , "-new.startRow=2"
                    , "-new.setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/compare_from_metadata_target.json"
                    , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/compare_from_metadata.json"
                    , "-result=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/compare_generated_from_metadata" + compareResultSuffix + "_2"
            });
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void testGenerateFromSeparateAfterJoin(boolean formulaProcess) {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";
            String compareResultSuffix = formulaProcess ? "" : "WithoutFormulaProcess";

            if (formulaProcess) {
                Generate.main(new String[]{
                        "-srcType=xlsx"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/xlsxwithschema/ComplexLayout.xlsx"
                        , "-xlsxSchema=src/test/resources/yo/dbunitcli/application/settings/xlsxwithschema/ComplexSchemaAddFileInfo.json"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/separate_after_join.json"
                        , "-addFileInfo=true"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/separate_after_join.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/separate_after_join.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "-srcType=xlsx"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/xlsxwithschema/ComplexLayout.xlsx"
                        , "-xlsxSchema=src/test/resources/yo/dbunitcli/application/settings/xlsxwithschema/ComplexSchemaAddFileInfo.json"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/separate_after_join.json"
                        , "-addFileInfo=true"
                        , "-formulaProcess=false"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/separate_after_join.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/separate_after_join_withoutFormulaProcess.xlsx"
                });
            }
            Compare.main(new String[]{
                    "-src=src/test/resources/yo/dbunitcli/application/expect/generate/with_metadata/separate_after_join"
                    , "-old.srcType=csv"
                    , "-old.encoding=UTF-8"
                    , "-new.src=" + this.getBaseDir() + "/generate/with_metadata/result/separate_after_join" + resultSuffix + ".xlsx"
                    , "-new.srcType=xlsx"
                    , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/compare_separate_after_join.json"
                    , "-result=target/test-classes/yo/dbunitcli/application/generate/with_metadata/result/separate_after_join" + compareResultSuffix
            });
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void testGenerateXlsxNotDeleteBlankCell(boolean formulaProcess) throws IOException {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";

            if (formulaProcess) {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/csv/multi/multi1.csv"
                        , "-encoding=UTF-8"
                        , "-generateType=xlsx"
                        , "-unit=table"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/template.xlsx"
                        , "-deleteBlankCells=false"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/NotDeleteBlankCell.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/csv/multi/multi1.csv"
                        , "-encoding=UTF-8"
                        , "-formulaProcess=false"
                        , "-generateType=xlsx"
                        , "-unit=table"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/template.xlsx"
                        , "-deleteBlankCells=false"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/NotDeleteBlankCell_withoutFormulaProcess.xlsx"
                });
            }
            Sheet resultSheet = new XSSFWorkbook(this.getBaseDir() + "/generate/NotDeleteBlankCell" + resultSuffix + ".xlsx").getSheet("multi");
            Row resultRow3 = resultSheet.getRow(3);
            Assertions.assertEquals("3", resultRow3.getCell(0).getStringCellValue());
            Assertions.assertEquals("", resultRow3.getCell(1).getStringCellValue());
            Assertions.assertEquals("", resultRow3.getCell(2).getStringCellValue());
            Assertions.assertEquals("", resultRow3.getCell(3).getStringCellValue());
            Assertions.assertEquals("", resultRow3.getCell(4).getStringCellValue());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void testGenerateXlsxDeleteBlankCell(boolean formulaProcess) throws IOException {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";

            if (formulaProcess) {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/csv/multi/multi1.csv"
                        , "-encoding=UTF-8"
                        , "-generateType=xlsx"
                        , "-unit=table"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/template.xlsx"
                        , "-deleteBlankCells=true"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/DeleteBlankCell.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/csv/multi/multi1.csv"
                        , "-encoding=UTF-8"
                        , "-formulaProcess=false"
                        , "-generateType=xlsx"
                        , "-unit=table"
                        , "-template=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/template.xlsx"
                        , "-deleteBlankCells=true"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/DeleteBlankCell_withoutFormulaProcess.xlsx"
                });
            }
            Sheet resultSheet = new XSSFWorkbook(this.getBaseDir() + "/generate/DeleteBlankCell" + resultSuffix + ".xlsx").getSheet("multi");
            Row resultRow3 = resultSheet.getRow(3);
            Assertions.assertEquals("3", resultRow3.getCell(0).getStringCellValue());
            Assertions.assertNull(resultRow3.getCell(1));
            Assertions.assertNull(resultRow3.getCell(2));
            Assertions.assertEquals("", resultRow3.getCell(3).getStringCellValue());
            Assertions.assertNull(resultRow3.getCell(4));
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void testGenerateXlsxTemplate(boolean formulaProcess) {
            String resultSuffix = formulaProcess ? "" : "_withoutFormulaProcess";
            String compareResultSuffix = formulaProcess ? "" : "WithoutFormulaProcess";

            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateXlsxTemplate.txt"});
            if (formulaProcess) {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/with_metadata/source/csv"
                        , "-encoding=UTF-8"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/settings.json"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=" + this.getBaseDir() + "/generate/xlsxTemplate/result/template.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/xlsxTemplate/result/generated.xlsx"
                });
            } else {
                Generate.main(new String[]{
                        "-srcType=csv"
                        , "-src=src/test/resources/yo/dbunitcli/application/src/generate/with_metadata/source/csv"
                        , "-encoding=UTF-8"
                        , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/settings.json"
                        , "-formulaProcess=false"
                        , "-generateType=xlsx"
                        , "-unit=dataset"
                        , "-template=" + this.getBaseDir() + "/generate/xlsxTemplate/result/template.xlsx"
                        , "-resultPath=target/test-classes/yo/dbunitcli/application/generate/xlsxTemplate/result/generated_withoutFormulaProcess.xlsx"
                });
            }
            Compare.main(new String[]{
                    "-src=src/test/resources/yo/dbunitcli/application/src/generate/with_metadata/source/csv"
                    , "-old.srcType=csv"
                    , "-old.encoding=UTF-8"
                    , "-new.src=" + this.getBaseDir() + "/generate/xlsxTemplate/result/generated" + resultSuffix + ".xlsx"
                    , "-new.srcType=xlsx"
                    , "-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/settings.json"
                    , "-result=target/test-classes/yo/dbunitcli/application/generate/xlsxTemplate/result/compare" + compareResultSuffix
            });
        }

        private void assertGenerateFileEquals(final String target, final String encode) throws IOException {
            final String expect = Files.readString(new File(GenerateTest.testResourcesDir + "expect/" + GenerateTest.subDirectory + "/expect/txt", target).toPath(), Charset.forName(encode));
            final String actual = Files.readString(new File(this.getResult(), target).toPath(), Charset.forName(encode));
            Assertions.assertEquals(expect, actual);
        }

        protected String getResult() {
            return this.getBaseDir() + GenerateTest.subDirectory + "/result";
        }

        protected String getBaseDir() {
            return GenerateTest.baseDir;
        }

        private void assertConditionalFormat(final String fileName) throws IOException {
            try (final Workbook workbook = WorkbookFactory.create(new File(this.getBaseDir() + fileName))) {
                final Sheet sheet2 = workbook.getSheet("シート2");
                final int conditionalFormattingCount = sheet2.getSheetConditionalFormatting().getNumConditionalFormattings();
                Assertions.assertEquals(3, conditionalFormattingCount);
            }
        }

    }

    @Nested
    class NoSystemPropertyTest extends GenerateTest.TestCase {
    }

    @Nested
    class AllSystemPropertyTest extends GenerateTest.TestCase {

        private static final String TOP_DIR = TEMP_DIR + "/all/";

        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(GenerateTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, TOP_DIR + "base");
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, TOP_DIR + "result");
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, TOP_DIR + "dataset");
            System.setProperties(newProperty);
            GenerateTest.clean(TOP_DIR);
            GenerateTest.copy(RESOURCES_DIR + "/settings", TOP_DIR + "base/" + RESOURCES_DIR + "/settings");
            GenerateTest.copy(RESOURCES_DIR + "/src", TOP_DIR + "dataset/" + RESOURCES_DIR + "/src");
            GenerateTest.copy(RESOURCES_DIR + "/expect", TOP_DIR + "dataset/" + RESOURCES_DIR + "/expect");
        }

        protected String getBaseDir() {
            return GenerateTest.baseDir.replaceAll("/target/", "/" + TOP_DIR + "result/target/");
        }
    }

    @Nested
    class ChangeWorkSpaceTest extends GenerateTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(GenerateTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, TEMP_DIR + "/base");
            System.setProperties(newProperty);
            GenerateTest.clean(TEMP_DIR + "/base");
            GenerateTest.copy(RESOURCES_DIR, TEMP_DIR + "/base/" + RESOURCES_DIR);
        }

        @Override
        protected String getBaseDir() {
            return super.getBaseDir().replaceAll("/target/", "/" + TEMP_DIR + "/base/target/");
        }
    }

    @Nested
    class ChangeResultBaseTest extends GenerateTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(GenerateTest.backup);
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, TEMP_DIR + "/result");
            System.setProperties(newProperty);
            GenerateTest.clean(TEMP_DIR + "/result");
        }

        @Override
        protected String getBaseDir() {
            return super.getBaseDir().replaceAll("/target/", "/" + TEMP_DIR + "/result/target/");
        }
    }

    @Nested
    class ChangeDataSetBaseTest extends GenerateTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(GenerateTest.backup);
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, TEMP_DIR + "/dataset");
            System.setProperties(newProperty);
            GenerateTest.clean(TEMP_DIR + "/dataset");
            GenerateTest.copy(RESOURCES_DIR + "/src", TEMP_DIR + "/dataset/" + RESOURCES_DIR + "/src");
        }

    }

}
