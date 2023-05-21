package yo.dbunitcli.application;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ParameterizeExecuteTest {

    private String baseDir;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources");
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