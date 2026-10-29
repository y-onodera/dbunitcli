package yo.dbunitcli.sidecar.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.producer.ComparableCsvDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsxDataSetProducer;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewResponseDto;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class DatasetSettingsControllerTest {

    private static final Path SETTING_DIR =
            Paths.get("target/test-temp/workspace/sample/resources/setting");

    private static final Path PREVIEW_SRC_DIR =
            Paths.get("target/test-temp/workspace/sample/resources/src/preview");

    private static final Path CONVERT_RESULT_DIR =
            Paths.get("target/test-temp/workspace/sample/result/tableConvert");

    private static final int BIG_ROWS = DatasetSettingsController.PREVIEW_ROW_LIMIT + 50;


    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @AfterEach
    void tearDown() {
        this.tryDelete("savedSetting.json");
        this.tryDelete("deleteSetting.json");
        this.tryDelete("sqlFunctionSetting.json");
        this.tryDelete("outputFilterSetting.json");
        this.tryDelete("importSetting.json");
        this.tryDelete("previewRenameSetting.json");
        this.tryDelete("previewJoinSetting.json");
        this.tryDelete("previewKeysSetting.json");
        this.deletePreviewSrc();
    }

    private void deletePreviewSrc() {
        if (Files.exists(PREVIEW_SRC_DIR)) {
            GenerateTemplateSupport.deleteRecursively(PREVIEW_SRC_DIR);
        }
        if (Files.exists(CONVERT_RESULT_DIR)) {
            GenerateTemplateSupport.deleteRecursively(CONVERT_RESULT_DIR);
        }
    }

    private void tryDelete(final String name) {
        try {
            this.client.toBlocking().retrieve(
                    HttpRequest.POST("dbunit-cli/dataset-setting/delete", name)
                            .contentType(MediaType.TEXT_PLAIN_TYPE));
        } catch (final Exception ignored) {
        }
    }

    @Test
    void testList() throws IOException {
        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.GET("dbunit-cli/dataset-setting/list"));
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/setting-list-response.json"),
                response);
    }

    @Test
    void testLoad() throws IOException {
        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/load", "test.json")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        System.out.println(response);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/setting-load-response.json"),
                response);
    }

    @Test
    void testSave_保存して再ロードで確認する() throws IOException {
        final String saveResponse = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/save",
                        "{\"name\":\"savedSetting.json\",\"input\":{\"settings\":[{\"name\":{\"any\":[\"TABLE_B\"]},\"keys\":[\"id\"]}],\"commonSettings\":[]}}"));
        System.out.println(saveResponse);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/setting-save-list-response.json"),
                saveResponse);

        Assertions.assertTrue(
                Files.exists(SETTING_DIR.resolve("savedSetting.json")),
                "savedSetting.json がファイルシステムに作成されること");

        final String loadResponse = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/load", "savedSetting.json")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        System.out.println(loadResponse);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/setting-save-load-response.json"),
                loadResponse);
    }

    @Test
    void testSave_sqlFunctionキーで保存され旧functionキーも受け付ける() throws IOException {
        for (final String key : new String[]{"sqlFunction", "function"}) {
            this.client.toBlocking()
                    .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/save",
                            "{\"name\":\"sqlFunctionSetting.json\",\"input\":{\"settings\":[{\"name\":{\"any\":[\"TABLE_B\"]},\"" + key + "\":{\"mark\":\"1\"}}],\"commonSettings\":[]}}"));

            final String saved = Files.readString(SETTING_DIR.resolve("sqlFunctionSetting.json"));
            Assertions.assertTrue(saved.contains("\"sqlFunction\""), key + " で渡した値が sqlFunction キーで保存されること: " + saved);
            Assertions.assertFalse(saved.contains("\"function\""), "function キーでは保存されないこと: " + saved);
        }
    }

    @Test
    void testSave_outputFilterが保存され再ロードで復元される() throws IOException {
        for (final String filter : new String[]{
                "{\"name\":{\"any\":[\"TABLE_B\"]}}",
                "{\"pattern\":{\"string\":\"TABLE\",\"exclude\":[\"TABLE_X\"]}}",
                "{\"pattern\":{\"regex\":\"^T_.*\",\"exclude\":[\"T_X\"]}}"}) {
            this.client.toBlocking()
                    .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/save",
                            "{\"name\":\"outputFilterSetting.json\",\"input\":{\"settings\":[],\"commonSettings\":[],\"outputFilter\":" + filter + "}}"));

            final String loaded = this.client.toBlocking()
                    .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/load", "outputFilterSetting.json")
                            .contentType(MediaType.TEXT_PLAIN_TYPE));
            JsonTestHelper.assertJsonEquals("{\"outputFilter\":" + filter + "}", loaded);
        }
    }

    @Test
    void testSave_importが保存され再ロードで復元される() throws IOException {
        final String imports = "[{\"path\":\"a.json\"},{\"path\":\"b.json\",\"outputFilterCombine\":\"or\"}]";
        this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/save",
                        "{\"name\":\"importSetting.json\",\"input\":{\"settings\":[],\"commonSettings\":[],\"import\":" + imports + "}}"));

        final String loaded = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/load", "importSetting.json")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        JsonTestHelper.assertJsonEquals("{\"import\":" + imports + "}", loaded);
    }

    @Test
    void testGenerate_データセットからdataset_settingテンプレートを生成する() throws IOException {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/dataset-setting/generate",
                        "{\"-src.src\":\"resources/src/csv\",\"-src.srcType\":\"csv\"}"));
        System.out.println(response);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/setting-generate-response.json"),
                response);
    }

    @Test
    void testGenerate_src未指定は400を返す() {
        final HttpClientResponseException ex = Assertions.assertThrows(
                HttpClientResponseException.class,
                () -> this.client.toBlocking().retrieve(
                        HttpRequest.POST("dbunit-cli/dataset-setting/generate", "{}")));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testDelete_ファイルを削除する() throws IOException {
        this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/save",
                        "{\"name\":\"deleteSetting.json\",\"input\":{\"settings\":[],\"commonSettings\":[]}}"));

        final String response = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/delete", "deleteSetting.json")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        System.out.println(response);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/setting-list-response.json"),
                response);
        Assertions.assertFalse(
                Files.exists(SETTING_DIR.resolve("deleteSetting.json")),
                "deleteSetting.json がファイルシステムから削除されること");
    }

    @Test
    void testTablePreview_設定なしは先頭の行と全行数を返す() throws IOException {
        this.createPreviewSrc();

        final DatasetTablePreviewResponseDto actual = this.tablePreview("", "big");

        Assertions.assertArrayEquals(new String[]{"key", "value"}, actual.getHeaders());
        Assertions.assertEquals(DatasetSettingsController.PREVIEW_ROW_LIMIT, actual.getRows().size());
        Assertions.assertEquals(BIG_ROWS, actual.getTotalRows());
        Assertions.assertEquals("1", String.valueOf(actual.getRows().getFirst()[0]));
    }

    @Test
    void testTablePreview_設定でテーブル名を変えても変更後の名前で読み込む() throws IOException {
        this.createPreviewSrc();
        final DatasetTablePreviewResponseDto actual = this.tablePreview(
                this.writeRenameSetting(), "renamed");

        Assertions.assertArrayEquals(new String[]{"key", "value"}, actual.getHeaders());
        Assertions.assertEquals(BIG_ROWS, actual.getTotalRows());
    }

    @Test
    void testTablePreview_joinした結果も読み込む() throws IOException {
        this.createPreviewSrc();
        final DatasetTablePreviewResponseDto actual = this.tablePreview(
                this.writeJoinSetting(), "joined");

        Assertions.assertEquals(3, actual.getTotalRows());
        Assertions.assertEquals(3, actual.getRows().size());
    }

    @Test
    void testTablePreview_存在しないテーブルは空を返す() throws IOException {
        this.createPreviewSrc();

        final DatasetTablePreviewResponseDto actual = this.tablePreview("", "nothing");

        // 空の配列はJSONで省略される
        Assertions.assertNull(actual.getHeaders());
        Assertions.assertEquals(0, actual.getTotalRows());
    }

    @Test
    void testTableConvert_選んだテーブルだけを出力して出力先を返す() throws IOException {
        this.createPreviewSrc();

        final String actual = this.tableConvert("", "small");

        final Path resultDir = Paths.get(actual);
        Assertions.assertEquals(CONVERT_RESULT_DIR.toAbsolutePath().normalize(), resultDir.toAbsolutePath().normalize());
        Assertions.assertEquals(List.of("\"key\",\"other\"", "\"1\",\"a\"", "\"2\",\"b\"", "\"3\",\"c\""),
                Files.readAllLines(resultDir.resolve("small.csv")));
        Assertions.assertFalse(Files.exists(resultDir.resolve("big.csv")));
    }

    @Test
    void testTableConvert_設定なしは対象外のソースを開かない() throws IOException {
        this.createPreviewSrc();

        final List<String> opened = this.produceStartLogs(ComparableCsvDataSetProducer.class,
                () -> this.tableConvert("", "small"));

        Assertions.assertEquals(1, opened.size(), opened.toString());
        Assertions.assertTrue(opened.getFirst().endsWith("small.csv"), opened.toString());
    }

    @Test
    void testTableConvert_テーブル名を変えない設定なら対象外のソースを開かない() throws IOException {
        this.createPreviewSrc();
        final String setting = this.writeKeysSetting();

        final List<String> opened = this.produceStartLogs(ComparableCsvDataSetProducer.class,
                () -> this.tableConvert(setting, "small"));

        Assertions.assertEquals(1, opened.size(), opened.toString());
        Assertions.assertTrue(opened.getFirst().endsWith("small.csv"), opened.toString());
    }

    @Test
    void testTablePreview_テーブル名を変えない設定なら対象外のソースを開かない() throws IOException {
        this.createPreviewSrc();
        final String setting = this.writeKeysSetting();

        final List<String> opened = this.produceStartLogs(ComparableCsvDataSetProducer.class,
                () -> this.tablePreview(setting, "small"));

        Assertions.assertEquals(1, opened.size(), opened.toString());
        Assertions.assertTrue(opened.getFirst().endsWith("small.csv"), opened.toString());
    }

    @Test
    void testTableConvert_テーブル名を変える設定でも対象外のソースを開かない() throws IOException {
        this.createPreviewSrc();
        final String setting = this.writeRenameSetting();

        final List<String> opened = this.produceStartLogs(ComparableCsvDataSetProducer.class,
                () -> this.tableConvert(setting, "renamed"));

        Assertions.assertEquals(1, opened.size(), opened.toString());
        Assertions.assertTrue(opened.getFirst().endsWith("big.csv"), opened.toString());
    }

    @Test
    void testTablePreview_テーブル名を変える設定でも対象外のソースを開かない() throws IOException {
        this.createPreviewSrc();
        final String setting = this.writeRenameSetting();

        final List<String> opened = this.produceStartLogs(ComparableCsvDataSetProducer.class,
                () -> this.tablePreview(setting, "renamed"));

        Assertions.assertEquals(1, opened.size(), opened.toString());
        Assertions.assertTrue(opened.getFirst().endsWith("big.csv"), opened.toString());
    }

    @Test
    void testTableConvert_xlsxSchemaなしのxlsxは対象外のシートを読まない() throws IOException {
        Files.createDirectories(PREVIEW_SRC_DIR);
        try (final XSSFWorkbook book = new XSSFWorkbook();
             final OutputStream out = Files.newOutputStream(PREVIEW_SRC_DIR.resolve("book.xlsx"))) {
            for (final String sheetName : List.of("big", "small")) {
                final Sheet sheet = book.createSheet(sheetName);
                sheet.createRow(0).createCell(0).setCellValue("key");
                sheet.createRow(1).createCell(0).setCellValue(sheetName);
            }
            book.write(out);
        }

        final List<String> opened = this.produceStartLogs(ComparableXlsxDataSetProducer.class,
                () -> this.tableConvert("xlsx", "", "small"));

        Assertions.assertEquals(List.of("small"), opened.stream()
                                                       .filter(it -> it.contains("sheetName="))
                                                       .map(it -> it.replaceAll(".*sheetName=([^,]*),.*", "$1"))
                                                       .toList(), opened.toString());
        Assertions.assertEquals(2, Files.readAllLines(CONVERT_RESULT_DIR.resolve("small.csv")).size());
    }

    @Test
    void testTableConvert_設定でテーブル名を変えても変更後の名前で出力する() throws IOException {
        this.createPreviewSrc();
        final Path resultDir = Paths.get(this.tableConvert(
                this.writeRenameSetting(), "renamed"));

        Assertions.assertEquals(BIG_ROWS + 1, Files.readAllLines(resultDir.resolve("renamed.csv")).size());
        Assertions.assertFalse(Files.exists(resultDir.resolve("small.csv")));
    }

    @Test
    void testTableConvert_joinした結果を出力する() throws IOException {
        this.createPreviewSrc();
        final Path resultDir = Paths.get(this.tableConvert(
                this.writeJoinSetting(), "joined"));

        Assertions.assertEquals(4, Files.readAllLines(resultDir.resolve("joined.csv")).size());
        Assertions.assertFalse(Files.exists(resultDir.resolve("big.csv")));
        Assertions.assertFalse(Files.exists(resultDir.resolve("small.csv")));
    }

    @Test
    void testTableConvert_resultの検証エラーは400を返す() throws IOException {
        this.createPreviewSrc();

        final HttpClientResponseException ex = Assertions.assertThrows(HttpClientResponseException.class,
                () -> this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/table-convert",
                        this.tableRequestBody("", "small",
                                Map.of("-result.resultType", "csv", "-result.outputEncoding", "NO-SUCH-ENCODING")))));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        Assertions.assertTrue(ex.getMessage().contains("-result.outputEncoding"), ex.getMessage());
    }

    private String tableConvert(final String setting, final String tableName) throws IOException {
        return this.tableConvert("csv", setting, tableName);
    }

    private String tableConvert(final String srcType, final String setting, final String tableName) throws IOException {
        return this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/table-convert",
                this.tableRequestBody(srcType, setting, tableName, Map.of("-result.resultType", "csv",
                        "-result.result", CONVERT_RESULT_DIR.toAbsolutePath().toString(),
                        "-result.outputEncoding", "UTF-8"))));
    }

    private String tableRequestBody(final String setting, final String tableName, final Map<String, String> result) throws IOException {
        return this.tableRequestBody("csv", setting, tableName, result);
    }

    private String tableRequestBody(final String srcType, final String setting, final String tableName,
                                    final Map<String, String> result) throws IOException {
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("setting", setting);
        body.put("srcType", srcType);
        body.put("src", PREVIEW_SRC_DIR.toAbsolutePath().toString());
        body.put("encoding", "UTF-8");
        body.put("tableName", tableName);
        if (result != null) {
            body.put("result", result);
        }
        return ObjectMapper.getDefault().writeValueAsString(body);
    }

    /**
     * actionの実行中にproducerが出した読込開始のログ（読み込んだソース・シート）
     */
    private List<String> produceStartLogs(final Class<?> producer, final IOAction action) throws IOException {
        final Logger logger = (Logger) LoggerFactory.getLogger(producer);
        final ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            action.run();
        } finally {
            logger.detachAppender(appender);
        }
        return appender.list.stream()
                            .map(ILoggingEvent::getFormattedMessage)
                            .filter(it -> it.startsWith("produce - start"))
                            .toList();
    }

    private interface IOAction {
        void run() throws IOException;
    }

    private String writeRenameSetting() throws IOException {
        return this.writeSetting("previewRenameSetting.json",
                "{\"settings\":[{\"name\":[\"big\"],\"tableName\":\"renamed\"}]}");
    }

    private String writeKeysSetting() throws IOException {
        return this.writeSetting("previewKeysSetting.json",
                "{\"settings\":[{\"name\":[\"small\"],\"keys\":[\"key\"]}]}");
    }

    private String writeJoinSetting() throws IOException {
        return this.writeSetting("previewJoinSetting.json",
                "{\"settings\":[{\"innerJoin\":{\"left\":\"big\",\"right\":\"small\",\"column\":[\"key\"]},\"tableName\":\"joined\"}]}");
    }

    private String writeSetting(final String fileName, final String content) throws IOException {
        final Path file = SETTING_DIR.resolve(fileName);
        Files.writeString(file, content);
        return file.toAbsolutePath().toString();
    }

    private void createPreviewSrc() throws IOException {
        Files.createDirectories(PREVIEW_SRC_DIR);
        final StringBuilder big = new StringBuilder("key,value\n");
        for (int i = 1; i <= BIG_ROWS; i++) {
            big.append(i).append(",v").append(i).append("\n");
        }
        Files.writeString(PREVIEW_SRC_DIR.resolve("big.csv"), big.toString());
        Files.writeString(PREVIEW_SRC_DIR.resolve("small.csv"), "key,other\n1,a\n2,b\n3,c\n");
    }

    private DatasetTablePreviewResponseDto tablePreview(final String setting, final String tableName) throws IOException {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/dataset-setting/table-preview", this.tableRequestBody(setting, tableName, null)));
        return ObjectMapper.getDefault().readValue(response, DatasetTablePreviewResponseDto.class);
    }
}
