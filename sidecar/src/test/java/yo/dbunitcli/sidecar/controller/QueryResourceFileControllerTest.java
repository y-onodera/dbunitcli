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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import yo.dbunitcli.resource.FileResources;

/**
 * sql/table/csvqはworkspaceのresources配下に生テキストとして保存する
 */
@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class QueryResourceFileControllerTest {

    private static final String FILE_NAME = "savedQuery.sql";

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @ParameterizedTest
    @ValueSource(strings = {"sql", "table", "csvq"})
    void save_load_delete(final String resource) throws IOException {
        final String saveBody = "{\"name\":\"" + FILE_NAME + "\",\"input\":{\"content\":\"select * from t\\n\"}}";
        final String saved = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/" + resource + "/save", saveBody));
        Assertions.assertEquals("[\"" + FILE_NAME + "\"]", saved);
        Assertions.assertEquals("select * from t\n", Files.readString(this.savedPath(resource)));

        final String loaded = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/" + resource + "/load", FILE_NAME)
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        Assertions.assertEquals("{\"content\":\"select * from t\\n\"}", loaded);

        final String list = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/" + resource + "/list"));
        Assertions.assertEquals("[\"" + FILE_NAME + "\"]", list);

        final String deleted = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/" + resource + "/delete", FILE_NAME)
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        Assertions.assertEquals("[]", deleted);
        Assertions.assertFalse(Files.exists(this.savedPath(resource)));
    }

    @AfterEach
    void tearDown() throws IOException {
        for (final String resource : new String[]{"sql", "table", "csvq"}) {
            Files.deleteIfExists(this.savedPath(resource));
        }
    }

    private Path savedPath(final String resource) {
        return Paths.get("target/test-temp/workspace/sample/resources", resource, FILE_NAME);
    }
}
