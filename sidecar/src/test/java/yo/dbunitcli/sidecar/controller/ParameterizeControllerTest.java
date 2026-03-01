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
import java.nio.file.Paths;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
public class ParameterizeControllerTest {

    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Workspace workspace;

    @AfterEach
    public void tearDown() {
        // parameterize コマンドのファイルをクリーンアップ
        this.tryDelete("savedParameterize");
        this.tryDelete("new item");
        this.tryDelete("csvConvert(1)");
        this.tryDelete("renamed");
    }

    @Test
    public void testAdd_パラメータを追加する() {
        final String response = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/parameterize/add"));
        Assertions.assertEquals("[\"csvConvert\",\"new item\"]", response);
    }

    @Test
    public void testCopy_パラメータをコピーする() {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/parameterize/copy", "{\"name\":\"csvConvert\"}"));
        Assertions.assertEquals("[\"csvConvert(1)\",\"csvConvert\"]", response);
    }

    @Test
    public void testDelete_パラメータを削除する() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/parameterize/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/parameterize/delete", "{\"name\":\"new item\"}"));
        Assertions.assertEquals("[\"csvConvert\"]", response);
    }

    @Test
    public void testRename_パラメータをリネームする() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/parameterize/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/parameterize/rename", "{\"oldName\":\"new item\",\"newName\":\"renamed\"}"));
        Assertions.assertEquals("[\"csvConvert\",\"renamed\"]", response);
    }

    @Test
    public void testSave_パラメータを保存して再ロードで確認する() throws IOException {
        final String saveResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/parameterize/save",
                        "{\"name\":\"savedParameterize\",\"input\":{\"-cmd\":\"convert\",\"-template\":\"csvToXlsx.txt\",\"-param.src\":\"csvToXlsx.csv\",\"-param.srcType\":\"csv\",\"-unit\":\"record\"}}"));
        Assertions.assertEquals("success", saveResponse);

        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/parameterize/load", "{\"name\":\"savedParameterize\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/parameterize-load-response.json"), jsonResponse);
    }

    @Test
    public void testLoad() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/parameterize/load"
                , "{\"name\":\"csvConvert\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/parameterize-load-response.json"), jsonResponse);
    }

    @Test
    public void testReset() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/parameterize/reset"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/parameterize-reset-response.json"), jsonResponse);
    }

    private void tryDelete(final String name) {
        try {
            this.client.toBlocking().retrieve(
                    HttpRequest.POST("dbunit-cli/parameterize/delete", "{\"name\":\"" + name + "\"}"));
        } catch (final Exception ignored) {
        }
    }
}
