package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@MicronautTest
@Property(name = "yo.dbunit.cli.sidecar.workspace", value = "src/test/resources/workspace/sample")
class ConvertControllerTest {
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Workspace workspace;

    @Test
    public void testList() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/convert/list"));
        Assertions.assertEquals("[\"csvToXlsx.txt\"]", jsonResponse);
    }

    @Test
    public void testLoad() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/convert/load"
                , "csvToXlsx.txt"));
        Assertions.assertEquals("{\"srcData\":{\"srcType\":{\"value\":\"csv\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"none\",\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":true}},\"src\":{\"value\":\"src\\\\test\\\\resources\\\\workspace\\\\sample\\\\resources\\\\src\\\\csv\",\"attribute\":{\"type\":\"FILE_OR_DIR\",\"required\":true}},\"setting\":{\"attribute\":{\"type\":\"FILE\",\"required\":false}},\"settingEncoding\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"loadData\":{\"value\":\"true\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"includeMetaData\":{\"value\":\"false\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"regInclude\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"regExclude\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"encoding\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"headerName\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"delimiter\":{\"value\":\",\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"extension\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"recursive\":{\"value\":\"true\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},\"convertResult\":{\"resultType\":{\"value\":\"xlsx\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"csv\",\"xls\",\"xlsx\",\"table\"],\"required\":false}},\"result\":{\"value\":\"target\\\\convert\\\\result\",\"attribute\":{\"type\":\"DIR\",\"required\":false}},\"resultPath\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"exportEmptyTable\":{\"value\":\"true\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"excelTable\":{\"value\":\"SHEET\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}}}", jsonResponse);
    }

    @Test
    public void testReset() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/convert/reset"));
        Assertions.assertEquals("{\"srcData\":{\"srcType\":{\"value\":\"csv\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"none\",\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":true}},\"src\":{\"attribute\":{\"type\":\"FILE_OR_DIR\",\"required\":true}},\"setting\":{\"attribute\":{\"type\":\"FILE\",\"required\":false}},\"settingEncoding\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"loadData\":{\"value\":\"true\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"includeMetaData\":{\"value\":\"false\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"regInclude\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"regExclude\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"encoding\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"headerName\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"delimiter\":{\"value\":\",\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"extension\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"recursive\":{\"value\":\"true\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},\"convertResult\":{\"resultType\":{\"value\":\"csv\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"csv\",\"xls\",\"xlsx\",\"table\"],\"required\":false}},\"result\":{\"value\":\".\",\"attribute\":{\"type\":\"DIR\",\"required\":false}},\"resultPath\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"exportEmptyTable\":{\"value\":\"true\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}},\"outputEncoding\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}}}", jsonResponse);
    }

}