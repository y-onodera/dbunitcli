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

import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class TemplateResourceFileControllerTest {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testList() throws IOException {
        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.GET("dbunit-cli/template/list"));
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/template-list-response.json"),
                response);
    }

    @Test
    void testLoad() throws IOException {
        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/template/load", "sample.json")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        System.out.println(response);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/template-load-response.json"),
                response);
    }

    @AfterEach
    void tearDown() {
        try {
            this.client.toBlocking().retrieve(
                    HttpRequest.POST("dbunit-cli/template/delete", "generatedTemplate.xlsx")
                            .contentType(MediaType.TEXT_PLAIN_TYPE));
        } catch (final Exception ignored) {
        }
        try {
            this.client.toBlocking().retrieve(
                    HttpRequest.POST("dbunit-cli/template/delete", "savedTemplate.stg")
                            .contentType(MediaType.TEXT_PLAIN_TYPE));
        } catch (final Exception ignored) {
        }
    }

    @Test
    void testGenerate_データセットからxlsxテンプレートを生成して保存する() {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/template/generate",
                        "{\"-src.src\":\"resources/src/csv\",\"-src.srcType\":\"csv\",\"-fileName\":\"generatedTemplate\"}"));
        Assertions.assertTrue(response.contains("generatedTemplate.xlsx"), response);
        final Path saved = Paths.get("target/test-temp/workspace/sample/resources/template/generatedTemplate.xlsx");
        Assertions.assertTrue(Files.exists(saved), "拡張子が補われて保存されること");
    }

    @Test
    void testGenerate_src未指定は400を返す() {
        final HttpClientResponseException ex = Assertions.assertThrows(
                HttpClientResponseException.class,
                () -> this.client.toBlocking().retrieve(
                        HttpRequest.POST("dbunit-cli/template/generate", "{\"-fileName\":\"a.xlsx\"}")));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testGenerate_fileNameにパス区切りは400を返す() {
        final HttpClientResponseException ex = Assertions.assertThrows(
                HttpClientResponseException.class,
                () -> this.client.toBlocking().retrieve(
                        HttpRequest.POST("dbunit-cli/template/generate",
                                "{\"-src.src\":\"resources/src/csv\",\"-src.srcType\":\"csv\",\"-fileName\":\"../a\"}")));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testSave_生テキストとして保存しloadで取得できる() throws IOException {
        final String template = "ddlType(row) ::= <<$row.TYPE_NAME$>>\n";
        final String saveBody = "{\"name\":\"savedTemplate.stg\",\"input\":{\"content\":"
                + "\"ddlType(row) ::= <<$row.TYPE_NAME$>>\\n\"}}";
        final String list = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/template/save", saveBody));
        Assertions.assertTrue(list.contains("savedTemplate.stg"), list);
        final Path saved = Paths.get("target/test-temp/workspace/sample/resources/template/savedTemplate.stg");
        Assertions.assertEquals(template, Files.readString(saved));

        final String loaded = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/template/load", "savedTemplate.stg")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        Assertions.assertTrue(loaded.contains("\"content\""), loaded);
        Assertions.assertTrue(loaded.contains("ddlType(row) ::= <<$row.TYPE_NAME$>>"), loaded);
    }
}
