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
        System.out.println(jsonResponse);
        Assertions.assertEquals("{\"prefix\":\"\",\"elements\":[],\"srcData\":{\"prefix\":\"src\",\"elements\":[{\"name\":\"srcType\",\"attribute\":{\"value\":\"csv\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"none\",\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":true}}},{\"name\":\"src\",\"attribute\":{\"value\":\"C:/dev/IdeaProjects/dbunitcli/sidecar/src/test/resources/workspace/sample/resources/src/csv/\",\"attribute\":{\"type\":\"FILE_OR_DIR\",\"required\":true}}},{\"name\":\"setting\",\"attribute\":{\"attribute\":{\"type\":\"FILE\",\"required\":false}}},{\"name\":\"settingEncoding\",\"attribute\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"loadData\",\"attribute\":{\"value\":\"true\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"includeMetaData\",\"attribute\":{\"value\":\"false\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"recursive\",\"attribute\":{\"value\":\"false\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"regInclude\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"regExclude\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"extension\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"encoding\",\"attribute\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"headerName\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"delimiter\",\"attribute\":{\"value\":\",\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}}]},\"convertResult\":{\"prefix\":\"result\",\"elements\":[{\"name\":\"resultType\",\"attribute\":{\"value\":\"xlsx\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"csv\",\"xls\",\"xlsx\",\"table\"],\"required\":false}}},{\"name\":\"result\",\"attribute\":{\"value\":\"C:/dev/IdeaProjects/dbunitcli/sidecar/target/convert/result\",\"attribute\":{\"type\":\"DIR\",\"required\":false}}},{\"name\":\"resultPath\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"exportEmptyTable\",\"attribute\":{\"value\":\"true\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"excelTable\",\"attribute\":{\"value\":\"SHEET\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}}]}}", jsonResponse);
    }

    @Test
    public void testReset() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/convert/reset"));
        System.out.println(jsonResponse);
        Assertions.assertEquals("{\"prefix\":\"\",\"elements\":[],\"srcData\":{\"prefix\":\"src\",\"elements\":[{\"name\":\"srcType\",\"attribute\":{\"value\":\"csv\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"none\",\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":true}}},{\"name\":\"src\",\"attribute\":{\"attribute\":{\"type\":\"FILE_OR_DIR\",\"required\":true}}},{\"name\":\"setting\",\"attribute\":{\"attribute\":{\"type\":\"FILE\",\"required\":false}}},{\"name\":\"settingEncoding\",\"attribute\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"loadData\",\"attribute\":{\"value\":\"true\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"includeMetaData\",\"attribute\":{\"value\":\"false\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"recursive\",\"attribute\":{\"value\":\"false\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"regInclude\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"regExclude\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"extension\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"encoding\",\"attribute\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"headerName\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"delimiter\",\"attribute\":{\"value\":\",\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}}]},\"convertResult\":{\"prefix\":\"result\",\"elements\":[{\"name\":\"resultType\",\"attribute\":{\"value\":\"csv\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"csv\",\"xls\",\"xlsx\",\"table\"],\"required\":false}}},{\"name\":\"result\",\"attribute\":{\"value\":\"C:/dev/IdeaProjects/dbunitcli/sidecar/./\",\"attribute\":{\"type\":\"DIR\",\"required\":false}}},{\"name\":\"resultPath\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"exportEmptyTable\",\"attribute\":{\"value\":\"true\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"outputEncoding\",\"attribute\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}}]}}", jsonResponse);
    }

}