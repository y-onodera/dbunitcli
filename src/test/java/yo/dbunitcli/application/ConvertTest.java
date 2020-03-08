package yo.dbunitcli.application;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class ConvertTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();

    private String baseDir;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(this.getClass().getResource(".").getPath(),"UTF-8");
    }

    @Test
    public void testFromRegexToXlsx() throws Exception {
        Convert.main(new String[]{"@" + this.baseDir + "/paramFromRegexToXlsx.txt"});
    }

    @Test
    public void testNoSetting() throws Exception {
        Convert.main(new String[]{"@" + this.baseDir + "/paramFromRegexToXlsxNoSetting.txt"});
    }

    @Test
    public void testFromCsvToXlsx() throws Exception {
        Convert.main(new String[]{"@" + this.baseDir + "/paramFromCsvToXlsx.txt"});
    }

    @Test
    public void testFromCsvqToCsv() throws Exception {
        Convert.main(new String[]{"@" + this.baseDir + "/paramFromCsvqToCsv.txt"});
    }

    @Test
    public void testXlsxWithSchemaToCsv() throws Exception {
        Convert.main(new String[]{"@" + this.baseDir + "/paramXlsxWithSchemaToCsv.txt"});
    }
}
