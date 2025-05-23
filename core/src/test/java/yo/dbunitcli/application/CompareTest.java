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
    private static final String RESOURCES_DIR = "src/test/resources/yo/dbunitcli/application";
    private static final String TEMP_DIR = "target/test-temp/compare";
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
        public void testCompareNoHeaderCsvToHeaderChangeCsv() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareNoHeaderCsvToHeaderChangeCsv.txt"});
        }

        @Test
        public void testCompareNoHeaderXlsxToSkipRowXlsx() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareNoHeaderXlsxToSkipRowXlsx.txt"});
        }

        @Test
        public void testCompareNoHeaderXlsToSkipRowXls() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareNoHeaderXlsToSkipRowXls.txt"});
        }

        @Test
        public void testCompareSkipRowCsvToCsv() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareSkipRowCsvToCsv.txt"});
        }

        @Test
        public void testCompareSkipRowNoHeaderCsvToCsv() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareSkipRowNoHeaderCsvToCsv.txt"});
        }

        @Test
        public void testCompareSkipRowFixedToFixed() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareSkipRowFixedToFixed.txt"});
        }

        @Test
        public void testCompareSkipRowRegexToRegex() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareSkipRowRegexToRegex.txt"});
        }

        @Test
        public void testCompareHeaderRegexTxtToCsv() {
            Compare.main(new String[]{"@" + CompareTest.baseDir + "/paramCompareNoHeaderRegexTxtAndCsv.txt"});
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

        private static final String TOP_DIR = TEMP_DIR + "/all/";

        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(CompareTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, TOP_DIR + "base");
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, TOP_DIR + "result");
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, TOP_DIR + "dataset");
            System.setProperties(newProperty);
            CompareTest.clean(TEMP_DIR + "/all");
            CompareTest.copy(RESOURCES_DIR + "/settings", TOP_DIR + "base/" + RESOURCES_DIR + "/settings");
            CompareTest.copy(RESOURCES_DIR + "/src", TOP_DIR + "dataset/" + RESOURCES_DIR + "/src");
            CompareTest.copy(RESOURCES_DIR + "/expect", TOP_DIR + "dataset/" + RESOURCES_DIR + "/expect");
            getReplace().execute();
        }

        private static Replace getReplace() {
            final Replace replace = new Replace();
            replace.setDir(new File(TOP_DIR + "dataset/" + RESOURCES_DIR + "/expect"));
            replace.setIncludes("**/*.csv");
            final Replace.Replacefilter filter1 = replace.createReplacefilter();
            filter1.setToken("src/test/resources");
            filter1.setValue(TOP_DIR + "dataset/src/test/resources");
            final Replace.Replacefilter filter2 = replace.createReplacefilter();
            filter2.setToken("src\\\\test\\\\resources");
            filter2.setValue("target\\\\test-temp\\\\compare\\\\all\\\\dataset\\\\src\\\\test\\\\resources");
            replace.setProject(CompareTest.PROJECT);
            return replace;
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
            newProperty.put(FileResources.PROPERTY_WORKSPACE, TEMP_DIR + "/base");
            System.setProperties(newProperty);
            CompareTest.clean(TEMP_DIR + "/base");
            CompareTest.copy(RESOURCES_DIR, TEMP_DIR + "/base/" + RESOURCES_DIR);
            getReplace().execute();
        }

        private static Replace getReplace() {
            final Replace replace = new Replace();
            replace.setDir(new File(TEMP_DIR + "/base/" + RESOURCES_DIR + "/expect"));
            replace.setIncludes("**/*.csv");
            final Replace.Replacefilter filter1 = replace.createReplacefilter();
            filter1.setToken("src/test/resources");
            filter1.setValue(TEMP_DIR + "/base/src/test/resources");
            final Replace.Replacefilter filter2 = replace.createReplacefilter();
            filter2.setToken("src\\\\test\\\\resources");
            filter2.setValue("target\\\\test-temp\\\\compare\\\\base\\\\src\\\\test\\\\resources");
            replace.setProject(CompareTest.PROJECT);
            return replace;
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
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, TEMP_DIR + "/result");
            System.setProperties(newProperty);
            CompareTest.clean(TEMP_DIR + "/result");
        }

    }

    @Nested
    class ChangeDataSetBaseTest extends CompareTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(CompareTest.backup);
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, TEMP_DIR + "/dataset");
            System.setProperties(newProperty);
            CompareTest.clean(TEMP_DIR + "/dataset");
            CompareTest.copy(RESOURCES_DIR + "/src", TEMP_DIR + "/dataset/" + RESOURCES_DIR + "/src");
            CompareTest.copy(RESOURCES_DIR + "/expect", TEMP_DIR + "/dataset/" + RESOURCES_DIR + "/expect");
            getReplace().execute();
        }

        private static Replace getReplace() {
            final Replace replace = new Replace();
            replace.setDir(new File(TEMP_DIR + "/dataset/" + RESOURCES_DIR + "/expect"));
            replace.setIncludes("**/*.csv");
            final Replace.Replacefilter filter1 = replace.createReplacefilter();
            filter1.setToken("src/test/resources");
            filter1.setValue(TEMP_DIR + "/dataset/src/test/resources");
            final Replace.Replacefilter filter2 = replace.createReplacefilter();
            filter2.setToken("src\\\\test\\\\resources");
            filter2.setValue("target\\\\test-temp\\\\compare\\\\dataset\\\\src\\\\test\\\\resources");
            replace.setProject(CompareTest.PROJECT);
            return replace;
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