package example.y.onodera.dbunitcli;

import org.dbunit.DatabaseUnitException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

public class ApplicationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();
    private String baseDir = this.getClass().getResource(".").getPath();

    @Test
    public void testSuccessResultDiffExpected() throws DatabaseUnitException {
        Application.main(new String[]{"@" + this.baseDir + "/paramResultDiffValidExpected.txt"});
    }

    @Test
    public void testSuccessNoDiffExpected() throws DatabaseUnitException {
        Application.main(new String[]{"@" + this.baseDir + "/paramNoDiff.txt"});
    }

    @Test
    public void testCompareCsvToXlsx() throws DatabaseUnitException {
        Application.main(new String[]{"@" + this.baseDir + "/paramCsvToXlsxCompare.txt"});
    }

    @Test
    public void testCompareXlsxToCsv() throws DatabaseUnitException {
        Application.main(new String[]{"@" + this.baseDir + "/paramXlsxToCsvCompare.txt"});
    }

    @Test
    public void testCompareXlsxToXls() throws DatabaseUnitException {
        Application.main(new String[]{"@" + this.baseDir + "/paramXlsxToXlsCompare.txt"});
    }

    @Test
    public void testCompareXlsToXlsx() throws DatabaseUnitException {
        Application.main(new String[]{"@" + this.baseDir + "/paramXlsToXlsxCompare.txt"});
    }

    @Test
    public void testFailedResultDiffNotExpected() throws DatabaseUnitException {
        this.exit.expectSystemExitWithStatus(1);
        Application.main(new String[]{"@" + this.baseDir + "/paramDiffNotExpected.txt"});
    }

    @Test
    public void testFailedResultDiffDifferExpected() throws DatabaseUnitException {
        this.exit.expectSystemExitWithStatus(1);
        this.expectedException.expect(AssertionError.class);
        Application.main(new String[]{"@" + this.baseDir + "/paramResultDiffInValidExpected.txt"});
    }

    @Test
    public void testFailedUnExpectedNoDiff() throws DatabaseUnitException {
        this.exit.expectSystemExitWithStatus(1);
        this.expectedException.expect(AssertionError.class);
        Application.main(new String[]{"@" + this.baseDir + "/paramNoDiffUnExpected.txt"});
    }

}