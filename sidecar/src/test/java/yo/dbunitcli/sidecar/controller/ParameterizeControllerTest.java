package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
public class ParameterizeControllerTest {

    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Test
    public void testReset() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/parameterize/reset"));
        System.out.println(jsonResponse);
        Assertions.assertEquals("{\"prefix\":\"\",\"elements\":[{\"name\":\"unit\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"record\",\"table\",\"dataset\"],\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"record\"},{\"name\":\"parameterize\",\"attribute\":{\"type\":\"FLG\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"true\"},{\"name\":\"ignoreFail\",\"attribute\":{\"type\":\"FLG\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"false\"},{\"name\":\"cmd\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"\"},{\"name\":\"cmdParam\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"\"},{\"name\":\"template\",\"attribute\":{\"type\":\"FILE\",\"required\":true,\"defaultPath\":\"TEMPLATE\"},\"value\":\"\"}],\"paramData\":{\"prefix\":\"param\",\"elements\":[{\"name\":\"srcType\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"none\",\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"none\"}]},\"templateOption\":{\"prefix\":\"template\",\"elements\":[{\"name\":\"encoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"UTF-8\"},{\"name\":\"templateGroup\",\"attribute\":{\"type\":\"FILE\",\"required\":false,\"defaultPath\":\"TEMPLATE\"},\"value\":\"\"},{\"name\":\"templateParameterAttribute\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"param\"},{\"name\":\"templateVarStart\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"$\"},{\"name\":\"templateVarStop\",\"attribute\":{\"type\":\"TEXT\",\"required\":false,\"defaultPath\":\"WORKSPACE\"},\"value\":\"$\"}]}}", jsonResponse);
    }

}