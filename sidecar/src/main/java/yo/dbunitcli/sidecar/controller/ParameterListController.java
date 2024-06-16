package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.serde.ObjectMapper;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.io.IOException;

@Controller("/parameter")
public class ParameterListController {
    private final Workspace workspace;

    public ParameterListController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Get(uri = "list", produces = MediaType.APPLICATION_JSON)
    public String list() throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.workspace.parameterFiles());
    }

}
