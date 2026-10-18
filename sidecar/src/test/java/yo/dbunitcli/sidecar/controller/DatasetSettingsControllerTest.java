package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
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
class DatasetSettingsControllerTest {

    private static final Path SETTING_DIR =
            Paths.get("target/test-temp/workspace/sample/resources/setting");


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
}
