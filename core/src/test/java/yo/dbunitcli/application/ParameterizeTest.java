package yo.dbunitcli.application;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;
import org.junit.jupiter.api.*;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

public class ParameterizeTest {

    private static final Project PROJECT = new Project();
    private static final Properties backup = new Properties();
    private static String baseDir;

    private static void copy(final String from, final String to) {
        final Copy copy = new Copy();
        copy.setProject(ParameterizeTest.PROJECT);
        final FileSet src = new FileSet();
        src.setDir(new File(from));
        copy.addFileset(src);
        copy.setTodir(new File(to));
        copy.execute();
    }

    private static void clean(final String target) {
        final Delete delete = new Delete();
        delete.setProject(ParameterizeTest.PROJECT);
        delete.setDir(new File(target));
        delete.execute();
    }

    @BeforeAll
    public static void setUp() throws UnsupportedEncodingException {
        ParameterizeTest.baseDir = URLDecoder.decode(Objects.requireNonNull(ParameterizeTest.class.getResource(".")).getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources")
                .replaceFirst("/", "");
        ParameterizeTest.PROJECT.setName("parameterizeTest");
        ParameterizeTest.PROJECT.setBaseDir(new File("."));
        ParameterizeTest.PROJECT.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        ParameterizeTest.backup.putAll(System.getProperties());
    }

    abstract static class TestCase {
        @AfterAll
        static void restore() {
            System.setProperties(backup);
        }

        @Test
        public void testDataDrivenExport() {
            Parameterize.main(new String[]{"@" + ParameterizeTest.baseDir + "/paramDataDrivenExport.txt"});
        }

        @Test
        public void testDataDrivenExecute() {
            Parameterize.main(new String[]{"@" + ParameterizeTest.baseDir + "/paramDataDrivenExecute.txt"});
        }

        @Test
        public void testSpaceContainsPathAsSrcParameter() {
            Parameterize.main(new String[]{"-param.src=" + ParameterizeTest.baseDir + "src/csv/has space"
                    , "-param.srcType=file"
                    , "-cmd=convert"
                    , "-template=" + ParameterizeTest.baseDir + "/settings/param/convertTemplate.txt"});
        }

        @Test
        public void testExecCommandNoneParameter() {
            Parameterize.main(new String[]{
                    "-cmd=$param.inputParam.cmdName$"
                    , "-cmdParam=$param.inputParam.templateName$"
                    , "-arg=-result=target/test-classes/yo/dbunitcli/application/param/csv2xlsx/result"
                    , "-P=cmdName=convert"
                    , "-PtemplateName=" + ParameterizeTest.baseDir + "/paramConvertCsvToXlsx.txt"
            });
            Parameterize.main(new String[]{
                    "-cmd=compare"
                    , "-template=" + ParameterizeTest.baseDir + "/paramCompareXlsAndXlsx.txt"
                    , "-arg=-setting=src/test/resources/yo/dbunitcli/application/settings/csv2xlsx/setting_replacelineseparator.json"
                    , "-arg=-new.src=target/test-classes/yo/dbunitcli/application/param/csv2xlsx/result"
                    , "-arg=-result=target/test-classes/yo/dbunitcli/application/param/csv2xlsx/compare/result"
            });
        }

        @Test
        public void testExecCommandNoTemplate() throws Exception {
            Parameterize.main(new String[]{
                    "-cmd=generate"
                    , "-arg=-setting=src/test/resources/yo/dbunitcli/application/settings/generate/with_metadata/settings.json"
                    , "-arg=-src=src/test/resources/yo/dbunitcli/application/src/generate/with_metadata/source/csv"
                    , "-A=-encoding=UTF-8"
                    , "-A-resultPath=target/test-classes/yo/dbunitcli/application/param/generate/result/settings.json"
                    , "-A-generateType=settings"
            });
            final String expect = Files.readString(new File("src/test/resources/yo/dbunitcli/application/expect/generate/settings/expect/txt", "settings.json").toPath(), StandardCharsets.UTF_8);
            final String actual = Files.readString(new File(this.getResult(), "settings.json").toPath(), StandardCharsets.UTF_8);
            Assertions.assertEquals(expect, actual);
        }

        protected String getResult() {
            return "target/test-classes/yo/dbunitcli/application/param/generate/result";
        }
    }

    @Nested
    class NoSystemPropertyTest extends ParameterizeTest.TestCase {
    }

    @Nested
    class AllSystemPropertyTest extends ParameterizeTest.TestCase {

        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ParameterizeTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "target/test-temp/parameterize/all/base");
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "target/test-temp/parameterize/all/result");
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "target/test-temp/parameterize/all/dataset");
            System.setProperties(newProperty);
            ParameterizeTest.clean("target/test-temp/parameterize/all");
            ParameterizeTest.copy("src/test/resources/yo/dbunitcli/application/settings", "target/test-temp/parameterize/all/base/src/test/resources/yo/dbunitcli/application/settings");
            ParameterizeTest.copy("src/test/resources/yo/dbunitcli/application/src", "target/test-temp/parameterize/all/dataset/src/test/resources/yo/dbunitcli/application/src");
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/target/test-temp/parameterize/all/result/target/");
        }
    }

    @Nested
    class ChangeWorkSpaceTest extends ParameterizeTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ParameterizeTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "target/test-temp/parameterize/base");
            System.setProperties(newProperty);
            ParameterizeTest.clean("target/test-temp/parameterize/base");
            ParameterizeTest.copy("src/test/resources/yo/dbunitcli/application", "target/test-temp/parameterize/base/src/test/resources/yo/dbunitcli/application");
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/target/test-temp/parameterize/base/target/");
        }
    }

    @Nested
    class ChangeResultBaseTest extends ParameterizeTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ParameterizeTest.backup);
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "target/test-temp/parameterize/result");
            System.setProperties(newProperty);
            ParameterizeTest.clean("target/test-temp/parameterize/result");
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/target/test-temp/parameterize/result/target/");
        }
    }

    @Nested
    class ChangeDataSetBaseTest extends ParameterizeTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ParameterizeTest.backup);
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "target/test-temp/parameterize/dataset");
            System.setProperties(newProperty);
            ParameterizeTest.clean("target/test-temp/parameterize/dataset");
            ParameterizeTest.copy("src/test/resources/yo/dbunitcli/application/src", "target/test-temp/parameterize/dataset/src/test/resources/yo/dbunitcli/application/src");
        }

    }
}