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

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "src/test/resources/workspace/sample")
@Property(name = FileResources.PROPERTY_DATASET_BASE, value = "dataset")
@Property(name = FileResources.PROPERTY_RESULT_BASE, value = "target/resources")
class FileContextControllerTest {
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void list() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/context/paths"));
        Assertions.assertEquals("{\"workspace\":\"src\\\\test\\\\resources\\\\workspace\\\\sample\",\"datasetBase\":\"dataset\",\"resultBase\":\"target\\\\resources\"}", jsonResponse);
    }
}