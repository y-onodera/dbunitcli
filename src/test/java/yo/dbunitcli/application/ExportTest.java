package yo.dbunitcli.application;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

public class ExportTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();

    private String baseDir = this.getClass().getResource(".").getPath();

    @Test
    public void testFromRegexToXlsx() throws Exception {
        Export.main(new String[]{"@" + this.baseDir + "/paramFromRegexToXlsx.txt"});
    }

    @Test
    public void testFromCsvToXlsx() throws Exception {
        Export.main(new String[]{"@" + this.baseDir + "/paramFromCsvToXlsx.txt"});
    }

    @Test
    public void testFromCsvqToCsv() throws Exception {
        Export.main(new String[]{"@" + this.baseDir + "/paramFromCsvqToCsv.txt"});
    }

}
