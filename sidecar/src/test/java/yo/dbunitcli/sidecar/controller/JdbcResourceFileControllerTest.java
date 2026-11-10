package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.resource.FileResources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class JdbcResourceFileControllerTest {

    @Inject
    EmbeddedServer server;

    private static final Path WORKSPACE = Paths.get("target/test-temp/workspace/sample");

    // resources/jdbcの外（workspace直下）に置く元のproperties
    private static final String OUTSIDE = "outsideJdbc.properties";

    @Inject
    @Client("/")
    HttpClient client;

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(WORKSPACE.resolve(OUTSIDE));
        Files.deleteIfExists(WORKSPACE.resolve(FileResources.RESOURCES_JDBC_PATH).resolve(OUTSIDE));
    }

    @Test
    void testLoad_propertiesをテキストのまま返し開いたファイルの場所も返す() {
        final JsonObject response = JsonTestHelper.loadResponseOf(this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/jdbc/load", "{\"name\":\"sample.properties\"}")));
        Assertions.assertTrue(response.getString("content").contains("url"), response.toString());
        Assertions.assertEquals("sample.properties", response.getJsonObject("location").getString("fileName"));
    }

    @Test
    void testSaveProperties_overwriteならresources_jdbcの外で見つかった元のファイルへフォームの値を重ねて書く() throws IOException {
        Files.writeString(WORKSPACE.resolve(OUTSIDE), "url=jdbc:h2:mem:old\nuser=baseUser\n");

        this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/jdbc/save-properties",
                "{\"name\":\"" + OUTSIDE + "\",\"overwrite\":true,\"input\":{\"url\":\"jdbc:h2:mem:new\",\"user\":\"\",\"pass\":\"\",\"properties\":\"" + OUTSIDE + "\"}}"));

        final Properties saved = load(WORKSPACE.resolve(OUTSIDE));
        Assertions.assertEquals("jdbc:h2:mem:new", saved.getProperty("url"));
        Assertions.assertEquals("baseUser", saved.getProperty("user"));
        Assertions.assertFalse(Files.exists(WORKSPACE.resolve(FileResources.RESOURCES_JDBC_PATH).resolve(OUTSIDE)),
                "resources/jdbcには作らない");
    }

    @Test
    void testSaveProperties_overwriteでなければresources_jdbcへ保存し元のファイルは変えない() throws IOException {
        Files.writeString(WORKSPACE.resolve(OUTSIDE), "url=jdbc:h2:mem:old\n");

        this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/jdbc/save-properties",
                "{\"name\":\"" + OUTSIDE + "\",\"input\":{\"url\":\"jdbc:h2:mem:new\",\"user\":\"\",\"pass\":\"\",\"properties\":\"" + OUTSIDE + "\"}}"));

        Assertions.assertEquals("jdbc:h2:mem:old", load(WORKSPACE.resolve(OUTSIDE)).getProperty("url"));
        Assertions.assertEquals("jdbc:h2:mem:new",
                load(WORKSPACE.resolve(FileResources.RESOURCES_JDBC_PATH).resolve(OUTSIDE)).getProperty("url"));
    }

    private static Properties load(final Path path) throws IOException {
        final Properties properties = new Properties();
        try (final InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        }
        return properties;
    }

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
