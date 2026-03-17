package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class RunControllerTest {

    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Workspace workspace;

    private static final String WORKSPACE = "target/test-temp/workspace/sample";

    @AfterEach
    public void tearDown() throws IOException {
        // parameterize コマンドの in-memory state も含めてクリーンアップ
        this.tryDeleteCommand("parameterize", "runSql");
        Files.deleteIfExists(Paths.get(WORKSPACE, "option/parameterize/template/runSql.txt"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "runSql.csv"));

        // run コマンドのファイルをクリーンアップ
        this.tryDelete("savedRun");
        this.tryDelete("new item");
        this.tryDelete("runSql(1)");
        this.tryDelete("renamed");
    }

    @Test
    public void testAdd_パラメータを追加する() {
        final String response = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/run/add"));
        Assertions.assertEquals("[\"new item\",\"runSql\"]", response);
    }

    @Test
    public void testCopy_パラメータをコピーする() {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/run/copy", "{\"name\":\"runSql\"}"));
        Assertions.assertEquals("[\"runSql(1)\",\"runSql\"]", response);
    }

    @Test
    public void testDelete_パラメータを削除する() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/run/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/run/delete", "{\"name\":\"new item\"}"));
        Assertions.assertEquals("[\"runSql\"]", response);
    }

    @Test
    public void testRename_パラメータをリネームする() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/run/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/run/rename", "{\"oldName\":\"new item\",\"newName\":\"renamed\"}"));
        Assertions.assertEquals("[\"renamed\",\"runSql\"]", response);
    }

    @Test
    public void testParameterize_runパラメータからparameterizeコマンドを生成する() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/run/parameterize", "{\"name\":\"runSql\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/run-parameterize-response.json"), jsonResponse);
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "runSql.csv")),
                "param.src に指定する runSql.csv がワークスペース直下に作成されること");
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "option/parameterize/template/runSql.txt")),
                "テンプレートファイルが option/parameterize/template/runSql.txt に作成されること");
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "option/parameterize/runSql.txt")),
                "parameterize パラメータファイルが option/parameterize/runSql.txt に作成されること");
    }

    @Test
    public void testSave_パラメータを保存して再ロードで確認する() throws IOException {
        final String saveResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/run/save",
                        "{\"name\":\"savedRun\",\"input\":{\"-scriptType\":\"sql\",\"-src.src\":\"resources/sql/sample.sql\"}}"));
        Assertions.assertEquals("success", saveResponse);

        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/run/load", "{\"name\":\"savedRun\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/run-load-response.json"), jsonResponse);
    }

    @Test
    public void testLoad() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/run/load"
                , "{\"name\":\"runSql\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/run-load-response.json"), jsonResponse);
    }

    @Test
    public void testReset() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/run/reset"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/run-reset-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_scriptTypeSql_デフォルト構造が返る() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/run/refresh", "{\"-scriptType\":\"sql\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/run-refresh-scriptType-sql-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_scriptTypeCmd_構造が返る() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/run/refresh", "{\"-scriptType\":\"cmd\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/run-refresh-scriptType-cmd-response.json"), jsonResponse);
    }

    private void tryDelete(final String name) {
        this.tryDeleteCommand("run", name);
    }

    private void tryDeleteCommand(final String command, final String name) {
        try {
            this.client.toBlocking().retrieve(
                    HttpRequest.POST("dbunit-cli/" + command + "/delete", "{\"name\":\"" + name + "\"}"));
        } catch (final Exception ignored) {
        }
    }
}
