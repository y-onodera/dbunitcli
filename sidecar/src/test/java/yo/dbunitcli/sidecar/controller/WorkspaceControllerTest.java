package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
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
    public void testList() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/workspace/resources"));
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/workspace-resources-response.json"),
                jsonResponse,
                new File("target/test-temp/workspace/sample").getAbsolutePath().replace("\\", "\\\\"),
                new File("dataset").getAbsolutePath().replace("\\", "\\\\"),
                new File("target/resources").getAbsolutePath().replace("\\", "\\\\"),
                new File("target/test-temp/workspace/sample/resources/setting").getAbsolutePath().replace("\\", "\\\\"),
                new File("target/test-temp/workspace/sample/resources/template").getAbsolutePath().replace("\\", "\\\\"),
                new File("target/test-temp/workspace/sample/option/parameterize/template").getAbsolutePath().replace("\\", "\\\\"),
                new File("target/test-temp/workspace/sample/resources/jdbc").getAbsolutePath().replace("\\", "\\\\"),
                new File("target/test-temp/workspace/sample/resources/xlsxSchema").getAbsolutePath().replace("\\", "\\\\")
        );
    }

}