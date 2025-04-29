package yo.dbunitcli.application;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;
import org.junit.jupiter.api.*;
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

        @Test
        public void testGenerateXlsx() {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateXlsx.txt"});
            Compare.main(new String[]{"@" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/xlsx/compareResult.txt"
                    , "-old.src=" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/Test1"
                    , "-new.src=" + GenerateTest.baseDir + "/generate/table/result/xlsx/Test1.xlsx"
                    , "-result=" + GenerateTest.baseDir + "/generate/table/result/xlsx/Test1"
            });
        }

        @Test
        public void testGenerateXlsxWithoutFormulaEvaluation() {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateXlsxWithoutEvaluate.txt"});
            Compare.main(new String[]{"@" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/xlsx/compareResult.txt"
                    , "-old.src=" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/Test1"
                    , "-new.src=" + GenerateTest.baseDir + "/generate/table/result/xlsx/Test1_withoutEvaluate.xlsx"
                    , "-result=" + GenerateTest.baseDir + "/generate/table/result/xlsx/Test1WithoutEvaluate"
            });
        }

        @Test
        public void testGenerateXlsxStreaming() {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateXlsxStreaming.txt"});
            Compare.main(new String[]{"@" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/streamxlsx/compareResult.txt"
                    , "-old.src=" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/Test1"
                    , "-new.src=" + GenerateTest.baseDir + "/generate/table/result/xlsx/Test1_Stream.xlsx"
                    , "-result=" + GenerateTest.baseDir + "/generate/table/result/xlsx/Test1Stream"
            });
        }

        @Test
        public void testGenerateXls() {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateXls.txt"});
            Compare.main(new String[]{"@" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/xls/compareResult.txt"
                    , "-old.src=" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/Test1"
                    , "-new.src=" + GenerateTest.baseDir + "/generate/table/result/xls/Test1.xls"
                    , "-result=" + GenerateTest.baseDir + "/generate/table/result/xls/Test1"
            });
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
