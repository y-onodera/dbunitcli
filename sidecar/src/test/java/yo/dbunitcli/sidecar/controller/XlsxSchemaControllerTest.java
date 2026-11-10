package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.resource.FileResources;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class XlsxSchemaControllerTest {

    private static final Path SCHEMA_DIR =
            Paths.get("target/test-temp/workspace/sample/resources/xlsx-schema");


    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @AfterEach
    void tearDown() {
        this.tryDelete("savedSchema.json");
        this.tryDelete("deleteSchema.json");
    }

    private void tryDelete(final String name) {
        try {
            this.client.toBlocking().retrieve(
                    HttpRequest.POST("dbunit-cli/xlsx-schema/delete", name)
                            .contentType(MediaType.TEXT_PLAIN_TYPE));
        } catch (final Exception ignored) {
        }
    }

    @Test
    void testList() throws IOException {
        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.GET("dbunit-cli/xlsx-schema/list"));
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/xlsx-schema-list-response.json"),
                response);
    }

    @Test
    void testLoad() throws IOException {
        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/xlsx-schema/load", "{\"name\":\"schema.json\"}"));
        System.out.println(response);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/xlsx-schema-load-response.json"),
                JsonTestHelper.contentOf(response));
    }

    @Test
    void testSave_保存して再ロードで確認する() throws IOException {
        final String saveResponse = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/xlsx-schema/save",
                        "{\"name\":\"savedSchema.json\",\"input\":{\"rows\":[{\"sheetName\":\"Sheet2\",\"tableName\":\"TABLE_B\",\"header\":[\"id\",\"value\"],\"dataStart\":2,\"columnIndex\":[1,2],\"breakKey\":[],\"addFileInfo\":false}],\"cells\":[]}}"));
        System.out.println(saveResponse);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/xlsx-schema-save-list-response.json"),
                saveResponse);

        Assertions.assertTrue(
                Files.exists(SCHEMA_DIR.resolve("savedSchema.json")),
                "savedSchema.json がファイルシステムに作成されること");

        final String loadResponse = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/xlsx-schema/load", "{\"name\":\"savedSchema.json\"}"));
        System.out.println(loadResponse);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/xlsx-schema-save-load-response.json"),
                JsonTestHelper.contentOf(loadResponse));
    }

    @Test
    void testSave_シートのパターンを保存して再ロードで確認する() throws IOException {
        this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/xlsx-schema/save",
                        "{\"name\":\"savedSchema.json\",\"input\":{\"patterns\":{\"regex:Sheet.* exclude:Sheet9\":{\"regex\":\"Sheet.*\",\"exclude\":[\"Sheet9\"]}},\"rows\":[{\"sheetName\":\"regex:Sheet.* exclude:Sheet9\",\"tableName\":\"TABLE_B\",\"header\":[\"id\",\"value\"],\"dataStart\":2,\"columnIndex\":[1,2],\"breakKey\":[],\"addFileInfo\":false}],\"cells\":[]}}"));

        final String loadResponse = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/xlsx-schema/load", "{\"name\":\"savedSchema.json\"}"));
        System.out.println(loadResponse);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/xlsx-schema-save-patterns-load-response.json"),
                JsonTestHelper.contentOf(loadResponse));
    }

    @Test
    void testGenerate_データセットからxlsxSchemaテンプレートを生成する() throws IOException {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/xlsx-schema/generate",
                        "{\"-src.src\":\"resources/src/csv\",\"-src.srcType\":\"csv\"}"));
        System.out.println(response);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/xlsx-schema-generate-response.json"),
                response);
    }

    @Test
    void testSave_定義ミスのあるスキーマは保存せず400を返す() {
        final HttpClientResponseException ex = Assertions.assertThrows(
                HttpClientResponseException.class,
                () -> this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/xlsx-schema/save",
                        "{\"name\":\"savedSchema.json\",\"input\":{\"rows\":[],\"cells\":[{\"sheetName\":\"Sheet1\",\"tableName\":\"TABLE_C\",\"header\":[\"id\"],\"rows\":[{\"cellAddress\":[\"$A$1\"]}],\"addFileInfo\":false}]}}")));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        Assertions.assertTrue(ex.getResponse().getBody(String.class).orElse("").contains("cellAddress '$A$1' is not A1 format"));
        Assertions.assertFalse(Files.exists(SCHEMA_DIR.resolve("savedSchema.json")), "savedSchema.json は作成されないこと");
    }

    @Test
    void testGenerate_src未指定は400を返す() {
        final HttpClientResponseException ex = Assertions.assertThrows(
                HttpClientResponseException.class,
                () -> this.client.toBlocking().retrieve(
                        HttpRequest.POST("dbunit-cli/xlsx-schema/generate", "{}")));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testDelete_ファイルを削除する() throws IOException {
        this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/xlsx-schema/save",
                        "{\"name\":\"deleteSchema.json\",\"input\":{\"rows\":[],\"cells\":[]}}"));

        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/xlsx-schema/delete", "deleteSchema.json")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        System.out.println(response);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/xlsx-schema-list-response.json"),
                response);
        Assertions.assertFalse(
                Files.exists(SCHEMA_DIR.resolve("deleteSchema.json")),
                "deleteSchema.json がファイルシステムから削除されること");
    }
}
