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
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "src/test/resources/workspace/sample")
class GenerateControllerTest {
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Workspace workspace;

    @Test
    public void testReset() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/generate/reset"));
        System.out.println(jsonResponse);
        Assertions.assertEquals("{\"prefix\":\"\",\"elements\":[{\"name\":\"generateType\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"txt\",\"xlsx\",\"xls\",\"settings\",\"sql\"],\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"txt\"},{\"name\":\"unit\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"record\",\"table\",\"dataset\"],\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"record\"},{\"name\":\"template\",\"attribute\":{\"type\":\"FILE\",\"required\":true,\"defaultPath\":\"TEMPLATE\"},\"value\":\"\"},{\"name\":\"result\",\"attribute\":{\"type\":\"DIR\",\"required\":false,\"defaultPath\":\"RESULT\"},\"value\":\"\"},{\"name\":\"resultPath\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"result\"},{\"name\":\"outputEncoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"UTF-8\"}],\"srcData\":{\"prefix\":\"src\",\"elements\":[{\"name\":\"srcType\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"csv\"},{\"name\":\"src\",\"attribute\":{\"type\":\"FILE_OR_DIR\",\"required\":true,\"defaultPath\":\"DATASET\"},\"value\":\"\"},{\"name\":\"recursive\",\"attribute\":{\"type\":\"FLG\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"false\"},{\"name\":\"regInclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"\"},{\"name\":\"regExclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"\"},{\"name\":\"extension\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"\"},{\"name\":\"encoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"UTF-8\"},{\"name\":\"headerName\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"\"},{\"name\":\"delimiter\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\",\"},{\"name\":\"ignoreQuoted\",\"attribute\":{\"type\":\"FLG\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"false\"},{\"name\":\"regTableInclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"\"},{\"name\":\"regTableExclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"\"},{\"name\":\"loadData\",\"attribute\":{\"type\":\"FLG\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"true\"},{\"name\":\"includeMetaData\",\"attribute\":{\"type\":\"FLG\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"false\"},{\"name\":\"setting\",\"attribute\":{\"type\":\"FILE\",\"required\":false,\"defaultPath\":\"SETTING\"},\"value\":\"\"},{\"name\":\"settingEncoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"UTF-8\"}]},\"templateOption\":{\"prefix\":\"template\",\"elements\":[{\"name\":\"encoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"UTF-8\"},{\"name\":\"templateGroup\",\"attribute\":{\"type\":\"FILE\",\"required\":false,\"defaultPath\":\"TEMPLATE\"},\"value\":\"\"},{\"name\":\"templateParameterAttribute\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"param\"},{\"name\":\"templateVarStart\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"$\"},{\"name\":\"templateVarStop\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"$\"}]}}", jsonResponse);
    }

}