package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "src/test/resources/workspace/sample")
class GenerateControllerTest {
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Workspace workspace;

    @Test
    public void testReset() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/generate/reset"));
        final String expectedJson = Files.readString(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/generate-reset-response.json"));
        final String normalizedExpected = this.normalizeJson(expectedJson);
        final String normalizedActual = this.normalizeJson(jsonResponse);
        Assertions.assertEquals(normalizedExpected, normalizedActual);
    }

    /**
     * JSONの空白や改行を取り除いて正規化するヘルパーメソッド
     */
    private String normalizeJson(final String json) {
        // 空白、タブ、改行を削除
        return Pattern.compile("\\s+").matcher(json).replaceAll("");
    }
}
