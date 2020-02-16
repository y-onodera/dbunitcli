package yo.dbunitcli.application;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class CompareTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();

    private String baseDir;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(this.getClass().getResource(".").getPath(),"MS932");
    }


    @Test
    public void testSuccessResultDiffExpected() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramResultDiffValidExpected.txt"});
    }

    @Test
    public void testResultXlsx() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramResultDiffXlsx.txt"});
    }

    @Test
    public void testSuccessNoDiffExpected() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramNoDiff.txt"});
    }

    @Test
    public void testSuccessNoDiffWithCommonSetting() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramNoDiffWithCommonSetting.txt"});
    }

    @Test
    public void testComparePatternMatch() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramPatternMatch.txt"});
    }

    @Test
    public void testCompareIgnoreNoSettingTables() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramTableNoMatch.txt"});
    }

    @Test
    public void testCompareWithRowNum() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCompareWithRow.txt"});
    }

    @Test
    public void testCompareCsvToXlsx() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCsvToXlsxCompare.txt"});
    }

    @Test
    public void testCompareXlsxToCsv() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramXlsxToCsvCompare.txt"});
    }

    @Test
    public void testCompareXlsxToXls() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramXlsxToXlsCompare.txt"});
    }

    @Test
    public void testCompareXlsToXlsx() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramXlsToXlsxCompare.txt"});
    }

    @Test
    public void testCompareCsvqToCsvq() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCsvqToCsvqCompare.txt"});
    }

    @Test
    public void testCompareFilter() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramColumnFilter.txt"});
    }

    @Test
    public void testCompareSort() throws Exception {
        Compare.main(new String[]{"@" + this.baseDir + "/paramColumnSort.txt"});
    }

    @Test
    public void testFailedResultDiffNotExpected() throws Exception {
        this.exit.expectSystemExitWithStatus(1);
        this.expectedException.expect(AssertionError.class);
        Compare.main(new String[]{"@" + this.baseDir + "/paramDiffNotExpected.txt"});
    }

    @Test
    public void testFailedResultDiffDifferExpected() throws Exception {
        this.exit.expectSystemExitWithStatus(1);
        this.expectedException.expect(AssertionError.class);
        Compare.main(new String[]{"@" + this.baseDir + "/paramResultDiffInValidExpected.txt"});
    }

    @Test
    public void testFailedUnExpectedNoDiff() throws Exception {
        this.exit.expectSystemExitWithStatus(1);
        this.expectedException.expect(AssertionError.class);
        Compare.main(new String[]{"@" + this.baseDir + "/paramNoDiffUnExpected.txt"});
    }

}