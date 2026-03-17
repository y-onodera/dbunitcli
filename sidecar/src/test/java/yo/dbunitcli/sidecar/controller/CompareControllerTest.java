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

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class CompareControllerTest {
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
        // parameterize コマンドの in-memory state も含めてクリーンアップ
        this.tryDeleteCommand("parameterize", "csvCsv");
        Files.deleteIfExists(Paths.get(WORKSPACE, "option/parameterize/template/csvCsv.txt"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "csvCsv.csv"));

        // compare コマンドのファイルをクリーンアップ
        this.tryDelete("savedCompare");
        this.tryDelete("new item");
        this.tryDelete("csvCsv(1)");
        this.tryDelete("renamed");
    }

    @Test
    public void testAdd_パラメータを追加する() {
        final String response = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/compare/add"));
        Assertions.assertEquals("[\"csvCsv\",\"new item\"]", response);
    }

    @Test
    public void testCopy_パラメータをコピーする() {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/compare/copy", "{\"name\":\"csvCsv\"}"));
        Assertions.assertEquals("[\"csvCsv(1)\",\"csvCsv\"]", response);
    }

    @Test
    public void testDelete_パラメータを削除する() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/compare/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/compare/delete", "{\"name\":\"new item\"}"));
        Assertions.assertEquals("[\"csvCsv\"]", response);
    }

    @Test
    public void testRename_パラメータをリネームする() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/compare/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/compare/rename", "{\"oldName\":\"new item\",\"newName\":\"renamed\"}"));
        Assertions.assertEquals("[\"csvCsv\",\"renamed\"]", response);
    }

    @Test
    public void testParameterize_compareパラメータからparameterizeコマンドを生成する() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/compare/parameterize", "{\"name\":\"csvCsv\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/compare-parameterize-response.json"), jsonResponse);
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "csvCsv.csv")),
                "param.src に指定する csvCsv.csv がワークスペース直下に作成されること");
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "option/parameterize/template/csvCsv.txt")),
                "テンプレートファイルが option/parameterize/template/csvCsv.txt に作成されること");
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "option/parameterize/csvCsv.txt")),
                "parameterize パラメータファイルが option/parameterize/csvCsv.txt に作成されること");
    }

    @Test
    public void testSave_パラメータを保存して再ロードで確認する() throws IOException {
        final String saveResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/compare/save",
                        "{\"name\":\"savedCompare\",\"input\":{\"-new.src\":\"resources/src/csv/multi1.csv\",\"-new.srcType\":\"csv\",\"-old.src\":\"resources/src/csv/multi2.csv\",\"-old.srcType\":\"csv\"}}"));
        Assertions.assertEquals("success", saveResponse);

        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/compare/load", "{\"name\":\"savedCompare\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/compare-save-load-response.json"), jsonResponse);
    }

    @Test
    public void testLoad() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/compare/load"
                , "{\"name\":\"csvCsv\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/compare-load-response.json"), jsonResponse);
    }

    @Test
    public void testReset() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/compare/reset"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/compare-reset-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_newSrcTypeXlsx_newDataにxlsxSchema要素が追加される() throws IOException {
        assertRefresh("{\"-new.srcType\":\"xlsx\"}", "compare-refresh-newSrcType-xlsx-response.json");
    }

    @Test
    public void testRefresh_newSrcTypeTable_newDataにJDBC要素が追加される() throws IOException {
        assertRefresh("{\"-new.srcType\":\"table\"}", "compare-refresh-newSrcType-table-response.json");
    }

    @Test
    public void testRefresh_oldSrcTypeXlsx_oldDataにxlsxSchema要素が追加される() throws IOException {
        assertRefresh("{\"-old.srcType\":\"xlsx\"}", "compare-refresh-oldSrcType-xlsx-response.json");
    }

    @Test
    public void testRefresh_expectSrcTypeCsv_expectDataにCSV要素が追加される() throws IOException {
        assertRefresh("{\"-expect.srcType\":\"csv\"}", "compare-refresh-expectSrcType-csv-response.json");
    }

    @Test
    public void testRefresh_expectSrcTypeXlsx_expectDataにxlsxSchema要素が追加される() throws IOException {
        assertRefresh("{\"-expect.srcType\":\"xlsx\"}", "compare-refresh-expectSrcType-xlsx-response.json");
    }

    private void assertRefresh(final String requestJson, final String expectedFile) throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/compare/refresh", requestJson));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/" + expectedFile), jsonResponse);
    }

    private void tryDelete(final String name) {
        this.tryDeleteCommand("compare", name);
    }

    private void tryDeleteCommand(final String command, final String name) {
        try {
            this.client.toBlocking().retrieve(
                    HttpRequest.POST("dbunit-cli/" + command + "/delete", "{\"name\":\"" + name + "\"}"));
        } catch (final Exception ignored) {
        }
    }
}
