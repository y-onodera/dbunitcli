package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.serde.ObjectMapper;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.io.IOException;

@Controller("metadata")
public class MetaDataSettingsController {

    private final Workspace workspace;

    public MetaDataSettingsController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Get(uri = "list", produces = MediaType.APPLICATION_JSON)
    public String list() throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.workspace.settings());
    }

    @Post(uri = "load", consumes = MediaType.TEXT_PLAIN, produces = MediaType.APPLICATION_JSON)
    public String load(@Body final String name) {
        return this.workspace.setting(name);
    }

    @Error
    public HttpResponse<JsonError> handleException(final HttpRequest<?> request, final ApplicationException ex) {
        return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, "Fix Input Parameter")
                .body(new JsonError("Execution failed. cause: " + ex.getMessage()));
    }


}
