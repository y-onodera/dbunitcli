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

}