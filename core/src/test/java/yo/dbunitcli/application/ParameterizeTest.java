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
    private static final String RESOURCES_DIR = "src/test/resources/yo/dbunitcli/application";
    private static final String TEMP_DIR = "target/test-temp/parameterize";
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
                    , "-arg=-setting=" + RESOURCES_DIR + "/settings/csv2xlsx/setting_replacelineseparator.json"
                    , "-arg=-new.src=target/test-classes/yo/dbunitcli/application/param/csv2xlsx/result"
                    , "-arg=-result=target/test-classes/yo/dbunitcli/application/param/csv2xlsx/compare/result"
            });
        }

        @Test
        public void testExecCommandNoTemplate() throws Exception {
            Parameterize.main(new String[]{
                    "-cmd=generate"
                    , "-arg=-setting=" + RESOURCES_DIR + "/settings/generate/with_metadata/settings.json"
                    , "-arg=-src=" + RESOURCES_DIR + "/src/generate/with_metadata/source/csv"
                    , "-A=-encoding=UTF-8"
                    , "-A-resultPath=target/test-classes/yo/dbunitcli/application/param/generate/result/settings.json"
                    , "-A-generateType=settings"
            });
            final String expect = Files.readString(new File(RESOURCES_DIR + "/expect/generate/settings/expect/txt", "settings.json").toPath(), StandardCharsets.UTF_8);
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

        private static final String TOP_DIR = TEMP_DIR + "/all/";

        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ParameterizeTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, TOP_DIR + "base");
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, TOP_DIR + "result");
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, TOP_DIR + "dataset");
            System.setProperties(newProperty);
            ParameterizeTest.clean(TEMP_DIR + "/all");
            ParameterizeTest.copy(RESOURCES_DIR + "/settings", TOP_DIR + "base/" + RESOURCES_DIR + "/settings");
            ParameterizeTest.copy(RESOURCES_DIR + "/src", TOP_DIR + "dataset/" + RESOURCES_DIR + "/src");
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/" + TOP_DIR + "result/target/");
        }
    }

    @Nested
    class ChangeWorkSpaceTest extends ParameterizeTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ParameterizeTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, TEMP_DIR + "/base");
            System.setProperties(newProperty);
            ParameterizeTest.clean(TEMP_DIR + "/base");
            ParameterizeTest.copy(RESOURCES_DIR, TEMP_DIR + "/base/" + RESOURCES_DIR);
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/" + TEMP_DIR + "/base/target/");
        }
    }

    @Nested
    class ChangeResultBaseTest extends ParameterizeTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ParameterizeTest.backup);
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, TEMP_DIR + "/result");
            System.setProperties(newProperty);
            ParameterizeTest.clean(TEMP_DIR + "/result");
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/" + TEMP_DIR + "/result/target/");
        }
    }

    @Nested
    class ChangeDataSetBaseTest extends ParameterizeTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ParameterizeTest.backup);
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, TEMP_DIR + "/dataset");
            System.setProperties(newProperty);
            ParameterizeTest.clean(TEMP_DIR + "/dataset");
            ParameterizeTest.copy(RESOURCES_DIR + "/src", TEMP_DIR + "/dataset/" + RESOURCES_DIR + "/src");
        }

    }
}