package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.QueryDataSourceDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@MicronautTest
public class QueryDatasourceControllerTest {

    private final String beforeContents = "before test";
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;
    @TempDir
    Path tempDir;
    File target;

    @BeforeEach
    public void setUp() throws IOException {
        System.setProperty(FileResources.PROPERTY_DATASET_BASE, this.tempDir.toString());
        final File parent = new File(this.tempDir.toFile(), "sql");
        Files.createDirectory(parent.toPath());
        this.target = new File(parent, "test.sql");
        Files.createFile(this.target.toPath());
        Files.writeString(this.target.toPath(), this.beforeContents, java.nio.charset.StandardCharsets.UTF_8);
        Assertions.assertTrue(this.target.exists());
    }

    @Test
    void testList() {
        final String response = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/query-datasource/list?type=csv"));
        Assertions.assertNotNull(response);
    }

    @Test
    void testLoad() {
        final QueryDataSourceDto request = new QueryDataSourceDto();
        request.setType(DataSourceType.sql);
        request.setName("test.sql");

        final HttpRequest<QueryDataSourceDto> httpRequest = HttpRequest.POST("dbunit-cli/query-datasource/load", request)
                .contentType(MediaType.APPLICATION_JSON_TYPE);
        final HttpResponse<String> response = this.client.toBlocking().exchange(httpRequest, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals(this.beforeContents, response.body());
    }

    @Test
    void testSave() throws IOException {
        final QueryDataSourceDto request = new QueryDataSourceDto();
        request.setType(DataSourceType.sql);
        request.setName("test.sql");
        final String contents = "test";
        request.setContents(contents);
        final HttpResponse<String> response = this.client.toBlocking().exchange(HttpRequest.POST("dbunit-cli/query-datasource/save", request), String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals("success", response.body());
        Assertions.assertEquals(contents, Files.readString(this.target.toPath()));
    }

    @Test
    void testDelete() {
        final QueryDataSourceDto request = new QueryDataSourceDto();
        request.setType(DataSourceType.sql);
        request.setName("test.sql");
        final HttpRequest<QueryDataSourceDto> httpRequest = HttpRequest.POST("dbunit-cli/query-datasource/delete", request)
                .contentType(MediaType.APPLICATION_JSON_TYPE);
        final HttpResponse<String> response = this.client.toBlocking().exchange(httpRequest, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals("success", response.body());
        Assertions.assertFalse(this.target.exists());
    }

}