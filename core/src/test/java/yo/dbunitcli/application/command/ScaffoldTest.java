package yo.dbunitcli.application.command;

import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ScaffoldTest {

    private static final String RESULT_BASE = "target/test-temp/scaffold";

    private static String[] args(final String subDir, final String... extra) {
        final String[] base = {"-result=" + Path.of(RESULT_BASE, subDir)};
        final String[] result = new String[base.length + extra.length];
        System.arraycopy(base, 0, result, 0, base.length);
        System.arraycopy(extra, 0, result, base.length, extra.length);
        return result;
    }

    private static File resultFile(final String subDir, final String relativePath) {
        return Path.of(RESULT_BASE, subDir, relativePath).toFile();
    }

    @Nested
    class DdlTarget {

        @Test
        void all_全ファイルが生成される() {
            Scaffold.main(args("ddl/all", "-target=ddl", "-setting=ddl", "-template=ddl", "-parameter=ddl"));
            assertTrue(resultFile("ddl/all", "resources/setting/ddl.json").exists());
            assertTrue(resultFile("ddl/all", "resources/template/ddl.stg").exists());
            assertTrue(resultFile("ddl/all", "resources/template/ddl.txt").exists());
            assertTrue(resultFile("ddl/all", "resources/param/ddl.param").exists());
        }

        @Test
        void settingOnly_settingのみ生成される() {
            Scaffold.main(args("ddl/setting", "-target=ddl", "-setting=ddl"));
            assertTrue(resultFile("ddl/setting", "resources/setting/ddl.json").exists());
            assertFalse(resultFile("ddl/setting", "resources/template/ddl.stg").exists());
            assertFalse(resultFile("ddl/setting", "resources/template/ddl.txt").exists());
            assertFalse(resultFile("ddl/setting", "resources/param/ddl.param").exists());
        }

        @Test
        void templateOnly_templateのみ生成される() {
            Scaffold.main(args("ddl/template", "-target=ddl", "-template=ddl"));
            assertFalse(resultFile("ddl/template", "resources/setting/ddl.json").exists());
            assertTrue(resultFile("ddl/template", "resources/template/ddl.stg").exists());
            assertTrue(resultFile("ddl/template", "resources/template/ddl.txt").exists());
            assertFalse(resultFile("ddl/template", "resources/param/ddl.param").exists());
        }

        @Test
        void parameterOnly_parameterのみ生成される() {
            Scaffold.main(args("ddl/parameter", "-target=ddl", "-parameter=ddl"));
            assertFalse(resultFile("ddl/parameter", "resources/setting/ddl.json").exists());
            assertFalse(resultFile("ddl/parameter", "resources/template/ddl.stg").exists());
            assertFalse(resultFile("ddl/parameter", "resources/template/ddl.txt").exists());
            assertTrue(resultFile("ddl/parameter", "resources/param/ddl.param").exists());
        }

        @Test
        void settingAndTemplate_settingとtemplateが生成される() {
            Scaffold.main(args("ddl/setting-template", "-target=ddl", "-setting=ddl", "-template=ddl"));
            assertTrue(resultFile("ddl/setting-template", "resources/setting/ddl.json").exists());
            assertTrue(resultFile("ddl/setting-template", "resources/template/ddl.stg").exists());
            assertTrue(resultFile("ddl/setting-template", "resources/template/ddl.txt").exists());
            assertFalse(resultFile("ddl/setting-template", "resources/param/ddl.param").exists());
        }

        @Test
        void カスタム名で生成される() {
            Scaffold.main(args("ddl/custom", "-target=ddl", "-setting=myDdl", "-template=myDdl", "-parameter=myDdl"));
            assertTrue(resultFile("ddl/custom", "resources/setting/myDdl.json").exists());
            assertTrue(resultFile("ddl/custom", "resources/template/myDdl.stg").exists());
            assertTrue(resultFile("ddl/custom", "resources/template/myDdl.txt").exists());
            assertTrue(resultFile("ddl/custom", "resources/param/myDdl.param").exists());
        }
    }

    @Nested
    class JavaBeanTarget {

        @Test
        void all_全ファイルが生成される() {
            Scaffold.main(args("javaBean/all", "-target=javaBean", "-setting=javaBean", "-template=javaBean", "-parameter=javaBean"));
            assertTrue(resultFile("javaBean/all", "resources/setting/javaBean.json").exists());
            assertTrue(resultFile("javaBean/all", "resources/template/javaBean.stg").exists());
            assertTrue(resultFile("javaBean/all", "resources/template/javaBean.txt").exists());
            assertTrue(resultFile("javaBean/all", "resources/param/javaBean.param").exists());
        }

        @Test
        void settingOnly_settingのみ生成される() {
            Scaffold.main(args("javaBean/setting", "-target=javaBean", "-setting=javaBean"));
            assertTrue(resultFile("javaBean/setting", "resources/setting/javaBean.json").exists());
            assertFalse(resultFile("javaBean/setting", "resources/template/javaBean.stg").exists());
            assertFalse(resultFile("javaBean/setting", "resources/template/javaBean.txt").exists());
            assertFalse(resultFile("javaBean/setting", "resources/param/javaBean.param").exists());
        }

        @Test
        void templateOnly_templateのみ生成される() {
            Scaffold.main(args("javaBean/template", "-target=javaBean", "-template=javaBean"));
            assertFalse(resultFile("javaBean/template", "resources/setting/javaBean.json").exists());
            assertTrue(resultFile("javaBean/template", "resources/template/javaBean.stg").exists());
            assertTrue(resultFile("javaBean/template", "resources/template/javaBean.txt").exists());
            assertFalse(resultFile("javaBean/template", "resources/param/javaBean.param").exists());
        }

        @Test
        void parameterOnly_parameterのみ生成される() {
            Scaffold.main(args("javaBean/parameter", "-target=javaBean", "-parameter=javaBean"));
            assertFalse(resultFile("javaBean/parameter", "resources/setting/javaBean.json").exists());
            assertFalse(resultFile("javaBean/parameter", "resources/template/javaBean.stg").exists());
            assertFalse(resultFile("javaBean/parameter", "resources/template/javaBean.txt").exists());
            assertTrue(resultFile("javaBean/parameter", "resources/param/javaBean.param").exists());
        }

        @Test
        void settingAndTemplate_settingとtemplateが生成される() {
            Scaffold.main(args("javaBean/setting-template", "-target=javaBean", "-setting=javaBean", "-template=javaBean"));
            assertTrue(resultFile("javaBean/setting-template", "resources/setting/javaBean.json").exists());
            assertTrue(resultFile("javaBean/setting-template", "resources/template/javaBean.stg").exists());
            assertTrue(resultFile("javaBean/setting-template", "resources/template/javaBean.txt").exists());
            assertFalse(resultFile("javaBean/setting-template", "resources/param/javaBean.param").exists());
        }
    }

    @Nested
    class NoOutput {

        @Test
        void パラメータ未指定で何も生成されない() {
            Scaffold.main(args("nooutput/ddl", "-target=ddl"));
            assertFalse(resultFile("nooutput/ddl", "resources/setting/ddl.json").exists());
            assertFalse(resultFile("nooutput/ddl", "resources/template/ddl.stg").exists());
            assertFalse(resultFile("nooutput/ddl", "resources/param/ddl.param").exists());
        }
    }

    @Nested
    class ParameterTarget {

        @Test
        void commandTypeを指定するとparamファイルが生成される() throws Exception {
            Scaffold.main(args("parameter/generate", "-target=parameter", "-commandType=generate"));
            final File paramFile = resultFile("parameter/generate", "resources/param/generate.param");
            assertTrue(paramFile.exists());
            assertTrue(Files.size(paramFile.toPath()) > 0);
            assertFalse(resultFile("parameter/generate", "resources/setting/ddl.json").exists());
            assertFalse(resultFile("parameter/generate", "resources/setting/javaBean.json").exists());
        }

        @Test
        void generateTargetsが空のとき何も生成されない() {
            Scaffold.main(args("empty"));
            assertFalse(resultFile("empty", "resources/setting/ddl.json").exists());
            assertFalse(resultFile("empty", "resources/setting/javaBean.json").exists());
            assertFalse(resultFile("empty", "resources/template/ddl.stg").exists());
            assertFalse(resultFile("empty", "resources/template/javaBean.stg").exists());
        }
    }
}
