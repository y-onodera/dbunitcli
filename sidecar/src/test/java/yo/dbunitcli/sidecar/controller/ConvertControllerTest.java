package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class ConvertControllerTest {
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Workspace workspace;

    private static final String WORKSPACE = "target/test-temp/workspace/sample";

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(WORKSPACE, "option/parameterize/csvToXlsx.txt"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "option/parameterize/template/csvToXlsx.txt"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "csvToXlsx.csv"));
    }

    @Test
    public void testParameterize_convertパラメータからparameterizeコマンドを生成する() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/parameterize", "{\"name\":\"csvToXlsx\"}"));
        System.out.println(jsonResponse);

        final String expectedJson = Files.readString(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-parameterize-response.json"));
        Assertions.assertEquals(this.normalizeJson(expectedJson), this.normalizeJson(jsonResponse));
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "csvToXlsx.csv")),
                "param.src に指定する csvToXlsx.csv がワークスペース直下に作成されること");
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "option/parameterize/template/csvToXlsx.txt")),
                "テンプレートファイルが option/parameterize/template/csvToXlsx.txt に作成されること");
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "option/parameterize/csvToXlsx.txt")),
                "parameterize パラメータファイルが option/parameterize/csvToXlsx.txt に作成されること");
    }

    @Test
    public void testLoad() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/convert/load"
                , "{\"name\":\"csvToXlsx\"}"));
        System.out.println(jsonResponse);

        final String expectedJson = Files.readString(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-load-response.json"));
        // 改行やスペースを無視して比較するために正規化
        final String normalizedExpected = this.normalizeJson(expectedJson);
        final String normalizedActual = this.normalizeJson(jsonResponse);
        Assertions.assertEquals(normalizedExpected, normalizedActual);
    }

    @Test
    public void testReset() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/convert/reset"));
        System.out.println(jsonResponse);

        final String expectedJson = Files.readString(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-reset-response.json"));
        // 改行やスペースを無視して比較するために正規化
        final String normalizedExpected = this.normalizeJson(expectedJson);
        final String normalizedActual = this.normalizeJson(jsonResponse);
        Assertions.assertEquals(normalizedExpected, normalizedActual);
    }

    /**
     * JSONの空白や改行を取り除いて正規化するヘルパーメソッド
     */
    private String normalizeJson(final String json) {
        // 空白、タブ、改行を削除
        return Pattern.compile("\\s+").matcher(json).replaceAll("");
    }
}
