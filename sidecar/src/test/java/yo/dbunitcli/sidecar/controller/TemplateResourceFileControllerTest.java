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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import java.util.List;

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
                .retrieve(HttpRequest.POST("dbunit-cli/template/load", "{\"name\":\"sample.json\"}"));
        System.out.println(response);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/template-load-response.json"),
                JsonTestHelper.contentOf(response));
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

    private static final String COMPARE_REQUEST = """
            {"fileName":"generatedTemplate","compareSheetName":"compare",
             "first":{"sheetName":"old","columns":["ID","NAME"]},
             "second":{"sheetName":"new","columns":["CODE","NAME"]},
             "keys":[{"first":"ID","second":"CODE"}],
             "compares":[{"first":"NAME","second":"NAME"}]}
            """;

    @Test
    void testGenerateCompare_比較用xlsxテンプレートを生成して保存する() throws IOException {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/template/generate-compare", COMPARE_REQUEST));
        Assertions.assertTrue(response.contains("generatedTemplate.xlsx"), response);
        final Path saved = Paths.get("target/test-temp/workspace/sample/resources/template/generatedTemplate.xlsx");
        try (final Workbook workbook = WorkbookFactory.create(saved.toFile())) {
            Assertions.assertEquals(List.of("old", "new", "compare")
                    , List.of(workbook.getSheetName(0), workbook.getSheetName(1), workbook.getSheetName(2)));
        }
    }

    @Test
    void testGenerateCompare_存在しない列は400を返す() {
        final HttpClientResponseException ex = Assertions.assertThrows(
                HttpClientResponseException.class,
                () -> this.client.toBlocking().retrieve(
                        HttpRequest.POST("dbunit-cli/template/generate-compare", COMPARE_REQUEST.replace("\"CODE\"}", "\"MISSING\"}"))));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        Assertions.assertFalse(Files.exists(Paths.get("target/test-temp/workspace/sample/resources/template/generatedTemplate.xlsx")));
    }

    @Test
    void testGenerateCompare_fileNameにパス区切りは400を返す() {
        final HttpClientResponseException ex = Assertions.assertThrows(
                HttpClientResponseException.class,
                () -> this.client.toBlocking().retrieve(
                        HttpRequest.POST("dbunit-cli/template/generate-compare", COMPARE_REQUEST.replace("generatedTemplate", "../a"))));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testLoad_見つかったファイルのフォルダと新規保存先を返す() {
        final JsonObject location = JsonTestHelper.loadResponseOf(this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/template/load", "{\"name\":\"sample.json\"}"))).getJsonObject("location");
        final String templateDir = Paths.get("target/test-temp/workspace/sample/resources/template")
                .toAbsolutePath().normalize().toString();
        Assertions.assertEquals("sample.json", location.getString("fileName"));
        Assertions.assertEquals(templateDir, location.getString("folder"));
        Assertions.assertEquals(templateDir, location.getString("defaultFolder"));
        Assertions.assertTrue(location.getBoolean("defaultFolderHasFile"));
    }

    @Test
    void testLoad_見つからなければ空の内容と場所nullを返す() throws IOException {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/template/load", "{\"name\":\"missing.stg\"}"));
        JsonTestHelper.assertJsonEquals("{\"content\":null,\"location\":null}", response);
    }

    @Test
    void testSave_overwriteは見つかったファイルへ上書きする() throws IOException {
        this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/template/save",
                "{\"name\":\"savedTemplate.stg\",\"input\":{\"content\":\"first\"}}"));
        this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/template/save",
                "{\"name\":\"savedTemplate.stg\",\"overwrite\":true,\"input\":{\"content\":\"second\"}}"));
        final Path saved = Paths.get("target/test-temp/workspace/sample/resources/template/savedTemplate.stg");
        Assertions.assertEquals("second", Files.readString(saved));
    }

    @Test
    void testSave_overwriteで見つからなければエラー() {
        Assertions.assertThrows(HttpClientResponseException.class,
                () -> this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/template/save",
                        "{\"name\":\"missing.stg\",\"overwrite\":true,\"input\":{\"content\":\"x\"}}")));
        Assertions.assertFalse(Files.exists(Paths.get(
                "target/test-temp/workspace/sample/resources/template/missing.stg")));
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
                HttpRequest.POST("dbunit-cli/template/load", "{\"name\":\"savedTemplate.stg\"}"));
        Assertions.assertTrue(loaded.contains("\"content\""), loaded);
        Assertions.assertTrue(loaded.contains("ddlType(row) ::= <<$row.TYPE_NAME$>>"), loaded);
    }
}
