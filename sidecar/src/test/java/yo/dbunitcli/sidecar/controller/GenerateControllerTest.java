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
        Assertions.assertEquals("{\"prefix\":\"\",\"elements\":[{\"name\":\"generateType\",\"attribute\":{\"value\":\"txt\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"txt\",\"xlsx\",\"xls\",\"settings\",\"sql\"],\"required\":false}}},{\"name\":\"unit\",\"attribute\":{\"value\":\"record\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"record\",\"table\",\"dataset\"],\"required\":false}}},{\"name\":\"template\",\"attribute\":{\"attribute\":{\"type\":\"FILE\",\"required\":true}}},{\"name\":\"outputEncoding\",\"attribute\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}}],\"srcData\":{\"prefix\":\"src\",\"elements\":[{\"name\":\"srcType\",\"attribute\":{\"value\":\"csv\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"none\",\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":true}}},{\"name\":\"src\",\"attribute\":{\"attribute\":{\"type\":\"FILE_OR_DIR\",\"required\":true}}},{\"name\":\"setting\",\"attribute\":{\"attribute\":{\"type\":\"FILE\",\"required\":false}}},{\"name\":\"settingEncoding\",\"attribute\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"loadData\",\"attribute\":{\"value\":\"true\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"includeMetaData\",\"attribute\":{\"value\":\"false\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"recursive\",\"attribute\":{\"value\":\"false\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"regInclude\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"regExclude\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"extension\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"encoding\",\"attribute\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"headerName\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"delimiter\",\"attribute\":{\"value\":\",\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}}]},\"templateOption\":{\"prefix\":\"template\",\"elements\":[{\"name\":\"encoding\",\"attribute\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"templateGroup\",\"attribute\":{\"attribute\":{\"type\":\"FILE\",\"required\":false}}},{\"name\":\"templateParameterAttribute\",\"attribute\":{\"value\":\"param\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"templateVarStart\",\"attribute\":{\"value\":\"$\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"templateVarStop\",\"attribute\":{\"value\":\"$\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}}]}}", jsonResponse);
    }

}