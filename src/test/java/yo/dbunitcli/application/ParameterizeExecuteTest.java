package yo.dbunitcli.application;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

public class ParameterizeExecuteTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();

    private String baseDir = this.getClass().getResource(".").getPath();

    @Test
    public void testDataDrivenExport() throws Exception {
        ParameterizeExecute.main(new String[]{"@" + this.baseDir + "/paramDataDrivenExport.txt"});
    }

}