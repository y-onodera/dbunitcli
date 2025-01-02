package yo.dbunitcli.application;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.types.FileSet;
import org.junit.jupiter.api.*;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

public class RunTest {

    private static final Project PROJECT = new Project();
    private static final Properties backup = new Properties();
    private static String testResourcesDir;

    private static String baseDir;

    private static String cmd;

    private static void copy(final String from, final String to) {
        final Copy copy = new Copy();
        copy.setProject(RunTest.PROJECT);
        final FileSet src = new FileSet();
        src.setDir(new File(from));
        copy.addFileset(src);
        copy.setTodir(new File(to));
        copy.execute();
    }

    private static void clean(final String target) {
        final Delete delete = new Delete();
        delete.setProject(RunTest.PROJECT);
        delete.setDir(new File(target));
        delete.execute();
    }

    @BeforeAll
    public static void setUp() throws UnsupportedEncodingException {
        RunTest.baseDir = URLDecoder.decode(Objects.requireNonNull(RunTest.class.getResource(".")).getPath(), StandardCharsets.UTF_8);
        RunTest.testResourcesDir = RunTest.baseDir.replace("target/test-classes", "src/test/resources");
        RunTest.PROJECT.setName("compareTest");
        RunTest.PROJECT.setBaseDir(new File("."));
        RunTest.PROJECT.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        RunTest.backup.putAll(System.getProperties());
    }

    abstract static class TestCase {
        @AfterAll
        static void restore() {
            System.setProperties(backup);
            FileResources.setContext(new FileResources.FileResourcesContext());
        }

        @Test
        public void testCmd() throws Exception {
            RunTest.cmd = "cmd";
            Run.main(new String[]{"@" + RunTest.testResourcesDir + "/paramRunCmd.txt"});
            this.assertGenerateFileEquals("テスト.txt");
        }

        @Test
        public void testBat() throws Exception {
            RunTest.cmd = "bat";
            Run.main(new String[]{"@" + RunTest.testResourcesDir + "/paramRunBat.txt"});
            this.assertGenerateFileEquals("テスト.txt");
        }

        @Test
        public void testAntRun() throws Exception {
            RunTest.cmd = "ant";
            Run.main(new String[]{"@" + RunTest.testResourcesDir + "/paramRunAntNoProperty.txt"});
            this.assertGenerateFileEquals("copy/no-property.txt");
        }

        @Test
        public void testAntRunWithProperty() throws Exception {
            RunTest.cmd = "ant";
            Run.main(new String[]{"@" + RunTest.testResourcesDir + "/paramRunAntWithProperty.txt"});
            this.assertGenerateFileEquals("replace/replace-property.txt");
        }

        private void assertGenerateFileEquals(final String target) throws IOException {
            final String expect = Files.readString(new File(RunTest.testResourcesDir + "/expect/" + RunTest.cmd + "/expect", target).toPath(), StandardCharsets.UTF_8);
            final String actual = Files.readString(new File(this.getResult(), target).toPath(), StandardCharsets.UTF_8);
            Assertions.assertEquals(expect, actual);
        }

        protected String getResult() {
            return RunTest.baseDir + "/" + RunTest.cmd;
        }
    }

    @Nested
    class NoSystemPropertyTest extends RunTest.TestCase {
    }

    @Nested
    class AllSystemPropertyTest extends RunTest.TestCase {

        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(RunTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "target/test-temp/run/all/base");
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "target/test-temp/run/all/result");
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "target/test-temp/run/all/dataset");
            System.setProperties(newProperty);
            FileResources.setContext(new FileResources.FileResourcesContext());
            RunTest.clean("target/test-temp/run/all");
            RunTest.copy("src/test/resources/yo/dbunitcli/application/settings", "target/test-temp/run/all/base/src/test/resources/yo/dbunitcli/application/settings");
            RunTest.copy("src/test/resources/yo/dbunitcli/application/src", "target/test-temp/run/all/dataset/src/test/resources/yo/dbunitcli/application/src");
            RunTest.copy("src/test/resources/yo/dbunitcli/application/expect", "target/test-temp/run/all/dataset/src/test/resources/yo/dbunitcli/application/expect");
            final Replace replace = new Replace();
            replace.setDir(new File("target/test-temp/run/all/dataset/src/test/resources/yo/dbunitcli/application/src"));
            replace.setIncludes("**/*.xml");
            replace.setIncludes("**/*.bat");
            replace.setIncludes("**/*.cmd");
            final Replace.Replacefilter filter1 = replace.createReplacefilter();
            filter1.setToken("core/src");
            filter1.setValue("dataset/src");
            final Replace.Replacefilter filter2 = replace.createReplacefilter();
            filter2.setToken("core\\src");
            filter2.setValue("dataset\\src");
            final Replace.Replacefilter filter3 = replace.createReplacefilter();
            filter3.setToken("core/target");
            filter3.setValue("dataset/target");
            final Replace.Replacefilter filter4 = replace.createReplacefilter();
            filter4.setToken("core\\target");
            filter4.setValue("dataset\\target");
            replace.setProject(RunTest.PROJECT);
            replace.execute();
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/target/test-temp/run/all/dataset/target/");
        }
    }

    @Nested
    class ChangeWorkSpaceTest extends RunTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(RunTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "target/test-temp/run/base");
            System.setProperties(newProperty);
            FileResources.setContext(new FileResources.FileResourcesContext());
            RunTest.clean("target/test-temp/run/base");
            RunTest.copy("src/test/resources/yo/dbunitcli/application", "target/test-temp/run/base/src/test/resources/yo/dbunitcli/application");
            final Replace replace = new Replace();
            replace.setDir(new File("target/test-temp/run/base/src/test/resources/yo/dbunitcli/application/src"));
            replace.setIncludes("**/*.xml");
            replace.setIncludes("**/*.bat");
            replace.setIncludes("**/*.cmd");
            final Replace.Replacefilter filter1 = replace.createReplacefilter();
            filter1.setToken("core/src");
            filter1.setValue("base/src");
            final Replace.Replacefilter filter2 = replace.createReplacefilter();
            filter2.setToken("core\\src");
            filter2.setValue("base\\src");
            final Replace.Replacefilter filter3 = replace.createReplacefilter();
            filter3.setToken("core/target");
            filter3.setValue("base/target");
            final Replace.Replacefilter filter4 = replace.createReplacefilter();
            filter4.setToken("core\\target");
            filter4.setValue("base\\target");
            replace.setProject(RunTest.PROJECT);
            replace.execute();
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/target/test-temp/run/base/target/");
        }
    }

    @Nested
    class ChangeResultBaseTest extends RunTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(RunTest.backup);
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "target/test-temp/run/result");
            System.setProperties(newProperty);
            FileResources.setContext(new FileResources.FileResourcesContext());
            RunTest.clean("target/test-temp/run/result");
        }

    }

    @Nested
    class ChangeDataSetBaseTest extends RunTest.TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(RunTest.backup);
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "target/test-temp/run/dataset");
            System.setProperties(newProperty);
            FileResources.setContext(new FileResources.FileResourcesContext());
            RunTest.clean("target/test-temp/run/dataset");
            RunTest.copy("src/test/resources/yo/dbunitcli/application/src", "target/test-temp/run/dataset/src/test/resources/yo/dbunitcli/application/src");
            RunTest.copy("src/test/resources/yo/dbunitcli/application/expect", "target/test-temp/run/dataset/src/test/resources/yo/dbunitcli/application/expect");
            final Replace replace = new Replace();
            replace.setDir(new File("target/test-temp/run/dataset/src/test/resources/yo/dbunitcli/application/src"));
            replace.setIncludes("**/*.xml");
            replace.setIncludes("**/*.bat");
            replace.setIncludes("**/*.cmd");
            final Replace.Replacefilter filter1 = replace.createReplacefilter();
            filter1.setToken("core/src");
            filter1.setValue("dataset/src");
            final Replace.Replacefilter filter2 = replace.createReplacefilter();
            filter2.setToken("core\\src");
            filter2.setValue("dataset\\src");
            final Replace.Replacefilter filter3 = replace.createReplacefilter();
            filter3.setToken("core/target");
            filter3.setValue("dataset/target");
            final Replace.Replacefilter filter4 = replace.createReplacefilter();
            filter4.setToken("core\\target");
            filter4.setValue("dataset\\target");
            replace.setProject(RunTest.PROJECT);
            replace.execute();
        }

        @Override
        protected String getResult() {
            return super.getResult().replaceAll("/target/", "/target/test-temp/run/dataset/target/");
        }
    }

}