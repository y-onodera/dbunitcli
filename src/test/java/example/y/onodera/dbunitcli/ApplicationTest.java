package example.y.onodera.dbunitcli;

import org.dbunit.DatabaseUnitException;
import org.junit.Test;

public class ApplicationTest {

    private String baseDir = this.getClass().getResource(".").getPath();

    @Test
    public void testCsvCompare() throws DatabaseUnitException {
        Application.main(new String[]{
                "@" + this.baseDir + "/param.txt"});
    }

}