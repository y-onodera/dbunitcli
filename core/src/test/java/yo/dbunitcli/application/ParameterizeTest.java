package yo.dbunitcli.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class ParameterizeTest {

    private String baseDir;

    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources")
                .replaceFirst("/", "");
    }

    @Test
    public void testDataDrivenExport() throws Exception {
        Parameterize.main(new String[]{"@" + this.baseDir + "/paramDataDrivenExport.txt"});
    }

    @Test
    public void testDataDrivenExecute() throws Exception {
        Parameterize.main(new String[]{"@" + this.baseDir + "/paramDataDrivenExecute.txt"});
    }

    @Test
    public void testSpaceContainsPathAsSrcParameter() throws Exception {
        Parameterize.main(new String[]{"-param.src=" + this.baseDir + "csv/has space"
                , "-param.srcType=file"
                , "-cmd=convert"
                , "-template=" + this.baseDir + "param/convertTemplate.txt"});
    }

    @Test
    public void testExecCommandNoneParameter() throws Exception {
        Parameterize.main(new String[]{
                "-cmd=$param.inputParam.cmdName$"
                , "-cmdParam=$param.inputParam.templateName$"
                , "-arg=-result=target/test-classes/yo/dbunitcli/application/param/csv2xlsx/result"
                , "-P=cmdName=convert"
                , "-PtemplateName=" + this.baseDir + "/paramConvertCsvToXlsx.txt"
        });
        Parameterize.main(new String[]{
                "-cmd=compare"
                , "-template=" + this.baseDir + "/paramCompareXlsAndXlsx.txt"
                , "-arg=-setting=src/test/resources/yo/dbunitcli/application/csv2xlsx/setting_replacelineseparator.json"
                , "-arg=-new.src=target/test-classes/yo/dbunitcli/application/param/csv2xlsx/result"
                , "-arg=-result=target/test-classes/yo/dbunitcli/application/param/csv2xlsx/compare/result"
        });
    }

    @Test
    public void testExecCommandNoTemplate() throws Exception {
        Parameterize.main(new String[]{
                "-cmd=generate"
                , "-arg=-setting=src/test/resources/yo/dbunitcli/application/generate/with_metadata/settings.json"
                , "-arg=-src=src/test/resources/yo/dbunitcli/application/generate/with_metadata/source/csv"
                , "-A=-encoding=UTF-8"
                , "-A-resultPath=target/test-classes/yo/dbunitcli/application/param/generate/result/settings.json"
                , "-A-generateType=settings"
        });
        final String expect = Files.readString(new File("src/test/resources/yo/dbunitcli/application/generate/settings/expect/txt", "settings.json").toPath(), StandardCharsets.UTF_8);
        final String actual = Files.readString(new File("target/test-classes/yo/dbunitcli/application/param/generate/result", "settings.json").toPath(), StandardCharsets.UTF_8);
        Assertions.assertEquals(expect, actual);
    }

}