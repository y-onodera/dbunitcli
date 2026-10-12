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
class ParameterizeTemplateResourceFileControllerTest {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testLoad() throws IOException {
        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/parameterize/template/load", "sample.json")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/parameterize-template-load-response.json"),
                response);
    }

    @Test
    void testSave_生テキストとして保存しloadで取得できる() throws IOException {
        final String saveBody = "{\"name\":\"savedParamTemplate.txt\",\"input\":{\"content\":\"hello $name$\\n\"}}";
        final String list = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/parameterize/template/save", saveBody));
        Assertions.assertTrue(list.contains("savedParamTemplate.txt"), list);
        final Path saved = this.savedPath();
        Assertions.assertEquals("hello $name$\n", Files.readString(saved));

        final String loaded = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/parameterize/template/load", "savedParamTemplate.txt")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        Assertions.assertTrue(loaded.contains("\"content\""), loaded);
        Assertions.assertTrue(loaded.contains("hello $name$"), loaded);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(this.savedPath());
    }

    private Path savedPath() {
        return Paths.get("target/test-temp/workspace/sample/option/parameterize/template/savedParamTemplate.txt");
    }
}
