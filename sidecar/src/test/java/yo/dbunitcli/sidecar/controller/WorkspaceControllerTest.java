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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
                new File("target/test-temp/workspace/sample/resources/xlsxSchema").getAbsolutePath().replace("\\", "\\\\"),
                new File("target/test-temp/workspace/sample/resources/fixed-column-def").getAbsolutePath().replace("\\", "\\\\"),
                new File("target/test-temp/workspace/sample/resources/sql").getAbsolutePath().replace("\\", "\\\\"),
                new File("target/test-temp/workspace/sample/resources/table").getAbsolutePath().replace("\\", "\\\\"),
                new File("target/test-temp/workspace/sample/resources/csvq").getAbsolutePath().replace("\\", "\\\\")
        );
    }

    @Test
    public void testResolvePath_sqlはresources配下を優先する() throws IOException {
        final Path sql = Paths.get("target/test-temp/workspace/sample/resources/sql/resolve.sql");
        final Path legacy = Paths.get("dataset/resolve.sql");
        try {
            Files.createDirectories(sql.getParent());
            Files.writeString(sql, "");
            Files.createDirectories(legacy.getParent());
            Files.writeString(legacy, "");
            Assertions.assertEquals(sql.toFile().getAbsolutePath(), this.resolvePath("resolve.sql", "SQL"));
        } finally {
            Files.deleteIfExists(sql);
            Files.deleteIfExists(legacy);
            Files.deleteIfExists(legacy.getParent());
        }
    }

    @Test
    public void testResolvePath_sqlはresources配下に無ければdatasetを探す() throws IOException {
        final Path legacy = Paths.get("dataset/legacy.sql");
        try {
            Files.createDirectories(legacy.getParent());
            Files.writeString(legacy, "");
            Assertions.assertEquals(legacy.toFile().getAbsolutePath(), this.resolvePath("legacy.sql", "SQL"));
            Assertions.assertEquals("", this.resolvePath("legacy.sql", "SETTING"));
        } finally {
            Files.deleteIfExists(legacy);
            Files.deleteIfExists(legacy.getParent());
        }
    }

    private String resolvePath(final String path, final String defaultPath) {
        final String body = "{\"path\":\"" + path + "\",\"defaultPath\":\"" + defaultPath + "\"}";
        return this.client.toBlocking()
                .exchange(HttpRequest.POST("dbunit-cli/workspace/resolve-path", body), String.class)
                .getBody()
                .orElse("");
    }
}
