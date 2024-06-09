package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.ParameterizeDto;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.sidecar.dto.OptionDto;

import java.io.IOException;
import java.util.HashMap;

@MicronautTest
public class ParameterizeControllerTest {

    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

    @Test
    public void testPost() throws IOException {
        final OptionDto<ParameterizeDto> input = new OptionDto<>();
        final ParameterizeDto request = new ParameterizeDto();
        request.setIgnoreFail("false");
        request.setCmd("convert");
        request.setParameterize("false");
        request.setArg(new HashMap<>());
        request.getArg().put("-src", "src");
        request.getArg().put("-srcType", "file");
        request.getArg().put("-resultPath", "result");
        request.getArg().put("-resultType", "xlsx");
        request.setParamData(new DataSetLoadDto());
        request.getParamData().setSrcType(DataSourceType.none);
        input.setValue(request);
        input.setName("parameterizeTest");
        final String response = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/parameterize/exec"
                , ObjectMapper.getDefault()
                        .writeValueAsString(input)));
        Assertions.assertEquals("success", response);
    }

    @Test
    public void testReset() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/parameterize/reset"));
        System.out.println(jsonResponse);
        Assertions.assertEquals("{\"prefix\":\"\",\"elements\":[{\"name\":\"unit\",\"attribute\":{\"value\":\"record\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"record\",\"table\",\"dataset\"],\"required\":false}}},{\"name\":\"parameterize\",\"attribute\":{\"value\":\"true\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"ignoreFail\",\"attribute\":{\"value\":\"false\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"cmd\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"cmdParam\",\"attribute\":{\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"template\",\"attribute\":{\"attribute\":{\"type\":\"FILE\",\"required\":true}}}],\"paramData\":{\"prefix\":\"param\",\"elements\":[{\"name\":\"srcType\",\"attribute\":{\"value\":\"none\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"none\",\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":true}}},{\"name\":\"src\",\"attribute\":{\"attribute\":{\"type\":\"FILE_OR_DIR\",\"required\":true}}},{\"name\":\"setting\",\"attribute\":{\"attribute\":{\"type\":\"FILE\",\"required\":false}}},{\"name\":\"settingEncoding\",\"attribute\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"loadData\",\"attribute\":{\"value\":\"true\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}},{\"name\":\"includeMetaData\",\"attribute\":{\"value\":\"false\",\"attribute\":{\"type\":\"FLG\",\"required\":false}}}]},\"templateOption\":{\"prefix\":\"template\",\"elements\":[{\"name\":\"encoding\",\"attribute\":{\"value\":\"UTF-8\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"templateGroup\",\"attribute\":{\"attribute\":{\"type\":\"FILE\",\"required\":false}}},{\"name\":\"templateParameterAttribute\",\"attribute\":{\"value\":\"param\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"templateVarStart\",\"attribute\":{\"value\":\"$\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}},{\"name\":\"templateVarStop\",\"attribute\":{\"value\":\"$\",\"attribute\":{\"type\":\"TEXT\",\"required\":false}}}]}}", jsonResponse);
    }

}