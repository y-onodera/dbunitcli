package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.ConvertDto;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.io.File;
import java.io.IOException;

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
    public void testLoadAndExec() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/convert/load"
                , "csvToXlsx.txt"));
        final String response = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/convert/exec", jsonResponse));
        Assertions.assertEquals("success", response);
    }

    @Test
    public void testList() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.GET("dbunit-cli/convert/list"));
        Assertions.assertEquals("[\"csvToXlsx.txt\"]", jsonResponse);
    }

    @Test
    public void testSave() throws IOException {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/convert/load"
                , "csvToXlsx.txt"));
        final ConvertDto dto = ObjectMapper.getDefault()
                .readValue(jsonResponse, ConvertDto.class);
        final OptionDto<ConvertDto> input = new OptionDto<>();
        dto.getDataSetLoad().setRecursive("false");
        dto.getDataSetLoad().setDelimiter("\\t");
        input.setName("newName");
        input.setValue(dto);
        final String response = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/convert/save"
                , ObjectMapper.getDefault().writeValueAsString(input)));
        Assertions.assertEquals("success", response);
        Assertions.assertTrue(new File(this.workspace.path().toFile(), "option/convert/newName.txt").exists());
        Assertions.assertTrue(new File(this.workspace.path().toFile(), "option/convert/newName.txt").delete());
    }


}