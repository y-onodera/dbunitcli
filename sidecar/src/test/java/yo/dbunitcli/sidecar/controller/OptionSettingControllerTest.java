package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import yo.dbunitcli.resource.FileResources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * compareの-setting（compare/setting）・generateの-unitSetting（generate/setting）の保存先が
 * option/compare/setting・option/generate/settingになることを確認する
 */
@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class OptionSettingControllerTest {

    private static final Path WORKSPACE = Paths.get("target/test-temp/workspace/sample");

    private static final String SAVED = "savedOptionSetting.json";

    @Inject
    @Client("/")
    HttpClient client;

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(WORKSPACE.resolve(FileResources.OPTION_COMPARE_SETTING_PATH).resolve(SAVED));
        Files.deleteIfExists(WORKSPACE.resolve(FileResources.OPTION_GENERATE_SETTING_PATH).resolve(SAVED));
    }

    @ParameterizedTest
    @CsvSource({"compare, option/compare/setting", "generate, option/generate/setting"})
    void testSave_optionのsettingに保存しloadと削除ができる(final String command, final String dir) throws IOException {
        final String base = "dbunit-cli/" + command + "/setting/";
        final String list = this.client.toBlocking()
                .retrieve(HttpRequest.POST(base + "save",
                        "{\"name\":\"" + SAVED + "\",\"input\":{\"settings\":[{\"name\":{\"any\":[\"TABLE_B\"]},\"keys\":[\"id\"]}],\"commonSettings\":[]}}"));
        Assertions.assertTrue(list.contains(SAVED), list);
        final Path saved = WORKSPACE.resolve(dir).resolve(SAVED);
        Assertions.assertTrue(Files.exists(saved), saved + " に作成されること");
        Assertions.assertFalse(Files.exists(WORKSPACE.resolve(FileResources.RESOURCES_SETTING_PATH).resolve(SAVED)),
                "resources/setting には作成されないこと");

        final String loaded = this.client.toBlocking()
                .retrieve(HttpRequest.POST(base + "load", SAVED).contentType(MediaType.TEXT_PLAIN_TYPE));
        Assertions.assertTrue(loaded.contains("TABLE_B"), loaded);

        final String afterDelete = this.client.toBlocking()
                .retrieve(HttpRequest.POST(base + "delete", SAVED).contentType(MediaType.TEXT_PLAIN_TYPE));
        Assertions.assertFalse(afterDelete.contains(SAVED), afterDelete);
        Assertions.assertFalse(Files.exists(saved), saved + " が削除されること");
    }

    @ParameterizedTest
    @CsvSource({"compare", "generate"})
    void testLoad_resourcesのsettingにある以前のファイルも読める(final String command) {
        final String loaded = this.client.toBlocking()
                .retrieve(HttpRequest.POST("dbunit-cli/" + command + "/setting/load", "test.json")
                        .contentType(MediaType.TEXT_PLAIN_TYPE));
        Assertions.assertNotEquals("{}", loaded);
    }
}
