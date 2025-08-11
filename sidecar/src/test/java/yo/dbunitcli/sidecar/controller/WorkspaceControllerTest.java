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

import java.io.File;

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
        Assertions.assertEquals(String.format("{\"parameterList\":{\"convert\":[\"csvToXlsx\"],\"compare\":[],\"generate\":[],\"run\":[],\"parameterize\":[]},\"resources\":{\"queryFiles\":{\"csvq\":[],\"sql\":[],\"table\":[]}},\"context\":{\"workspace\":\"%s\",\"datasetBase\":\"%s\",\"resultBase\":\"%s\",\"settingBase\":\"%s\",\"templateBase\":\"%s\",\"jdbcBase\":\"%s\",\"xlsxSchemaBase\":\"%s\"}}"
                , new File("src/test/resources/workspace/sample").getAbsolutePath().replace("\\", "\\\\")
                , new File("dataset").getAbsolutePath().replace("\\", "\\\\")
                , new File("target/resources").getAbsolutePath().replace("\\", "\\\\")
                , new File("src/test/resources/workspace/sample/resources/setting").getAbsolutePath().replace("\\", "\\\\")
                , new File("src/test/resources/workspace/sample/resources/template").getAbsolutePath().replace("\\", "\\\\")
                , new File("src/test/resources/workspace/sample/resources/jdbc").getAbsolutePath().replace("\\", "\\\\")
                , new File("src/test/resources/workspace/sample/resources/xlsxSchema").getAbsolutePath().replace("\\", "\\\\")
        ), jsonResponse);
    }

}