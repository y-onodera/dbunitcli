package yo.dbunitcli.application;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit5.JMockitExtension;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.types.FileSet;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public class CompareTest {

    private static final Project PROJECT = new Project();
    private static final Properties backup = new Properties();
    private static String baseDir;

    private static void copy(final String from, final String to) {
        final Copy copy = new Copy();
        copy.setProject(CompareTest.PROJECT);
        final FileSet src = new FileSet();
        src.setDir(new File(from));
        copy.addFileset(src);
        copy.setTodir(new File(to));
        copy.execute();
    }

    private static void clean(final String target) {
        final Delete delete = new Delete();
        delete.setProject(CompareTest.PROJECT);
        delete.setDir(new File(target));
        delete.execute();
    }

    @BeforeAll
    public static void setUp() throws UnsupportedEncodingException {
        CompareTest.baseDir = URLDecoder.decode(Objects.requireNonNull(CompareTest.class.getResource(".")).getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources");
        CompareTest.PROJECT.setName("compareTest");
        CompareTest.PROJECT.setBaseDir(new File("."));
        CompareTest.PROJECT.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        CompareTest.backup.putAll(System.getProperties());
    }

    abstract static class TestCase {
        @AfterAll
        public static void restore() {
            System.setProperties(backup);
        }

        @Test
        public void testSuccessResultDiffExpected() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareResultDiffValidExpected.txt"});
        }

        @Test
        public void testResultXlsx() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareResultDiffXlsx.txt"});
        }

        @Test
        public void testSuccessNoDiffExpected() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareResultNoDiff.txt"});
        }

        @Test
        public void testSuccessNoDiffWithCommonSetting() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareResultNoDiffWithCommonSetting.txt"});
        }

        @Test
        public void testComparePatternMatch() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareOnlyPatternMatch.txt"});
        }

        @Test
        public void testCompareIgnoreNoSettingTables() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareTableNoMatch.txt"});
        }

        @Test
        public void testCompareWithRowNum() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareWithRow.txt"});
        }

        @Test
        public void testCompareWithRowFilter() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareWithFilterRow.txt"});
        }

        @Test
        public void testCompareCsvAndXlsx() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareCsvAndXlsx.txt"});
        }

        @Test
        public void testCompareCsvAndTsv() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareCsvAndTsv.txt"});
        }

        @Test
        public void testCompareXlsxAndCsv() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareXlsxAndCsv.txt"});
        }

        @Test
        public void testCompareXlsxAndXls() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareXlsxAndXls.txt"});
        }

        @Test
        public void testCompareXlsAndXlsx() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareXlsAndXlsx.txt"});
        }

        @Test
        public void testCompareCsvqToCsvq() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareCsvqAndCsvq.txt"});
        }

        @Test
        public void testCompareNoHeaderRegexTxtToCsv() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareNoHeaderRegexTxtAndCsv.txt"});
        }

        @Test
        public void testCompareNoHeaderRegexTxtToNoHeaderCsv() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareNoHeaderRegexTxtAndNoHeaderCsv.txt"});
        }

        @Test
        public void testCompareFilter() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareColumnFilter.txt"});
        }

        @Test
        public void testCompareSort() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareColumnSort.txt"});
        }

        @Test
        public void testComparePdf() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramComparePdf.txt"});
        }

        @Test
        public void testComparePdfDiffAllPage() {
            Assumptions.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("win"));
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramComparePdfDiffAllPage.txt"});
        }

        @Test
        public void testCompareImage() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareImage.txt"});
        }

        @Test
        public void testCompareImageExcludedAreas() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareImageExcludedAreas.txt"});
        }

        @Test
        public void testSettingMerge() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareSettingMerge.txt"});
        }

        @Test
        public void testSettingImport() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareSettingImport.txt"});
        }

        @Test
        public void testExcelWithSchema() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareXlsxAndXlsWithSchema.txt"});
        }
    }

    @Nested
    class NoSystemPropertyTest extends CompareTest.TestCase {
    }

    @Nested
    class AllSystemPropertyTest extends CompareTest.TestCase {

        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(CompareTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "target/test-temp/compare/all/base");
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "target/test-temp/compare/all/result");
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "target/test-temp/compare/all/dataset");
            System.setProperties(newProperty);
            CompareTest.clean("target/test-temp/compare/all");
            CompareTest.copy("src/test/resources/yo/dbunitcli/application/settings", "target/test-temp/compare/all/base/src/test/resources/yo/dbunitcli/application/settings");
            CompareTest.copy("src/test/resources/yo/dbunitcli/application/src", "target/test-temp/compare/all/dataset/src/test/resources/yo/dbunitcli/application/src");
            CompareTest.copy("src/test/resources/yo/dbunitcli/application/expect", "target/test-temp/compare/all/dataset/src/test/resources/yo/dbunitcli/application/expect");
            final Replace replace = new Replace();
            replace.setDir(new File("target/test-temp/compare/all/dataset/src/test/resources/yo/dbunitcli/application/expect"));
            replace.setIncludes("**/*.csv");
            final Replace.Replacefilter filter1 = replace.createReplacefilter();
            filter1.setToken("src/test/resources");
            filter1.setValue("target/test-temp/compare/all/dataset/src/test/resources");
            final Replace.Replacefilter filter2 = replace.createReplacefilter();
            filter2.setToken("src\\\\test\\\\resources");
            filter2.setValue("target\\\\test-temp\\\\compare\\\\all\\\\dataset\\\\src\\\\test\\\\resources");
            replace.setProject(CompareTest.PROJECT);
            replace.execute();
        }

        @Test
        @Override
        public void testResultXlsx() {
            //super.testResultXlsx();
        }
    }

    @Nested
    class ChangeWorkSpaceTest extends CompareTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(CompareTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "target/test-temp/compare/base");
            System.setProperties(newProperty);
            CompareTest.clean("target/test-temp/compare/base");
            CompareTest.copy("src/test/resources/yo/dbunitcli/application", "target/test-temp/compare/base/src/test/resources/yo/dbunitcli/application");
            final Replace replace = new Replace();
            replace.setDir(new File("target/test-temp/compare/base/src/test/resources/yo/dbunitcli/application/expect"));
            replace.setIncludes("**/*.csv");
            final Replace.Replacefilter filter1 = replace.createReplacefilter();
            filter1.setToken("src/test/resources");
            filter1.setValue("target/test-temp/compare/base/src/test/resources");
            final Replace.Replacefilter filter2 = replace.createReplacefilter();
            filter2.setToken("src\\\\test\\\\resources");
            filter2.setValue("target\\\\test-temp\\\\compare\\\\base\\\\src\\\\test\\\\resources");
            replace.setProject(CompareTest.PROJECT);
            replace.execute();
        }

        @Test
        @Override
        public void testResultXlsx() {
            //super.testResultXlsx();
        }

    }

    @Nested
    class ChangeResultBaseTest extends CompareTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(CompareTest.backup);
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "target/test-temp/compare/result");
            System.setProperties(newProperty);
            CompareTest.clean("target/test-temp/compare/result");
        }

    }

    @Nested
    class ChangeDataSetBaseTest extends CompareTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(CompareTest.backup);
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "target/test-temp/compare/dataset");
            System.setProperties(newProperty);
            CompareTest.clean("target/test-temp/compare/dataset");
            CompareTest.copy("src/test/resources/yo/dbunitcli/application/src", "target/test-temp/compare/dataset/src/test/resources/yo/dbunitcli/application/src");
            CompareTest.copy("src/test/resources/yo/dbunitcli/application/expect", "target/test-temp/compare/dataset/src/test/resources/yo/dbunitcli/application/expect");
            final Replace replace = new Replace();
            replace.setDir(new File("target/test-temp/compare/dataset/src/test/resources/yo/dbunitcli/application/expect"));
            replace.setIncludes("**/*.csv");
            final Replace.Replacefilter filter1 = replace.createReplacefilter();
            filter1.setToken("src/test/resources");
            filter1.setValue("target/test-temp/compare/dataset/src/test/resources");
            final Replace.Replacefilter filter2 = replace.createReplacefilter();
            filter2.setToken("src\\\\test\\\\resources");
            filter2.setValue("target\\\\test-temp\\\\compare\\\\dataset\\\\src\\\\test\\\\resources");
            replace.setProject(CompareTest.PROJECT);
            replace.execute();
        }

        @Test
        @Override
        public void testResultXlsx() {
            //super.testResultXlsx();
        }
    }

    @Tag("jvmTest")
    @Nested
    @ExtendWith(JMockitExtension.class)
    class ExitCodeTest {

        @BeforeEach
        public void setUp() {
            new MockUp<System>() {
                @Mock
                public void exit(final int value) {
                    throw new RuntimeException(String.valueOf(value));
                }
            };
        }

        @Test
        public void testFailedResultDiffNotExpected() {
            try {
                Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareResultDiffNotExpected.txt"});
            } catch (final RuntimeException ex) {
                Assertions.assertEquals("1", ex.getMessage());
            }
        }

        @Test
        public void testFailedResultDiffDifferExpected() {
            try {
                Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareResultDiffInValidExpected.txt"});
            } catch (final RuntimeException ex) {
                Assertions.assertEquals("1", ex.getMessage());
            }
        }

        @Test
        public void testFailedUnExpectedNoDiff() {
            try {
                Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareResultNoDiffUnExpected.txt"});
            } catch (final RuntimeException ex) {
                Assertions.assertEquals("1", ex.getMessage());
            }
        }
    }
}