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

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "src/test/resources/workspace/sample")
class WorkspaceControllerTest {
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Workspace workspace;

    @Test
    public void testList() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/workspace/resources"));
        Assertions.assertEquals("{\"parameterList\":{\"convert\":[\"csvToXlsx\"],\"compare\":[],\"generate\":[],\"run\":[],\"parameterize\":[]},\"resources\":{\"datasetSettings\":[]}}", jsonResponse);
    }

}