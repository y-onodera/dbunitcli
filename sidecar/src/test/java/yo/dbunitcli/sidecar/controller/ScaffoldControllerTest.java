package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.resource.FileResources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class ScaffoldControllerTest {
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    private static final String WORKSPACE = "target/test-temp/workspace/sample";

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(WORKSPACE, "resources/template/scaffoldTpl.stg"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "resources/template/scaffoldTpl.txt"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "resources/setting/scaffoldUnit.json"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "src/multi1.csv"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "src/multi2.csv"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "src"));
    }

    @Test
    public void testRefresh_targetDdl_dataset入力フォームが構成される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/scaffold/refresh",
                        "{\"-target\":\"ddl\",\"-dataset.src\":\"resources/src/csv\",\"-dataset.srcType\":\"csv\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/scaffold-refresh-target-ddl-response.json"),
                jsonResponse);
    }

    @Test
    public void testRefresh_targetFixedColumnDef_固有オプションが追加される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/scaffold/refresh", "{\"-target\":\"fixedColumnDef\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/scaffold-refresh-target-fixedColumnDef-response.json"),
                jsonResponse);
    }

    @Test
    public void testExec_雛型を生成しtxt駆動generateパラメータを返す() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/scaffold/exec",
                        "{\"-target\":\"ddl\",\"-template\":\"scaffoldTpl\",\"-unitSetting\":\"scaffoldUnit\""
                                + ",\"-dataset.src\":\"resources/src/csv\",\"-dataset.srcType\":\"csv\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/scaffold-exec-response.json"),
                jsonResponse);
        Assertions.assertTrue(Files.exists(Paths.get(WORKSPACE, "resources/template/scaffoldTpl.stg")),
                "テンプレート.stgがworkspaceのresources/templateに作成されること");
        Assertions.assertTrue(Files.exists(Paths.get(WORKSPACE, "resources/template/scaffoldTpl.txt")),
                "テンプレート.txtがworkspaceのresources/templateに作成されること");
        Assertions.assertTrue(Files.exists(Paths.get(WORKSPACE, "resources/setting/scaffoldUnit.json")),
                "unitSettingがworkspaceのresources/settingに作成されること");
        Assertions.assertTrue(Files.exists(Paths.get(WORKSPACE, "src/multi1.csv")),
                "記述子datasetがworkspaceのsrcに作成されること");
        Assertions.assertTrue(Files.exists(Paths.get(WORKSPACE, "src/multi2.csv")),
                "記述子datasetがworkspaceのsrcに作成されること");
    }

    @Test
    public void testExec_template未指定は400() {
        final HttpClientResponseException error = Assertions.assertThrows(HttpClientResponseException.class,
                () -> this.client.toBlocking().retrieve(
                        HttpRequest.POST("dbunit-cli/scaffold/exec", "{\"-target\":\"ddl\"}")));
        Assertions.assertEquals(400, error.getStatus().getCode());
    }

    @Test
    public void testExec_targetParameterは400() {
        final HttpClientResponseException error = Assertions.assertThrows(HttpClientResponseException.class,
                () -> this.client.toBlocking().retrieve(
                        HttpRequest.POST("dbunit-cli/scaffold/exec",
                                "{\"-target\":\"parameter\",\"-template\":\"scaffoldTpl\"}")));
        Assertions.assertEquals(400, error.getStatus().getCode());
    }
}
