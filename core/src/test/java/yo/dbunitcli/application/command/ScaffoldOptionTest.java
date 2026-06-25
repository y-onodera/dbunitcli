package yo.dbunitcli.application.command;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("IntegrationTest")
@DisabledInNativeImage
class ScaffoldOptionTest {

    private static final String JDBC_URL = "jdbc:h2:mem:scaffoldtest;DB_CLOSE_DELAY=-1";
    private static final String JDBC_USER = "admin";
    private static final String JDBC_PASS = "admin";
    private static final String RESULT_BASE = "target/test-temp/scaffold";

    @BeforeAll
    static void createTable() throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS DOCUMENT (ID INTEGER PRIMARY KEY, NAME VARCHAR(40))");
        }
    }

    @AfterAll
    static void dropTable() throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
             Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS DOCUMENT");
        }
    }

    private String[] baseArgs(final String subDir, final String... extra) {
        final String[] base = {
                "-result=" + Path.of(RESULT_BASE, subDir),
                "-src.srcType=jdbcMetadata",
                "-src.jdbcUrl=" + JDBC_URL,
                "-src.jdbcUser=" + JDBC_USER,
                "-src.jdbcPass=" + JDBC_PASS,
                "-src.regTableInclude=(?i)DOCUMENT"
        };
        return Stream.concat(Arrays.stream(base), Arrays.stream(extra)).toArray(String[]::new);
    }

    private File resultFile(final String subDir, final String relativePath) {
        return Path.of(RESULT_BASE, subDir, relativePath).toFile();
    }

    @Nested
    class DdlTarget {

        @Test
        void allIncludes_全ファイルが生成される() {
            Scaffold.main(baseArgs("ddl/all", "-target=ddl"));
            assertTrue(resultFile("ddl/all", "resources/setting/ddl.json").exists());
            assertTrue(resultFile("ddl/all", "resources/template/ddl.stg").exists());
            assertTrue(resultFile("ddl/all", "resources/template/ddl.txt").exists());
            assertTrue(resultFile("ddl/all", "resources/param/DOCUMENT_ddl.param").exists());
        }

        @Test
        void settingOnly_settingのみ生成される() {
            Scaffold.main(baseArgs("ddl/setting", "-target=ddl", "-ddlIncludes=setting"));
            assertTrue(resultFile("ddl/setting", "resources/setting/ddl.json").exists());
            assertFalse(resultFile("ddl/setting", "resources/template/ddl.stg").exists());
            assertFalse(resultFile("ddl/setting", "resources/template/ddl.txt").exists());
            assertFalse(resultFile("ddl/setting", "resources/param/DOCUMENT_ddl.param").exists());
        }

        @Test
        void templateOnly_templateのみ生成される() {
            Scaffold.main(baseArgs("ddl/template", "-target=ddl", "-ddlIncludes=template"));
            assertFalse(resultFile("ddl/template", "resources/setting/ddl.json").exists());
            assertTrue(resultFile("ddl/template", "resources/template/ddl.stg").exists());
            assertTrue(resultFile("ddl/template", "resources/template/ddl.txt").exists());
            assertFalse(resultFile("ddl/template", "resources/param/DOCUMENT_ddl.param").exists());
        }

        @Test
        void parameterOnly_parameterのみ生成される() {
            Scaffold.main(baseArgs("ddl/parameter", "-target=ddl", "-ddlIncludes=parameter"));
            assertFalse(resultFile("ddl/parameter", "resources/setting/ddl.json").exists());
            assertFalse(resultFile("ddl/parameter", "resources/template/ddl.stg").exists());
            assertFalse(resultFile("ddl/parameter", "resources/template/ddl.txt").exists());
            assertTrue(resultFile("ddl/parameter", "resources/param/DOCUMENT_ddl.param").exists());
        }

        @Test
        void settingAndTemplate_settingとtemplateが生成される() {
            Scaffold.main(baseArgs("ddl/setting-template", "-target=ddl", "-ddlIncludes=setting,template"));
            assertTrue(resultFile("ddl/setting-template", "resources/setting/ddl.json").exists());
            assertTrue(resultFile("ddl/setting-template", "resources/template/ddl.stg").exists());
            assertTrue(resultFile("ddl/setting-template", "resources/template/ddl.txt").exists());
            assertFalse(resultFile("ddl/setting-template", "resources/param/DOCUMENT_ddl.param").exists());
        }
    }

    @Nested
    class JavaBeanTarget {

        @Test
        void allIncludes_全ファイルが生成される() {
            Scaffold.main(baseArgs("javaBean/all", "-target=javaBean"));
            assertTrue(resultFile("javaBean/all", "resources/setting/javaBean.json").exists());
            assertTrue(resultFile("javaBean/all", "resources/template/javaBean.stg").exists());
            assertTrue(resultFile("javaBean/all", "resources/template/javaBean.txt").exists());
            assertTrue(resultFile("javaBean/all", "resources/param/DOCUMENT_javaBean.param").exists());
        }

        @Test
        void settingOnly_settingのみ生成される() {
            Scaffold.main(baseArgs("javaBean/setting", "-target=javaBean", "-javaBeanIncludes=setting"));
            assertTrue(resultFile("javaBean/setting", "resources/setting/javaBean.json").exists());
            assertFalse(resultFile("javaBean/setting", "resources/template/javaBean.stg").exists());
            assertFalse(resultFile("javaBean/setting", "resources/template/javaBean.txt").exists());
            assertFalse(resultFile("javaBean/setting", "resources/param/DOCUMENT_javaBean.param").exists());
        }

        @Test
        void templateOnly_templateのみ生成される() {
            Scaffold.main(baseArgs("javaBean/template", "-target=javaBean", "-javaBeanIncludes=template"));
            assertFalse(resultFile("javaBean/template", "resources/setting/javaBean.json").exists());
            assertTrue(resultFile("javaBean/template", "resources/template/javaBean.stg").exists());
            assertTrue(resultFile("javaBean/template", "resources/template/javaBean.txt").exists());
            assertFalse(resultFile("javaBean/template", "resources/param/DOCUMENT_javaBean.param").exists());
        }

        @Test
        void parameterOnly_parameterのみ生成される() {
            Scaffold.main(baseArgs("javaBean/parameter", "-target=javaBean", "-javaBeanIncludes=parameter"));
            assertFalse(resultFile("javaBean/parameter", "resources/setting/javaBean.json").exists());
            assertFalse(resultFile("javaBean/parameter", "resources/template/javaBean.stg").exists());
            assertFalse(resultFile("javaBean/parameter", "resources/template/javaBean.txt").exists());
            assertTrue(resultFile("javaBean/parameter", "resources/param/DOCUMENT_javaBean.param").exists());
        }

        @Test
        void settingAndTemplate_settingとtemplateが生成される() {
            Scaffold.main(baseArgs("javaBean/setting-template", "-target=javaBean", "-javaBeanIncludes=setting,template"));
            assertTrue(resultFile("javaBean/setting-template", "resources/setting/javaBean.json").exists());
            assertTrue(resultFile("javaBean/setting-template", "resources/template/javaBean.stg").exists());
            assertTrue(resultFile("javaBean/setting-template", "resources/template/javaBean.txt").exists());
            assertFalse(resultFile("javaBean/setting-template", "resources/param/DOCUMENT_javaBean.param").exists());
        }
    }

    @Nested
    class NoSrcData {

        private String[] argsWithoutSrc(final String subDir, final String... extra) {
            return Stream.concat(
                    Arrays.stream(new String[]{"-result=" + Path.of(RESULT_BASE, subDir)}),
                    Arrays.stream(extra)
            ).toArray(String[]::new);
        }

        @Test
        void srcdata未指定でddlの全ファイルが生成される() throws Exception {
            Scaffold.main(argsWithoutSrc("nosrc/ddl", "-target=ddl"));
            assertTrue(resultFile("nosrc/ddl", "resources/setting/ddl.json").exists());
            assertTrue(resultFile("nosrc/ddl", "resources/template/ddl.stg").exists());
            assertTrue(resultFile("nosrc/ddl", "resources/template/ddl.txt").exists());
            final File paramFile = resultFile("nosrc/ddl", "resources/param/ddl.param");
            assertTrue(paramFile.exists());
            assertTrue(Files.size(paramFile.toPath()) > 0);
        }

        @Test
        void srcdata未指定でjavaBeanの全ファイルが生成される() throws Exception {
            Scaffold.main(argsWithoutSrc("nosrc/javaBean", "-target=javaBean"));
            assertTrue(resultFile("nosrc/javaBean", "resources/setting/javaBean.json").exists());
            assertTrue(resultFile("nosrc/javaBean", "resources/template/javaBean.stg").exists());
            assertTrue(resultFile("nosrc/javaBean", "resources/template/javaBean.txt").exists());
            final File paramFile = resultFile("nosrc/javaBean", "resources/param/javaBean.param");
            assertTrue(paramFile.exists());
            assertTrue(Files.size(paramFile.toPath()) > 0);
        }
    }

    @Nested
    class ParameterTarget {

        @Test
        void commandTypeを指定するとparamファイルが生成される() throws Exception {
            Scaffold.main(baseArgs("parameter/generate",
                    "-target=parameter",
                    "-commandType=generate"));
            final File paramFile = resultFile("parameter/generate", "resources/param/generate.param");
            assertTrue(paramFile.exists());
            assertTrue(Files.size(paramFile.toPath()) > 0);
            assertFalse(resultFile("parameter/generate", "resources/setting/ddl.json").exists());
            assertFalse(resultFile("parameter/generate", "resources/setting/javaBean.json").exists());
        }

        @Test
        void generateTargetsが空のとき何も生成されない() {
            Scaffold.main(baseArgs("empty"));
            assertFalse(resultFile("empty", "resources/setting/ddl.json").exists());
            assertFalse(resultFile("empty", "resources/setting/javaBean.json").exists());
            assertFalse(resultFile("empty", "resources/template/ddl.stg").exists());
            assertFalse(resultFile("empty", "resources/template/javaBean.stg").exists());
        }
    }
}
