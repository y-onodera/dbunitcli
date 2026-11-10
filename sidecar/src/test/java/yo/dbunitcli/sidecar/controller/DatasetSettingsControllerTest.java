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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.producer.ComparableCsvDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsxDataSetProducer;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.DatasetTableNamesRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewResponseDto;
import yo.dbunitcli.sidecar.dto.DatasetUnitTablePreviewResponseDto;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class DatasetSettingsControllerTest {

    private static final Path SETTING_DIR =
            Paths.get("target/test-temp/workspace/sample/resources/setting");

    private static final Path UNIT_SETTING_DIR =
            Paths.get("target/test-temp/workspace/sample").resolve(FileResources.OPTION_GENERATE_SETTING_PATH);

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
    void tearDown() throws IOException {
        this.tryDelete("savedSetting.json");
        this.tryDelete("deleteSetting.json");
        this.tryDelete("sqlFunctionSetting.json");
        this.tryDelete("outputFilterSetting.json");
        this.tryDelete("importSetting.json");
        this.tryDelete("previewRenameSetting.json");
        this.tryDelete("previewJoinSetting.json");
        this.tryDelete("previewUnmatchedJoinSetting.json");
        this.tryDelete("previewKeysSetting.json");
        Files.deleteIfExists(UNIT_SETTING_DIR.resolve("previewUnitSetting.json"));
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
                .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/load", "{\"name\":\"test.json\"}"));
        System.out.println(response);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/setting-load-response.json"),
                JsonTestHelper.contentOf(response));
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
                .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/load", "{\"name\":\"savedSetting.json\"}"));
        System.out.println(loadResponse);
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/setting-save-load-response.json"),
                JsonTestHelper.contentOf(loadResponse));
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
                    .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/load", "{\"name\":\"outputFilterSetting.json\"}"));
            JsonTestHelper.assertJsonEquals("{\"outputFilter\":" + filter + "}", JsonTestHelper.contentOf(loaded));
        }
    }

    @Test
    void testSave_importが保存され再ロードで復元される() throws IOException {
        final String imports = "[{\"path\":\"a.json\"},{\"path\":\"b.json\",\"outputFilterCombine\":\"or\"}]";
        this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/save",
                        "{\"name\":\"importSetting.json\",\"input\":{\"settings\":[],\"commonSettings\":[],\"import\":" + imports + "}}"));

        final String loaded = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/dataset-setting/load", "{\"name\":\"importSetting.json\"}"));
        JsonTestHelper.assertJsonEquals("{\"import\":" + imports + "}", JsonTestHelper.contentOf(loaded));
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
    void testTablePreview_joinがあってもjoinに関係しないテーブルと相手の無い元テーブルを読み込む() throws IOException {
        this.createPreviewSrc();
        final String setting = this.writeUnmatchedJoinSetting();

        Assertions.assertEquals(3, this.tablePreview(setting, "small").getTotalRows());
        Assertions.assertEquals(BIG_ROWS, this.tablePreview(setting, "big").getTotalRows());
    }

    @Test
    void testUnitTablePreview_unitSettingを適用した元のテーブルとsubTableを返す() throws IOException {
        this.createPreviewSrc();
        this.writeUnitSetting();

        final List<DatasetUnitTablePreviewResponseDto> actual = this.unitTablePreview("", "big", "previewUnitSetting.json");

        Assertions.assertEquals(List.of("big", "ONE"), actual.stream().map(DatasetUnitTablePreviewResponseDto::getTableName).toList());
        final DatasetUnitTablePreviewResponseDto big = actual.getFirst();
        Assertions.assertArrayEquals(new String[]{"key", "value", "label"}, big.getHeaders());
        Assertions.assertEquals(DatasetSettingsController.PREVIEW_ROW_LIMIT, big.getRows().size());
        Assertions.assertEquals(BIG_ROWS, big.getTotalRows());
        Assertions.assertEquals("L1", String.valueOf(big.getRows().getFirst()[2]));
        final DatasetUnitTablePreviewResponseDto one = actual.get(1);
        Assertions.assertEquals(1, one.getTotalRows());
        Assertions.assertEquals("1", String.valueOf(one.getRows().getFirst()[0]));
    }

    @Test
    void testUnitTablePreview_srcのsettingを適用した後のテーブルに適用する() throws IOException {
        this.createPreviewSrc();
        this.writeUnitSetting();

        final List<DatasetUnitTablePreviewResponseDto> actual = this.unitTablePreview(
                this.writeRenameSetting(), "renamed", "previewUnitSetting.json");

        Assertions.assertEquals(List.of("renamed", "ONE"), actual.stream().map(DatasetUnitTablePreviewResponseDto::getTableName).toList());
        Assertions.assertEquals(BIG_ROWS, actual.getFirst().getTotalRows());
    }

    @Test
    void testUnitTablePreview_unitSettingが無ければエラーを返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertThrows(HttpClientResponseException.class,
                () -> this.unitTablePreview("", "big", "missingUnitSetting.json"));
    }

    @Test
    void testTableNames_inlineSettingはsettingより優先し対象のテーブルだけを適用後の名前で返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(List.of("renamed"), this.inlineTableNames(this.writeJoinSetting(),
                Map.of("name", Map.of("any", List.of("big")), "tableName", "renamed")));
    }

    @Test
    void testTableNames_inlineSettingは変換の無い設定でも対象のテーブルを返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(List.of("small"), this.inlineTableNames("",
                Map.of("name", Map.of("any", List.of("small")), "keys", List.of("key"))));
    }

    @Test
    void testTableNames_inlineSettingのjoinは結合結果だけを返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(List.of("joined"), this.inlineTableNames("",
                Map.of("innerJoin", Map.of("left", "big", "right", "small", "column", List.of("key")), "tableName", "joined")));
    }

    @Test
    void testTableNames_inlineSettingのsplitは行を読んで分割後の名前を返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(List.of("small_00", "small_01"), this.inlineTableNames("",
                Map.of("name", Map.of("any", List.of("small")), "split", Map.of("suffix", "_%02d", "limit", 2))));
    }

    @Test
    void testTableNames_inlineSettingのbreakKeyでのsplitも分割後の名前を返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(List.of("small_00", "small_01", "small_02"), this.inlineTableNames("",
                Map.of("name", Map.of("any", List.of("small")),
                        "split", Map.of("suffix", "_%02d", "limit", 1, "breakKey", List.of("key")))));
    }

    @Test
    void testTableNames_inlineSettingのjoinをsplitすれば分割後の名前だけを返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(List.of("joined_00", "joined_01", "joined_02"), this.inlineTableNames("",
                Map.of("innerJoin", Map.of("left", "big", "right", "small", "column", List.of("key")),
                        "split", Map.of("tableName", "joined", "suffix", "_%02d", "limit", 1))));
    }

    @Test
    void testTableNames_inlineSettingに一致するテーブルが無ければ空を返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(List.of(), this.inlineTableNames("",
                Map.of("name", Map.of("any", List.of("nothing")), "tableName", "renamed")));
    }

    @Test
    void testTableNames_inlineXlsxSchemaはxlsxSchemaより優先し対象のテーブルだけを返す() throws IOException {
        this.createPreviewBook();
        final Map<String, Object> body = this.tableBody("xlsx", "", "", Map.of("xlsxSchema", "schema.json"));
        body.put("inlineXlsxSchema", Map.of("rows", List.of(this.inlineRowSetting(List.of()))));

        Assertions.assertEquals(List.of("renamed"), this.tableNames(body));
    }

    @Test
    void testTablePreview_inlineXlsxSchemaのrowsで読み込む() throws IOException {
        this.createPreviewBook();
        final Map<String, Object> body = this.tableBody("xlsx", "", "renamed");
        body.put("inlineXlsxSchema", Map.of("rows", List.of(this.inlineRowSetting(List.of("id")))));

        final DatasetTablePreviewResponseDto actual = this.tablePreview(body);

        Assertions.assertArrayEquals(new String[]{"id", "name"}, actual.getHeaders());
        Assertions.assertEquals(List.of(List.of("1", "a"), List.of("2", "b")), this.rows(actual));
    }

    @Test
    void testTablePreview_inlineXlsxSchemaのcellsで読み込む() throws IOException {
        this.createPreviewBook();
        final Map<String, Object> body = this.tableBody("xlsx", "", "cell");
        body.put("inlineXlsxSchema", Map.of("cells", List.of(Map.of(
                "sheetName", "small",
                "tableName", "cell",
                "header", List.of("first", "second"),
                "rows", List.of(Map.of("cellAddress", List.of("A2", "B3"))),
                "addFileInfo", false))));

        final DatasetTablePreviewResponseDto actual = this.tablePreview(body);

        Assertions.assertArrayEquals(new String[]{"first", "second"}, actual.getHeaders());
        Assertions.assertEquals(List.of(List.of("1", "b")), this.rows(actual));
    }

    @Test
    void testTableNames_settingTargetOnlyならsettingの対象のテーブルだけを返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(List.of("renamed"), this.tableNames(
                this.tableBody("csv", this.writeRenameSetting(), "", Map.of("settingTargetOnly", "true"))));
    }

    @Test
    void testTableNames_settingTargetOnlyでもsettingが無ければ絞らない() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(List.of("big", "small"), this.tableNames(
                this.tableBody("csv", "", "", Map.of("settingTargetOnly", "true"))));
    }

    @Test
    void testTableNames_settingTargetOnlyでなければsettingの対象外のテーブルも返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(List.of("renamed", "small"), this.tableNames(
                this.tableBody("csv", this.writeRenameSetting(), "", Map.of("settingTargetOnly", "false"))));
    }

    @Test
    void testTablePreview_inlineSettingで変えたテーブル名で読み込む() throws IOException {
        this.createPreviewSrc();
        final Map<String, Object> body = this.tableBody("csv", "", "renamed");
        body.put("inlineSetting", Map.of("name", Map.of("any", List.of("big")), "tableName", "renamed"));

        final DatasetTablePreviewResponseDto actual = this.tablePreview(body);

        Assertions.assertArrayEquals(new String[]{"key", "value"}, actual.getHeaders());
        Assertions.assertEquals(BIG_ROWS, actual.getTotalRows());
    }

    @Test
    void testTablePreview_読み込めなければエラーを返す() throws IOException {
        this.createPreviewSrc();

        final HttpClientResponseException ex = Assertions.assertThrows(HttpClientResponseException.class,
                () -> this.tablePreview("missingSetting.json", "big"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
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
    void testTableColumns_テーブル名未指定なら全テーブルの列名を返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(Map.of("big", List.of("key", "value"), "small", List.of("key", "other")),
                this.tableColumns(this.tableBody("csv", "", "")));
    }

    @Test
    void testTableColumns_設定を適用した後のテーブル名と列名を返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(Map.of("joined", List.of("big_key", "big_value", "small_key", "small_other")),
                this.tableColumns(this.tableBody("csv", this.writeJoinSetting(), "joined")));
    }

    @Test
    void testTableColumns_テーブル名を指定すれば対象外のソースを開かない() throws IOException {
        this.createPreviewSrc();
        final String setting = this.writeRenameSetting();

        this.assertOpensOnly("big.csv", () -> Assertions.assertEquals(Map.of("renamed", List.of("key", "value")),
                this.tableColumns(this.tableBody("csv", setting, "renamed"))));
    }

    @Test
    void testTableColumns_inlineSourceはcsvqとして実行したときの列を返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(Map.of(DatasetTableNamesRequestDto.INLINE_SOURCE_TABLE_NAME, List.of("LABEL", "KEY", "OTHER")),
                this.tableColumns(this.inlineSourceBody(DatasetTableNamesRequestDto.INLINE_SOURCE_TABLE_NAME)));
    }

    @Test
    void testTableNames_inlineSourceはsrcの代わりにinlineSourceのテーブルだけを返す() throws IOException {
        this.createPreviewSrc();

        Assertions.assertEquals(List.of(DatasetTableNamesRequestDto.INLINE_SOURCE_TABLE_NAME),
                this.tableNames(this.inlineSourceBody("")));
    }

    @Test
    void testTablePreview_inlineSourceの行を読み込む() throws IOException {
        this.createPreviewSrc();

        final DatasetTablePreviewResponseDto actual = this.tablePreview(
                this.inlineSourceBody(DatasetTableNamesRequestDto.INLINE_SOURCE_TABLE_NAME));

        Assertions.assertArrayEquals(new String[]{"LABEL", "KEY", "OTHER"}, actual.getHeaders());
        Assertions.assertEquals(3, actual.getTotalRows());
        Assertions.assertEquals(List.of("a", "1", "a"),
                Arrays.stream(actual.getRows().getFirst()).map(String::valueOf).toList());
    }

    @Test
    void testTablePreview_inlineSourceが読めなければエラーを返す() throws IOException {
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("srcOptions", Map.of("srcType", "csvq"));
        body.put("inlineSource", "SELECT * FROM");
        body.put("tableName", DatasetTableNamesRequestDto.INLINE_SOURCE_TABLE_NAME);

        final HttpClientResponseException ex = Assertions.assertThrows(HttpClientResponseException.class,
                () -> this.tablePreview(body));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testTableColumns_inlineSourceを読めない形式ならエラーを返す() throws IOException {
        this.createPreviewSrc();
        final Map<String, Object> body = this.inlineSourceBody(DatasetTableNamesRequestDto.INLINE_SOURCE_TABLE_NAME);
        body.put("srcOptions", Map.of("srcType", "csv", "src", PREVIEW_SRC_DIR.toAbsolutePath().toString()));

        final HttpClientResponseException ex = Assertions.assertThrows(HttpClientResponseException.class,
                () -> this.tableColumns(body));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    /**
     * srcのファイルの代わりに読むことを確かめるため、srcにはcsvのあるディレクトリを指定する（csvqとして読めばSQLとして失敗する）
     */
    private Map<String, Object> inlineSourceBody(final String tableName) {
        final Map<String, Object> body = this.tableBody("csvq", "", tableName);
        body.put("inlineSource", "SELECT S.OTHER AS LABEL, S.* FROM CSVREAD('"
                + PREVIEW_SRC_DIR.resolve("small.csv").toAbsolutePath().toString().replace('\\', '/')
                + "',null,'charset=UTF-8') S");
        return body;
    }

    @Test
    void testTableColumns_読み込めなければエラーを返す() throws IOException {
        this.createPreviewSrc();

        final HttpClientResponseException ex = Assertions.assertThrows(HttpClientResponseException.class,
                () -> this.tableColumns(this.tableBody("csv", "missingSetting.json", "big")));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
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

        this.assertOpensOnly("small.csv", () -> this.tableConvert("", "small"));
    }

    @Test
    void testTableConvert_テーブル名を変えない設定なら対象外のソースを開かない() throws IOException {
        this.createPreviewSrc();
        final String setting = this.writeKeysSetting();

        this.assertOpensOnly("small.csv", () -> this.tableConvert(setting, "small"));
    }

    @Test
    void testTablePreview_テーブル名を変えない設定なら対象外のソースを開かない() throws IOException {
        this.createPreviewSrc();
        final String setting = this.writeKeysSetting();

        this.assertOpensOnly("small.csv", () -> this.tablePreview(setting, "small"));
    }

    @Test
    void testTableConvert_テーブル名を変える設定でも対象外のソースを開かない() throws IOException {
        this.createPreviewSrc();
        final String setting = this.writeRenameSetting();

        this.assertOpensOnly("big.csv", () -> this.tableConvert(setting, "renamed"));
    }

    @Test
    void testTablePreview_テーブル名を変える設定でも対象外のソースを開かない() throws IOException {
        this.createPreviewSrc();
        final String setting = this.writeRenameSetting();

        this.assertOpensOnly("big.csv", () -> this.tablePreview(setting, "renamed"));
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
    void testTableConvert_joinがあってもjoinに関係しないテーブルだけを出力する() throws IOException {
        this.createPreviewSrc();
        final Path resultDir = Paths.get(this.tableConvert(
                this.writeUnmatchedJoinSetting(), "small"));

        Assertions.assertEquals(4, Files.readAllLines(resultDir.resolve("small.csv")).size());
        Assertions.assertFalse(Files.exists(resultDir.resolve("big.csv")));
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
        final Map<String, Object> body = this.tableBody(srcType, setting, tableName);
        if (result != null) {
            body.put("result", result);
        }
        return ObjectMapper.getDefault().writeValueAsString(body);
    }

    private Map<String, Object> tableBody(final String srcType, final String setting, final String tableName) {
        return this.tableBody(srcType, setting, tableName, Map.of());
    }

    private Map<String, Object> tableBody(final String srcType, final String setting, final String tableName,
                                          final Map<String, String> otherSrcOptions) {
        final Map<String, String> srcOptions = new LinkedHashMap<>(otherSrcOptions);
        srcOptions.put("setting", setting);
        srcOptions.put("srcType", srcType);
        srcOptions.put("src", PREVIEW_SRC_DIR.toAbsolutePath().toString());
        srcOptions.put("encoding", "UTF-8");
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("srcOptions", srcOptions);
        body.put("tableName", tableName);
        return body;
    }

    /**
     * actionの実行中に開いたcsvファイルがexpectedFileだけであること
     */
    private void assertOpensOnly(final String expectedFile, final IOAction action) throws IOException {
        final List<String> opened = this.produceStartLogs(ComparableCsvDataSetProducer.class, action);
        Assertions.assertEquals(1, opened.size(), opened.toString());
        Assertions.assertTrue(opened.getFirst().endsWith(expectedFile), opened.toString());
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

    /**
     * 全行にlabel列を足し、key=1の行だけのサブテーブルONEを作るunitSetting
     */
    private void writeUnitSetting() throws IOException {
        Files.createDirectories(UNIT_SETTING_DIR);
        Files.writeString(UNIT_SETTING_DIR.resolve("previewUnitSetting.json"),
                "{\"settings\":[{\"pattern\":{\"regex\":\".*\",\"exclude\":[\"ONE\"]},\"separate\":["
                        + "{\"string\":{\"label\":\"'L' + key\"}},"
                        + "{\"tableName\":\"ONE\",\"filter\":[\"key == '1'\"]}]}]}");
    }

    private String writeKeysSetting() throws IOException {
        return this.writeSetting("previewKeysSetting.json",
                "{\"settings\":[{\"name\":[\"small\"],\"keys\":[\"key\"]}]}");
    }

    private String writeJoinSetting() throws IOException {
        return this.writeSetting("previewJoinSetting.json",
                "{\"settings\":[{\"innerJoin\":{\"left\":\"big\",\"right\":\"small\",\"column\":[\"key\"]},\"tableName\":\"joined\"}]}");
    }

    /**
     * bigと存在しないテーブルのjoin。bigはjoinで使われずに残る
     */
    private String writeUnmatchedJoinSetting() throws IOException {
        return this.writeSetting("previewUnmatchedJoinSetting.json",
                "{\"settings\":[{\"innerJoin\":{\"left\":\"big\",\"right\":\"missing\",\"column\":[\"key\"]}}]}");
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

    /**
     * big・smallの2シートのxlsx。smallはA1:B1が見出し、2行目からデータ
     */
    private void createPreviewBook() throws IOException {
        Files.createDirectories(PREVIEW_SRC_DIR);
        try (final XSSFWorkbook book = new XSSFWorkbook();
             final OutputStream out = Files.newOutputStream(PREVIEW_SRC_DIR.resolve("book.xlsx"))) {
            book.createSheet("big").createRow(0).createCell(0).setCellValue("key");
            final Sheet small = book.createSheet("small");
            this.writeRow(small, 0, "id", "name");
            this.writeRow(small, 1, "1", "a");
            this.writeRow(small, 2, "2", "b");
            book.write(out);
        }
    }

    private void writeRow(final Sheet sheet, final int rowNum, final String... values) {
        final Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }

    /**
     * 空のbreakKeyはJSONから省かれる（読込時は空として扱う）。行を読むにはbreakKeyが要る
     */
    private Map<String, Object> inlineRowSetting(final List<String> breakKey) {
        return Map.of(
                "sheetName", "small",
                "tableName", "renamed",
                "header", List.of("id", "name"),
                "dataStart", 1,
                "columnIndex", List.of(0, 1),
                "breakKey", breakKey,
                "addFileInfo", false);
    }

    private List<List<Object>> rows(final DatasetTablePreviewResponseDto preview) {
        return preview.getRows().stream().map(List::of).toList();
    }

    private DatasetTablePreviewResponseDto tablePreview(final Map<String, Object> body) throws IOException {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/dataset-setting/table-preview", ObjectMapper.getDefault().writeValueAsString(body)));
        return ObjectMapper.getDefault().readValue(response, DatasetTablePreviewResponseDto.class);
    }

    private List<DatasetUnitTablePreviewResponseDto> unitTablePreview(final String setting, final String tableName,
                                                                      final String unitSetting) throws IOException {
        final Map<String, Object> body = this.tableBody("csv", setting, tableName);
        body.put("unitSetting", unitSetting);
        body.put("unitSettingEncoding", "UTF-8");
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/dataset-setting/unit-table-preview", ObjectMapper.getDefault().writeValueAsString(body)));
        return List.of(ObjectMapper.getDefault().readValue(response, DatasetUnitTablePreviewResponseDto[].class));
    }

    private List<String> inlineTableNames(final String setting, final Map<String, Object> inlineSetting) throws IOException {
        final Map<String, Object> body = this.tableBody("csv", setting, "");
        body.put("inlineSetting", inlineSetting);
        return this.tableNames(body);
    }

    /**
     * table-namesの結果（名前順）
     */
    private List<String> tableNames(final Map<String, Object> body) throws IOException {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/dataset-setting/table-names", ObjectMapper.getDefault().writeValueAsString(body)));
        return List.of(ObjectMapper.getDefault().readValue(response, String[].class)).stream().sorted().toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> tableColumns(final Map<String, Object> body) throws IOException {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/dataset-setting/table-columns", ObjectMapper.getDefault().writeValueAsString(body)));
        return ObjectMapper.getDefault().readValue(response, Map.class);
    }

    private DatasetTablePreviewResponseDto tablePreview(final String setting, final String tableName) throws IOException {
        return this.tablePreview(this.tableBody("csv", setting, tableName));
    }
}
