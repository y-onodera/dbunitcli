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
class CompareControllerTest {
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Workspace workspace;

    @Test
    public void testReset() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/compare/reset"));
        System.out.println(jsonResponse);
        Assertions.assertEquals("{\"prefix\":\"\",\"elements\":[{\"name\":\"targetType\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"data\",\"image\",\"pdf\"],\"required\":false},\"value\":\"data\"},{\"name\":\"setting\",\"attribute\":{\"type\":\"FILE\",\"required\":false},\"value\":\"\"},{\"name\":\"settingEncoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"UTF-8\"}],\"newData\":{\"prefix\":\"new\",\"elements\":[{\"name\":\"srcType\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":false},\"value\":\"csv\"},{\"name\":\"src\",\"attribute\":{\"type\":\"FILE_OR_DIR\",\"required\":true},\"value\":\"\"},{\"name\":\"recursive\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"false\"},{\"name\":\"regInclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"regExclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"extension\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"encoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"UTF-8\"},{\"name\":\"headerName\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"delimiter\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\",\"},{\"name\":\"regTableInclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"regTableExclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"loadData\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"true\"},{\"name\":\"includeMetaData\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"false\"},{\"name\":\"setting\",\"attribute\":{\"type\":\"FILE\",\"required\":false},\"value\":\"\"},{\"name\":\"settingEncoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"UTF-8\"}]},\"oldData\":{\"prefix\":\"old\",\"elements\":[{\"name\":\"srcType\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":false},\"value\":\"csv\"},{\"name\":\"src\",\"attribute\":{\"type\":\"FILE_OR_DIR\",\"required\":true},\"value\":\"\"},{\"name\":\"recursive\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"false\"},{\"name\":\"regInclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"regExclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"extension\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"encoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"UTF-8\"},{\"name\":\"headerName\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"delimiter\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\",\"},{\"name\":\"regTableInclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"regTableExclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"loadData\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"true\"},{\"name\":\"includeMetaData\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"false\"},{\"name\":\"setting\",\"attribute\":{\"type\":\"FILE\",\"required\":false},\"value\":\"\"},{\"name\":\"settingEncoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"UTF-8\"}]},\"convertResult\":{\"prefix\":\"result\",\"elements\":[{\"name\":\"resultType\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"csv\",\"xls\",\"xlsx\"],\"required\":true},\"value\":\"csv\"},{\"name\":\"result\",\"attribute\":{\"type\":\"DIR\",\"required\":false},\"value\":\".\"},{\"name\":\"resultPath\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"exportEmptyTable\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"true\"},{\"name\":\"outputEncoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"UTF-8\"}]},\"expectData\":{\"prefix\":\"expect\",\"elements\":[{\"name\":\"srcType\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"none\",\"sql\",\"csv\",\"csvq\",\"xls\",\"xlsx\"],\"required\":false},\"value\":\"none\"}]}}", jsonResponse);
    }

}