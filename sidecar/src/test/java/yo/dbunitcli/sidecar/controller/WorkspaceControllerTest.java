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
@Property(name = FileResources.PROPERTY_DATASET_BASE, value = "dataset")
@Property(name = FileResources.PROPERTY_RESULT_BASE, value = "target/resources")
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
        System.out.println(jsonResponse);
        Assertions.assertEquals("{\"parameterList\":{\"convert\":[\"csvToXlsx\"],\"compare\":[],\"generate\":[],\"run\":[],\"parameterize\":[]},\"resources\":{},\"context\":{\"workspace\":\"src/test/resources/workspace/sample\",\"datasetBase\":\"dataset\",\"resultBase\":\"target/resources\",\"settingBase\":\"src\\\\test\\\\resources\\\\workspace\\\\sample\\\\resources\\\\setting\",\"templateBase\":\"src\\\\test\\\\resources\\\\workspace\\\\sample\\\\resources\\\\template\",\"jdbcBase\":\"src\\\\test\\\\resources\\\\workspace\\\\sample\\\\resources\\\\jdbc\",\"xlsxSchemaBase\":\"src\\\\test\\\\resources\\\\workspace\\\\sample\\\\resources\\\\xlsxschema\"}}", jsonResponse);
    }

}