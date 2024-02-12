package yo.dbunitcli.application;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit5.JMockitExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CompareTest {

    private String baseDir;

    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources");
    }

    @Test
    public void testSuccessResultDiffExpected() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareResultDiffValidExpected.txt"});
    }

    @Test
    public void testResultXlsx() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareResultDiffXlsx.txt"});
    }

    @Test
    public void testSuccessNoDiffExpected() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareResultNoDiff.txt"});
    }

    @Test
    public void testSuccessNoDiffWithCommonSetting() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareResultNoDiffWithCommonSetting.txt"});
    }

    @Test
    public void testComparePatternMatch() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareOnlyPatternMatch.txt"});
    }

    @Test
    public void testCompareIgnoreNoSettingTables() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareTableNoMatch.txt"});
    }

    @Test
    public void testCompareWithRowNum() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareWithRow.txt"});
    }

    @Test
    public void testCompareWithRowFilter() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareWithFilterRow.txt"});
    }

    @Test
    public void testCompareCsvAndXlsx() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareCsvAndXlsx.txt"});
    }

    @Test
    public void testCompareCsvAndTsv() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareCsvAndTsv.txt"});
    }

    @Test
    public void testCompareXlsxAndCsv() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareXlsxAndCsv.txt"});
    }

    @Test
    public void testCompareXlsxAndXls() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareXlsxAndXls.txt"});
    }

    @Test
    public void testCompareXlsAndXlsx() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareXlsAndXlsx.txt"});
    }

    @Test
    public void testCompareCsvqToCsvq() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareCsvqAndCsvq.txt"});
    }

    @Test
    public void testCompareNoHeaderRegexTxtToCsv() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareNoHeaderRegexTxtAndCsv.txt"});
    }

    @Test
    public void testCompareNoHeaderRegexTxtToNoHeaderCsv() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareNoHeaderRegexTxtAndNoHeaderCsv.txt"});
    }

    @Test
    public void testCompareFilter() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareColumnFilter.txt"});
    }

    @Test
    public void testCompareSort() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareColumnSort.txt"});
    }

    @Test
    public void testComparePdf() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramComparePdf.txt"});
    }

    @Test
    public void testComparePdfDiffAllPage() throws Exception {
        Assumptions.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("win"));
        Compare.main(new String[]{"@" + this.baseDir + "/paramComparePdfDiffAllPage.txt"});
    }

    @Test
    public void testCompareImage() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareImage.txt"});
    }

    @Test
    public void testCompareImageExcludedAreas() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareImageExcludedAreas.txt"});
    }

    @Test
    public void testSettingMerge() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareSettingMerge.txt"});
    }

    @Test
    public void testSettingImport() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareSettingImport.txt"});
    }

    @Test
    public void testExcelWithSchema() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareXlsxAndXlsWithSchema.txt"});
    }

    @Tag("jvmTest")
    @Nested
    @ExtendWith(JMockitExtension.class)
    class ExitCodeTest {
        @Test
        public void testFailedResultDiffNotExpected() throws Exception {
            new MockUp<System>() {
                @Mock
                public void exit(final int value) {
                    throw new RuntimeException(String.valueOf(value));
                }
            };
            try {
                Compare.main(new String[]{"@" + CompareTest.this.baseDir + "/paramCompareResultDiffNotExpected.txt"});
            } catch (final RuntimeException ex) {
                Assertions.assertEquals("1", ex.getMessage());
            }
        }

        @Test
        public void testFailedResultDiffDifferExpected() throws Exception {
            new MockUp<System>() {
                @Mock
                public void exit(final int value) {
                    throw new RuntimeException(String.valueOf(value));
                }
            };
            try {
                Compare.main(new String[]{"@" + CompareTest.this.baseDir + "/paramCompareResultDiffInValidExpected.txt"});
            } catch (final RuntimeException ex) {
                Assertions.assertEquals("1", ex.getMessage());
            }
        }

        @Test
        public void testFailedUnExpectedNoDiff() throws Exception {
            new MockUp<System>() {
                @Mock
                public void exit(final int value) {
                    throw new RuntimeException(String.valueOf(value));
                }
            };
            try {
                Compare.main(new String[]{"@" + CompareTest.this.baseDir + "/paramCompareResultNoDiffUnExpected.txt"});
            } catch (final RuntimeException ex) {
                Assertions.assertEquals("1", ex.getMessage());
            }
        }
    }
}