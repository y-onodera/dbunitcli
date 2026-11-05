package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * xlsxSchema・各settingの保存・読込が、リクエストのencodingで行われることを確認する
 */
@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class EncodedResourceFileControllerTest {

    private static final Path WORKSPACE = Paths.get("target/test-temp/workspace/sample");

    private static final String SAVED = "savedEncodedResource.json";

    private static final String SETTING_INPUT = "{\"settings\":[{\"name\":{\"any\":[\"表B\"]},\"keys\":[\"id\"]}],\"commonSettings\":[]}";

    private static final String XLSX_SCHEMA_INPUT = "{\"rows\":[{\"sheetName\":\"シート2\",\"tableName\":\"表B\",\"header\":[\"id\"],\"dataStart\":2,\"columnIndex\":[1],\"breakKey\":[],\"addFileInfo\":false}],\"cells\":[]}";

    @Inject
    @Client("/")
    HttpClient client;

    @AfterEach
    void tearDown() throws IOException {
        for (final String dir : List.of(FileResources.RESOURCES_SETTING_PATH
                , FileResources.RESOURCES_XLSX_SCHEMA_PATH
                , FileResources.OPTION_COMPARE_SETTING_PATH
                , FileResources.OPTION_GENERATE_SETTING_PATH)) {
            Files.deleteIfExists(WORKSPACE.resolve(dir).resolve(SAVED));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "dataset-setting, resources/setting, MS932",
            "dataset-setting, resources/setting, UTF-8",
            "compare/setting, option/compare/setting, MS932",
            "generate/setting, option/generate/setting, MS932",
            "xlsx-schema, resources/xlsx-schema, MS932",
            "xlsx-schema, resources/xlsx-schema, UTF-8",
    })
    void testSaveAndLoad_指定した文字コードで保存し読み込む(final String base, final String dir, final String encoding) throws IOException {
        final String input = base.equals("xlsx-schema") ? XLSX_SCHEMA_INPUT : SETTING_INPUT;
        this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/" + base + "/save",
                "{\"name\":\"" + SAVED + "\",\"encoding\":\"" + encoding + "\",\"input\":" + input + "}"));

        final String saved = Files.readString(WORKSPACE.resolve(dir).resolve(SAVED), Charset.forName(encoding));
        Assertions.assertTrue(saved.contains("表B"), encoding + "で書かれていること: " + saved);

        final String loaded = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/" + base + "/load",
                "{\"name\":\"" + SAVED + "\",\"encoding\":\"" + encoding + "\"}"));
        Assertions.assertTrue(loaded.contains("表B"), encoding + "で読まれること: " + loaded);
    }
}
