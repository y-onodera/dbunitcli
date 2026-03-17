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
class GenerateControllerTest {
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
        this.tryDeleteCommand("parameterize", "csvToTxt");
        Files.deleteIfExists(Paths.get(WORKSPACE, "option/parameterize/template/csvToTxt.txt"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "csvToTxt.csv"));

        // generate コマンドのファイルをクリーンアップ
        this.tryDelete("savedGenerate");
        this.tryDelete("new item");
        this.tryDelete("csvToTxt(1)");
        this.tryDelete("renamed");
    }

    @Test
    public void testAdd_パラメータを追加する() {
        final String response = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/generate/add"));
        Assertions.assertEquals("[\"csvToTxt\",\"new item\"]", response);
    }

    @Test
    public void testCopy_パラメータをコピーする() {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/generate/copy", "{\"name\":\"csvToTxt\"}"));
        Assertions.assertEquals("[\"csvToTxt(1)\",\"csvToTxt\"]", response);
    }

    @Test
    public void testDelete_パラメータを削除する() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/generate/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/generate/delete", "{\"name\":\"new item\"}"));
        Assertions.assertEquals("[\"csvToTxt\"]", response);
    }

    @Test
    public void testRename_パラメータをリネームする() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/generate/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/generate/rename", "{\"oldName\":\"new item\",\"newName\":\"renamed\"}"));
        Assertions.assertEquals("[\"csvToTxt\",\"renamed\"]", response);
    }

    @Test
    public void testParameterize_generateパラメータからparameterizeコマンドを生成する() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/generate/parameterize", "{\"name\":\"csvToTxt\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/generate-parameterize-response.json"), jsonResponse);
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "csvToTxt.csv")),
                "param.src に指定する csvToTxt.csv がワークスペース直下に作成されること");
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "option/parameterize/template/csvToTxt.txt")),
                "テンプレートファイルが option/parameterize/template/csvToTxt.txt に作成されること");
        Assertions.assertTrue(
                Files.exists(Paths.get(WORKSPACE, "option/parameterize/csvToTxt.txt")),
                "parameterize パラメータファイルが option/parameterize/csvToTxt.txt に作成されること");
    }

    @Test
    public void testSave_パラメータを保存して再ロードで確認する() throws IOException {
        final String saveResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/generate/save",
                        "{\"name\":\"savedGenerate\",\"input\":{\"-src.src\":\"resources/src/csv\",\"-src.srcType\":\"csv\",\"-template\":\"sample.stg\",\"-result\":\"target/generate/result\",\"-generateType\":\"txt\"}}"));
        Assertions.assertEquals("success", saveResponse);

        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/generate/load", "{\"name\":\"savedGenerate\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/generate-load-response.json"), jsonResponse);
    }

    @Test
    public void testLoad() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/generate/load"
                , "{\"name\":\"csvToTxt\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/generate-load-response.json"), jsonResponse);
    }

    @Test
    public void testReset() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/generate/reset"));

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/generate-reset-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeXlsx_xlsxSchema要素が追加されdelimiterIgnoreQuotedが削除される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/generate/refresh", "{\"-src.srcType\":\"xlsx\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/generate-refresh-srcType-xlsx-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeReg_regDataSplitとregHeaderSplitが追加される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/generate/refresh", "{\"-src.srcType\":\"reg\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/generate-refresh-srcType-reg-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeTable_JDBC要素が追加されファイルトラバース要素が削除される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/generate/refresh", "{\"-src.srcType\":\"table\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/generate-refresh-srcType-table-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeFile_最小限の要素のみになる() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/generate/refresh", "{\"-src.srcType\":\"file\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/generate-refresh-srcType-file-response.json"), jsonResponse);
    }

    private void tryDelete(final String name) {
        this.tryDeleteCommand("generate", name);
    }

    private void tryDeleteCommand(final String command, final String name) {
        try {
            this.client.toBlocking().retrieve(
                    HttpRequest.POST("dbunit-cli/" + command + "/delete", "{\"name\":\"" + name + "\"}"));
        } catch (final Exception ignored) {
        }
    }
}
