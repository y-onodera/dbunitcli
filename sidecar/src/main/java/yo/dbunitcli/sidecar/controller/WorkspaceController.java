package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.ContextDto;

import java.io.IOException;

@Controller("/workspace")
public class WorkspaceController implements ControllerExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceController.class);
    private final Workspace workspace;

    public WorkspaceController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Get(uri = "resources", produces = MediaType.APPLICATION_JSON)
    public String resources() {
        try {
            return this.currentResources();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "update", produces = MediaType.APPLICATION_JSON)
    public String update(@Body final ContextDto context) {
        try {
            this.workspace.contextReload(context.getWorkspace(), context.getDatasetBase(), context.getResultBase());
            return this.currentResources();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    private String currentResources() throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.workspace.toDto());
    }

}
