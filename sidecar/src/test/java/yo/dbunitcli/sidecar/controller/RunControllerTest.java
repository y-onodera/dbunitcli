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
class RunControllerTest {

    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    Workspace workspace;

    @Test
    public void testReset() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/run/reset"));
        System.out.println(jsonResponse);
        Assertions.assertEquals("{\"prefix\":\"\",\"elements\":[{\"name\":\"scriptType\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"cmd\",\"bat\",\"sql\",\"ant\"],\"required\":false},\"value\":\"sql\"}],\"srcData\":{\"prefix\":\"src\",\"elements\":[{\"name\":\"srcType\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":true},\"value\":\"file\"},{\"name\":\"src\",\"attribute\":{\"type\":\"FILE_OR_DIR\",\"required\":true},\"value\":\"\"},{\"name\":\"setting\",\"attribute\":{\"type\":\"FILE\",\"required\":false},\"value\":\"\"},{\"name\":\"settingEncoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"UTF-8\"},{\"name\":\"regTableInclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"regTableExclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"loadData\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"true\"},{\"name\":\"includeMetaData\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"false\"},{\"name\":\"recursive\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"false\"},{\"name\":\"regInclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"regExclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"extension\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"}]},\"templateOption\":{\"prefix\":\"template\",\"elements\":[{\"name\":\"encoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"UTF-8\"},{\"name\":\"templateGroup\",\"attribute\":{\"type\":\"FILE\",\"required\":false},\"value\":\"\"},{\"name\":\"templateParameterAttribute\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"param\"},{\"name\":\"templateVarStart\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"$\"},{\"name\":\"templateVarStop\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"$\"}]},\"jdbcOption\":{\"prefix\":\"jdbc\",\"elements\":[{\"name\":\"jdbcProperties\",\"attribute\":{\"type\":\"FILE\",\"required\":false},\"value\":\"\"},{\"name\":\"jdbcUrl\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"jdbcUser\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"jdbcPass\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"}]}}", jsonResponse);
    }

}