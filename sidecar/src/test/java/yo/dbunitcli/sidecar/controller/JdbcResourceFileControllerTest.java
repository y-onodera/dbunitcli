package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.resource.FileResources;

import java.io.IOException;
import java.nio.file.Paths;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class JdbcResourceFileControllerTest {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testList() throws IOException {
        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.GET("dbunit-cli/jdbc/list"));
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/jdbc-list-response.json"),
                response);
    }

    @Test
    void testReadContent() throws IOException {
        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/jdbc/read-content", "sample.properties")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/jdbc-read-content-response.json"),
                response);
    }
}
