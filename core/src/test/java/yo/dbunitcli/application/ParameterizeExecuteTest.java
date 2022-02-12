package yo.dbunitcli.application;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ParameterizeExecuteTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();

    private String baseDir;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(this.getClass().getResource(".").getPath(),"UTF-8")
                .replace("target/test-classes","src/test/resources");
    }

    @Test
    public void testDataDrivenExport() throws Exception {
        ParameterizeExecute.main(new String[]{"@" + this.baseDir + "/paramDataDrivenExport.txt"});
    }

    @Test
    public void testDataDrivenExecute() throws Exception {
        ParameterizeExecute.main(new String[]{"@" + this.baseDir + "/paramDataDrivenExecute.txt"});
    }


}