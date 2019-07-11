package yo.dbunitcli.application;

import org.dbunit.DatabaseUnitException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

public class CompareTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();

    private String baseDir = this.getClass().getResource(".").getPath();

    @Test
    public void testSuccessResultDiffExpected() throws DatabaseUnitException {
        Compare.main(new String[]{"@" + this.baseDir + "/paramResultDiffValidExpected.txt"});
    }

    @Test
    public void testResultXlsx() throws DatabaseUnitException {
        Compare.main(new String[]{"@" + this.baseDir + "/paramResultDiffXlsx.txt"});
    }

    @Test
    public void testSuccessNoDiffExpected() throws DatabaseUnitException {
        Compare.main(new String[]{"@" + this.baseDir + "/paramNoDiff.txt"});
    }

    @Test
    public void testSuccessNoDiffWithCommonSetting() throws DatabaseUnitException {
        Compare.main(new String[]{"@" + this.baseDir + "/paramNoDiffWithCommonSetting.txt"});
    }

    @Test
    public void testComparePatternMatch() throws DatabaseUnitException {
        Compare.main(new String[]{"@" + this.baseDir + "/paramPatternMatch.txt"});
    }

    @Test
    public void testCompareCsvToXlsx() throws DatabaseUnitException {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCsvToXlsxCompare.txt"});
    }

    @Test
    public void testCompareXlsxToCsv() throws DatabaseUnitException {
        Compare.main(new String[]{"@" + this.baseDir + "/paramXlsxToCsvCompare.txt"});
    }

    @Test
    public void testCompareXlsxToXls() throws DatabaseUnitException {
        Compare.main(new String[]{"@" + this.baseDir + "/paramXlsxToXlsCompare.txt"});
    }

    @Test
    public void testCompareXlsToXlsx() throws DatabaseUnitException {
        Compare.main(new String[]{"@" + this.baseDir + "/paramXlsToXlsxCompare.txt"});
    }

    @Test
    public void testCompareCsvqToCsvq() throws DatabaseUnitException {
        Compare.main(new String[]{"@" + this.baseDir + "/paramCsvqToCsvqCompare.txt"});
    }

    @Test
    public void testCompareFilter() throws DatabaseUnitException {
        Compare.main(new String[]{"@" + this.baseDir + "/paramColumnFilter.txt"});
    }

    @Test
    public void testFailedResultDiffNotExpected() throws DatabaseUnitException {
        this.exit.expectSystemExitWithStatus(1);
        Compare.main(new String[]{"@" + this.baseDir + "/paramDiffNotExpected.txt"});
    }

    @Test
    public void testFailedResultDiffDifferExpected() throws DatabaseUnitException {
        this.exit.expectSystemExitWithStatus(1);
        this.expectedException.expect(AssertionError.class);
        Compare.main(new String[]{"@" + this.baseDir + "/paramResultDiffInValidExpected.txt"});
    }

    @Test
    public void testFailedUnExpectedNoDiff() throws DatabaseUnitException {
        this.exit.expectSystemExitWithStatus(1);
        this.expectedException.expect(AssertionError.class);
        Compare.main(new String[]{"@" + this.baseDir + "/paramNoDiffUnExpected.txt"});
    }

}