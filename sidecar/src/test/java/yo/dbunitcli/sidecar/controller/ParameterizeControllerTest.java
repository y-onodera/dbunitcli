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
        request.getArg().put("-recursive", "true");
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
        Assertions.assertEquals("{\"prefix\":\"\",\"elements\":[{\"name\":\"unit\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"record\",\"table\",\"dataset\"],\"required\":false},\"value\":\"record\"},{\"name\":\"parameterize\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"true\"},{\"name\":\"ignoreFail\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"false\"},{\"name\":\"cmd\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"cmdParam\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"template\",\"attribute\":{\"type\":\"FILE\",\"required\":true},\"value\":\"\"}],\"paramData\":{\"prefix\":\"param\",\"elements\":[{\"name\":\"srcType\",\"attribute\":{\"type\":\"ENUM\",\"selectOption\":[\"none\",\"table\",\"sql\",\"file\",\"dir\",\"csv\",\"csvq\",\"reg\",\"fixed\",\"xls\",\"xlsx\"],\"required\":true},\"value\":\"none\"},{\"name\":\"src\",\"attribute\":{\"type\":\"FILE_OR_DIR\",\"required\":true},\"value\":\"\"},{\"name\":\"setting\",\"attribute\":{\"type\":\"FILE\",\"required\":false},\"value\":\"\"},{\"name\":\"settingEncoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"UTF-8\"},{\"name\":\"regTableInclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"regTableExclude\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"\"},{\"name\":\"loadData\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"true\"},{\"name\":\"includeMetaData\",\"attribute\":{\"type\":\"FLG\",\"required\":false},\"value\":\"false\"}]},\"templateOption\":{\"prefix\":\"template\",\"elements\":[{\"name\":\"encoding\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"UTF-8\"},{\"name\":\"templateGroup\",\"attribute\":{\"type\":\"FILE\",\"required\":false},\"value\":\"\"},{\"name\":\"templateParameterAttribute\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"param\"},{\"name\":\"templateVarStart\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"$\"},{\"name\":\"templateVarStop\",\"attribute\":{\"type\":\"TEXT\",\"required\":false},\"value\":\"$\"}]}}", jsonResponse);
    }

}