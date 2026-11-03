package yo.dbunitcli.application.command;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.types.FileSet;
import org.junit.jupiter.api.*;
import yo.dbunitcli.resource.FileResources;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CompareTest {

    private static final Project PROJECT = new Project();
    private static final Properties backup = new Properties();
    private static final String PARAMETER_DIR = "src/test/resources/yo/dbunitcli/application/command";
    private static final String RESOURCES_DIR = "src/test/resources/yo/dbunitcli/application";
    private static final String TEMP_DIR = "target/test-temp/compare";

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

    private static final Pattern RESOURCES_PATH = Pattern.compile("src[\\\\/]test[\\\\/]resources");

    // xlsxはReplaceで置換できないため、セルの値の比較元パスを置換先のパスに書き換える
    private static void replaceXlsxPaths(final String dir, final String resourcesPath) {
        final String replacement = Matcher.quoteReplacement(resourcesPath);
        try (final Stream<Path> files = Files.walk(Path.of(dir))) {
            for (final Path file : files.filter(it -> it.toString().endsWith(".xlsx")).toList()) {
                try (final XSSFWorkbook book = new XSSFWorkbook(new ByteArrayInputStream(Files.readAllBytes(file)))) {
                    boolean changed = false;
                    for (final Sheet sheet : book) {
                        for (final Row row : sheet) {
                            for (final Cell cell : row) {
                                if (cell.getCellType() == CellType.STRING) {
                                    final String value = cell.getStringCellValue();
                                    final String replaced = RESOURCES_PATH.matcher(value).replaceAll(replacement);
                                    if (!replaced.equals(value)) {
                                        cell.setCellValue(replaced);
                                        changed = true;
                                    }
                                }
                            }
                        }
                    }
                    if (changed) {
                        try (final OutputStream out = Files.newOutputStream(file)) {
                            book.write(out);
                        }
                    }
                }
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @BeforeAll
    public static void setUp() {
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
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareResultDiffValidExpected.txt"});
        }

        @Test
        public void testResultXlsx() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareResultDiffXlsx.txt"});
        }

        @Test
        public void testSuccessNoDiffExpected() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareResultNoDiff.txt"});
        }

        @Test
        public void testSuccessNoDiffWithCommonSetting() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareResultNoDiffWithCommonSetting.txt"});
        }

        @Test
        public void testComparePatternMatch() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareOnlyPatternMatch.txt"});
        }

        @Test
        public void testCompareIgnoreNoSettingTables() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareTableNoMatch.txt"});
        }

        @Test
        public void testCompareWithRowNum() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareWithRow.txt"});
        }

        @Test
        public void testCompareWithRowFilter() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareWithFilterRow.txt"});
        }

        @Test
        public void testCompareCsvAndXlsx() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareCsvAndXlsx.txt"});
        }

        @Test
        public void testCompareCsvAndTsv() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareCsvAndTsv.txt"});
        }

        @Test
        public void testCompareXlsxAndCsv() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareXlsxAndCsv.txt"});
        }

        @Test
        public void testCompareXlsxAndXls() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareXlsxAndXls.txt"});
        }

        @Test
        public void testCompareXlsAndXlsx() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareXlsAndXlsx.txt"});
        }

        @Test
        public void testCompareCsvqToCsvq() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareCsvqAndCsvq.txt"});
        }

        @Test
        public void testCompareNoHeaderCsvToHeaderChangeCsv() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareNoHeaderCsvToHeaderChangeCsv.txt"});
        }

        @Test
        public void testCompareNoHeaderXlsxToSkipRowXlsx() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareNoHeaderXlsxToSkipRowXlsx.txt"});
        }

        @Test
        public void testCompareNoHeaderXlsToSkipRowXls() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareNoHeaderXlsToSkipRowXls.txt"});
        }

        @Test
        public void testCompareSkipRowCsvToCsv() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareSkipRowCsvToCsv.txt"});
        }

        @Test
        public void testCompareSkipRowNoHeaderCsvToCsv() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareSkipRowNoHeaderCsvToCsv.txt"});
        }

        @Test
        public void testCompareSkipRowFixedToFixed() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareSkipRowFixedToFixed.txt"});
        }

        @Test
        public void testCompareSkipRowRegexToRegex() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareSkipRowRegexToRegex.txt"});
        }

        @Test
        public void testCompareHeaderRegexTxtToCsv() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareNoHeaderRegexTxtAndCsv.txt"});
        }

        @Test
        public void testCompareNoHeaderRegexTxtToCsv() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareNoHeaderRegexTxtAndCsv.txt"});
        }

        @Test
        public void testCompareNoHeaderRegexTxtToNoHeaderCsv() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareNoHeaderRegexTxtAndNoHeaderCsv.txt"});
        }

        @Test
        public void testCompareFilter() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareColumnFilter.txt"});
        }

        @Test
        public void testCompareSort() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareColumnSort.txt"});
        }

        @Test
        public void testComparePdf() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramComparePdf.txt"});
        }

        @Test
        public void testComparePdfDiffAllPage() {
            Assumptions.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("win"));
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramComparePdfDiffAllPage.txt"});
        }

        @Test
        public void testCompareImage() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareImage.txt"});
        }

        @Test
        public void testCompareImageExcludedAreas() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareImageExcludedAreas.txt"});
        }

        @Test
        public void testSettingMerge() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareSettingMerge.txt"});
        }

        @Test
        public void testSettingImport() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareSettingImport.txt"});
        }

        @Test
        public void testExcelWithSchema() {
            Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareXlsxAndXlsWithSchema.txt"});
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
            CompareTest.replaceXlsxPaths(TOP_DIR + "dataset/" + RESOURCES_DIR + "/expect", TOP_DIR + "dataset/src/test/resources");
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
            CompareTest.replaceXlsxPaths(TEMP_DIR + "/base/" + RESOURCES_DIR + "/expect", TEMP_DIR + "/base/src/test/resources");
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
            CompareTest.replaceXlsxPaths(TEMP_DIR + "/dataset/" + RESOURCES_DIR + "/expect", TEMP_DIR + "/dataset/src/test/resources");
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
    }

    @Nested
    class ExitCodeTest {

        @Test
        public void testFailedResultDiffNotExpected() {
            try {
                Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareResultDiffNotExpected.txt"});
            } catch (final RuntimeException ex) {
                Assertions.assertEquals("1", ex.getMessage());
            }
        }

        @Test
        public void testFailedResultDiffDifferExpected() {
            try {
                Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareResultDiffInValidExpected.txt"});
            } catch (final RuntimeException ex) {
                Assertions.assertEquals("1", ex.getMessage());
            }
        }

        @Test
        public void testFailedUnExpectedNoDiff() {
            try {
                Compare.main(new String[]{"@" + PARAMETER_DIR + "/paramCompareResultNoDiffUnExpected.txt"});
            } catch (final RuntimeException ex) {
                Assertions.assertEquals("1", ex.getMessage());
            }
        }
    }
}