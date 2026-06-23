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
class ScaffoldControllerTest {
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Workspace workspace;

    @AfterEach
    public void tearDown() throws IOException {
        this.tryDelete("new item");
        this.tryDelete("savedScaffold");
        this.tryDelete("jdbcToScaffold(1)");
        this.tryDelete("renamed");
    }

    private void tryDelete(final String name) throws IOException {
        try {
            this.client.toBlocking().retrieve(
                    HttpRequest.POST("dbunit-cli/scaffold/delete", "{\"name\":\"" + name + "\"}"));
        } catch (final Exception ignored) {
        }
    }

    @Test
    public void testAdd_パラメータを追加する() {
        final String response = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/scaffold/add"));
        Assertions.assertEquals("[\"jdbcToScaffold\",\"new item\"]", response);
    }

    @Test
    public void testCopy_パラメータをコピーする() {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/scaffold/copy", "{\"name\":\"jdbcToScaffold\"}"));
        Assertions.assertEquals("[\"jdbcToScaffold(1)\",\"jdbcToScaffold\"]", response);
    }

    @Test
    public void testDelete_パラメータを削除する() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/scaffold/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/scaffold/delete", "{\"name\":\"new item\"}"));
        Assertions.assertEquals("[\"jdbcToScaffold\"]", response);
    }

    @Test
    public void testRename_パラメータをリネームする() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/scaffold/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/scaffold/rename", "{\"oldName\":\"new item\",\"newName\":\"renamed\"}"));
        Assertions.assertEquals("[\"jdbcToScaffold\",\"renamed\"]", response);
    }

    @Test
    public void testLoad() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/scaffold/load", "{\"name\":\"jdbcToScaffold\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/scaffold-load-response.json"),
                jsonResponse);
    }

    @Test
    public void testReset() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/scaffold/reset"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/scaffold-reset-response.json"),
                jsonResponse);
    }

    @Test
    public void testSave_パラメータを保存して再ロードで確認する() throws IOException {
        final String saveResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/scaffold/save",
                        "{\"name\":\"savedScaffold\",\"input\":{\"-src.srcType\":\"jdbcMetadata\",\"-src.src\":\"resources/src/csv\",\"-result\":\"target/scaffold/result\"}}"));
        Assertions.assertEquals("success", saveResponse);

        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/scaffold/load", "{\"name\":\"savedScaffold\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/scaffold-load-response.json"),
                jsonResponse);
    }
}
