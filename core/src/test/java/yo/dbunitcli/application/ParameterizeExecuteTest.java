package yo.dbunitcli.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ParameterizeExecuteTest {

    private String baseDir;

    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources")
                .replaceFirst("/", "");
    }

    @Test
    public void testDataDrivenExport() throws Exception {
        ParameterizeExecute.main(new String[]{"@" + this.baseDir + "/paramDataDrivenExport.txt"});
    }

    @Test
    public void testDataDrivenExecute() throws Exception {
        ParameterizeExecute.main(new String[]{"@" + this.baseDir + "/paramDataDrivenExecute.txt"});
    }

    @Test
    public void testSpaceContainsPathAsSrcParameter() throws Exception {
        ParameterizeExecute.main(new String[]{"-param.src=" + this.baseDir + "csv/has space"
                , "-param.srcType=file"
                , "-cmd=convert"
                , "-template=" + this.baseDir + "param/convertTemplate.txt"});
    }

}