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
        // parameterize コマンドの in-memory state も含めてクリーンアップ
        this.tryDeleteCommand("parameterize", "csvToXlsx");
        Files.deleteIfExists(Paths.get(WORKSPACE, "option/parameterize/template/csvToXlsx.txt"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "csvToXlsx.csv"));

        // convert コマンドのファイルをクリーンアップ
        this.tryDelete("savedConvert");
        this.tryDelete("new item");
        this.tryDelete("csvToXlsx(1)");
        this.tryDelete("renamed");
    }

    @Test
    public void testAdd_パラメータを追加する() {
        final String response = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/convert/add"));
        Assertions.assertEquals("[\"csvToXlsx\",\"new item\"]", response);
    }

    @Test
    public void testCopy_パラメータをコピーする() {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/copy", "{\"name\":\"csvToXlsx\"}"));
        Assertions.assertEquals("[\"csvToXlsx(1)\",\"csvToXlsx\"]", response);
    }

    @Test
    public void testDelete_パラメータを削除する() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/convert/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/delete", "{\"name\":\"new item\"}"));
        Assertions.assertEquals("[\"csvToXlsx\"]", response);
    }

    @Test
    public void testRename_パラメータをリネームする() {
        this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/convert/add"));
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/rename", "{\"oldName\":\"new item\",\"newName\":\"renamed\"}"));
        Assertions.assertEquals("[\"csvToXlsx\",\"renamed\"]", response);
    }

    @Test
    public void testParameterize_convertパラメータからparameterizeコマンドを生成する() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/parameterize", "{\"name\":\"csvToXlsx\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-parameterize-response.json"), jsonResponse);
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
    public void testSave_パラメータを保存して再ロードで確認する() throws IOException {
        final String saveResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/save",
                        "{\"name\":\"savedConvert\",\"input\":{\"-src\":\"resources/src/csv\",\"-srcType\":\"csv\",\"-result\":\"target/convert/result\",\"-resultType\":\"xlsx\"}}"));
        Assertions.assertEquals("success", saveResponse);

        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/load", "{\"name\":\"savedConvert\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-save-load-response.json"), jsonResponse);
    }

    @Test
    public void testLoad() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/convert/load"
                , "{\"name\":\"csvToXlsx\"}"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-load-response.json"), jsonResponse);
    }

    @Test
    public void testReset() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/convert/reset"));
        System.out.println(jsonResponse);

        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-reset-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeXlsx_xlsxSchema要素が追加されdelimiterIgnoreQuotedが削除される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/refresh", "{\"-srcType\":\"xlsx\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-refresh-srcType-xlsx-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeXls_xlsxSchema要素が追加されdelimiterIgnoreQuotedが削除される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/refresh", "{\"-srcType\":\"xls\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-refresh-srcType-xls-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeReg_regDataSplitとregHeaderSplitが追加される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/refresh", "{\"-srcType\":\"reg\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-refresh-srcType-reg-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeFixed_fixedLength要素が追加される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/refresh", "{\"-srcType\":\"fixed\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-refresh-srcType-fixed-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeTable_JDBC要素が追加されファイルトラバース要素が削除される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/refresh", "{\"-srcType\":\"table\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-refresh-srcType-table-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeSql_JDBC要素が追加されファイルトラバース要素が削除される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/refresh", "{\"-srcType\":\"sql\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-refresh-srcType-sql-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeFile_最小限の要素のみになる() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/refresh", "{\"-srcType\":\"file\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-refresh-srcType-file-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_srcTypeDir_トラバース要素のみになる() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/refresh", "{\"-srcType\":\"dir\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-refresh-srcType-dir-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_resultTypeXlsx_excelTable要素が追加されoutputEncodingが削除される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/refresh", "{\"-resultType\":\"xlsx\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-refresh-resultType-xlsx-response.json"), jsonResponse);
    }

    @Test
    public void testRefresh_resultTypeXls_excelTable要素が追加されoutputEncodingが削除される() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/convert/refresh", "{\"-resultType\":\"xls\"}"));
        System.out.println(jsonResponse);
        JsonTestHelper.assertJsonEquals(Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/convert-refresh-resultType-xls-response.json"), jsonResponse);
    }

    private void tryDelete(final String name) {
        this.tryDeleteCommand("convert", name);
    }

    private void tryDeleteCommand(final String command, final String name) {
        try {
            this.client.toBlocking().retrieve(
                    HttpRequest.POST("dbunit-cli/" + command + "/delete", "{\"name\":\"" + name + "\"}"));
        } catch (final Exception ignored) {
        }
    }
}
