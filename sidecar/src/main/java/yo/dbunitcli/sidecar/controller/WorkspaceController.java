package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.serde.ObjectMapper;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.io.IOException;

@Controller("/workspace")
public class WorkspaceController {
    private final Workspace workspace;

    public WorkspaceController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Get(uri = "resources", produces = MediaType.APPLICATION_JSON)
    public String resources() throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.workspace.toDto());
    }

}
