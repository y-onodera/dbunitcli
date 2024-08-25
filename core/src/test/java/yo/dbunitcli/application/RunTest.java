package yo.dbunitcli.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class RunTest {
    private String testResourcesDir;

    private String baseDir;

    private String cmd;

    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8);
        this.testResourcesDir = this.baseDir.replace("target/test-classes", "src/test/resources");
    }

    @Test
    public void testCmd() throws Exception {
        this.cmd = "cmd";
        Run.main(new String[]{"@" + this.testResourcesDir + "/paramRunCmd.txt"});
        this.assertGenerateFileEquals("テスト.txt");
    }

    @Test
    public void testBat() throws Exception {
        this.cmd = "bat";
        Run.main(new String[]{"@" + this.testResourcesDir + "/paramRunBat.txt"});
        this.assertGenerateFileEquals("テスト.txt");
    }

    @Test
    public void testAntRun() throws Exception {
        this.cmd = "ant";
        Run.main(new String[]{"@" + this.testResourcesDir + "/paramRunAntNoProperty.txt"});
        this.assertGenerateFileEquals("copy/no-property.txt");
    }

    @Test
    public void testAntRunWithProperty() throws Exception {
        this.cmd = "ant";
        Run.main(new String[]{"@" + this.testResourcesDir + "/paramRunAntWithProperty.txt"});
        this.assertGenerateFileEquals("replace/replace-property.txt");
    }

    private void assertGenerateFileEquals(final String target) throws IOException {
        final String expect = Files.readString(new File(this.testResourcesDir + "/" + this.cmd + "/expect", target).toPath(), StandardCharsets.UTF_8);
        final String actual = Files.readString(new File(this.baseDir + "/" + this.cmd, target).toPath(), StandardCharsets.UTF_8);
        Assertions.assertEquals(expect, actual);
    }
}
