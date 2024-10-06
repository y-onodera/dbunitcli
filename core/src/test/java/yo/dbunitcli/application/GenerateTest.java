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
    private static String testResourcesDir;
    private static String baseDir;
    private static String subDirectory;

    @BeforeAll
    public static void setUp() throws UnsupportedEncodingException {
        GenerateTest.baseDir = URLDecoder.decode(Objects.requireNonNull(GenerateTest.class.getResource(".")).getPath(), StandardCharsets.UTF_8);
        GenerateTest.testResourcesDir = GenerateTest.baseDir.replace("target/test-classes", "src/test/resources");
        GenerateTest.PROJECT.setName("generateTest");
        GenerateTest.PROJECT.setBaseDir(new File("."));
        GenerateTest.PROJECT.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        GenerateTest.backup.putAll(System.getProperties());
    }

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

    abstract static class TestCase {

        @Test
        public void testGenerateXlsx() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateXlsx.txt"});
            Compare.main(new String[]{"@" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/xlsx/compareResult.txt"
                    , "-old.src=" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/Test1"
                    , "-new.src=" + GenerateTest.baseDir + "/generate/table/result/xlsx/Test1.xlsx"
                    , "-result=" + GenerateTest.baseDir + "/generate/table/result/xlsx/Test1"
            });
        }

        @Test
        public void testGenerateXlsxStreaming() throws Exception {
            Generate.main(new String[]{"@" + GenerateTest.testResourcesDir + "/paramGenerateXlsxStreaming.txt"});
            Compare.main(new String[]{"@" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/streamxlsx/compareResult.txt"
                    , "-old.src=" + GenerateTest.testResourcesDir + "/expect/generate/table/expect/Test1"
                    , "-new.src=" + GenerateTest.baseDir + "/generate/table/result/xlsx/Test1_Stream.xlsx"
                    , "-result=" + GenerateTest.baseDir + "/generate/table/result/xlsx/Test1Stream"
            });
        }

        @Test
        public void testGenerateXls() throws Exception {
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
            return GenerateTest.baseDir + GenerateTest.subDirectory + "/result";
        }
    }

    @Nested
    class NoSystemPropertyTest extends GenerateTest.TestCase {
    }

    @Nested
    class AllSystemPropertyTest extends GenerateTest.TestCase {

        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(GenerateTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "target/test-temp/generate/all/base");
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "target/test-temp/generate/all/result");
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "target/test-temp/generate/all/dataset");
            System.setProperties(newProperty);
            GenerateTest.clean("target/test-temp/generate/all");
            GenerateTest.copy("src/test/resources/yo/dbunitcli/application/settings", "target/test-temp/generate/all/base/src/test/resources/yo/dbunitcli/application/settings");
            GenerateTest.copy("src/test/resources/yo/dbunitcli/application/src", "target/test-temp/generate/all/dataset/src/test/resources/yo/dbunitcli/application/src");
            GenerateTest.copy("src/test/resources/yo/dbunitcli/application/expect", "target/test-temp/generate/all/dataset/src/test/resources/yo/dbunitcli/application/expect");
        }

        @AfterAll
        static void restore() {
            System.setProperties(GenerateTest.backup);
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/target/test-temp/generate/all/result/target/");
        }
    }

    @Nested
    class ChangeWorkSpaceTest extends GenerateTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(GenerateTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "target/test-temp/generate/base");
            System.setProperties(newProperty);
            GenerateTest.clean("target/test-temp/generate/base");
            GenerateTest.copy("src/test/resources/yo/dbunitcli/application", "target/test-temp/generate/base/src/test/resources/yo/dbunitcli/application");
        }

        @AfterAll
        static void restore() {
            System.setProperties(GenerateTest.backup);
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/target/test-temp/generate/base/target/");
        }
    }

    @Nested
    class ChangeResultBaseTest extends GenerateTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(GenerateTest.backup);
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "target/test-temp/generate/result");
            System.setProperties(newProperty);
            GenerateTest.clean("target/test-temp/generate/result");
        }

        @AfterAll
        static void restore() {
            System.setProperties(GenerateTest.backup);
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/target/test-temp/generate/result/target/");
        }
    }

    @Nested
    class ChangeDataSetBaseTest extends GenerateTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(GenerateTest.backup);
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "target/test-temp/generate/dataset");
            System.setProperties(newProperty);
            GenerateTest.clean("target/test-temp/generate/dataset");
            GenerateTest.copy("src/test/resources/yo/dbunitcli/application/src", "target/test-temp/generate/dataset/src/test/resources/yo/dbunitcli/application/src");
        }

        @AfterAll
        static void restore() {
            System.setProperties(GenerateTest.backup);
        }
    }

}
