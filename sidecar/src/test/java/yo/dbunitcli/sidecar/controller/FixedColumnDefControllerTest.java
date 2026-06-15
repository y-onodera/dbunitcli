package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.resource.FileResources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class FixedColumnDefControllerTest {

    private static final Path DEF_DIR =
            Paths.get("target/test-temp/workspace/sample/resources/fixed-column-def");

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @AfterEach
    void tearDown() {
        this.tryDelete("savedDef.json");
        this.tryDelete("deleteDef.json");
    }

    private void tryDelete(final String name) {
        try {
            this.client.toBlocking().retrieve(
                    HttpRequest.POST("dbunit-cli/fixed-column-def/delete", name)
                            .contentType(MediaType.TEXT_PLAIN_TYPE));
        } catch (final Exception ignored) {
        }
    }

    @Test
    void testList() throws IOException {
        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.GET("dbunit-cli/fixed-column-def/list"));
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/fixed-column-def-list-response.json"),
                response);
    }

    @Test
    void testLoad() throws IOException {
        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/fixed-column-def/load", "sample.json")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        System.out.println(response);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/fixed-column-def-load-response.json"),
                response);
    }

    @Test
    void testSave_保存して再ロードで確認する() throws IOException {
        final String saveResponse = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/fixed-column-def/save",
                        "{\"name\":\"savedDef.json\",\"input\":{\"columns\":[{\"name\":\"code\",\"length\":5,\"align\":\"right\",\"pad\":\"0\"}]}}"));
        System.out.println(saveResponse);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/fixed-column-def-save-list-response.json"),
                saveResponse);

        Assertions.assertTrue(
                Files.exists(DEF_DIR.resolve("savedDef.json")),
                "savedDef.json がファイルシステムに作成されること");

        final String loadResponse = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/fixed-column-def/load", "savedDef.json")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        System.out.println(loadResponse);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/fixed-column-def-save-load-response.json"),
                loadResponse);
    }

    @Test
    void testDelete_ファイルを削除する() throws IOException {
        this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/fixed-column-def/save",
                        "{\"name\":\"deleteDef.json\",\"input\":{\"columns\":[]}}"));

        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/fixed-column-def/delete", "deleteDef.json")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        System.out.println(response);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/fixed-column-def-list-response.json"),
                response);
        Assertions.assertFalse(
                Files.exists(DEF_DIR.resolve("deleteDef.json")),
                "deleteDef.json がファイルシステムから削除されること");
    }
}
