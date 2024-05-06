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
class ConvertControllerTest {
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;

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
    public void testSave() {
        final String jsonResponse = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/convert/load"
                , "csvToXlsx.txt"));
        final String response = this.client.toBlocking().retrieve(HttpRequest.POST("dbunit-cli/convert/save", jsonResponse));
        Assertions.assertEquals("success", response);
    }


}