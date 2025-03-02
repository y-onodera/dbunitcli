package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.ContextDto;

import java.io.IOException;

@Controller("/workspace")
public class WorkspaceController {
    private final ApplicationContext applicationContext;

    public WorkspaceController(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Get(uri = "resources", produces = MediaType.APPLICATION_JSON)
    public String resources() throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.applicationContext.getBean(Workspace.class).toDto());
    }

    @Post(uri = "update", produces = MediaType.TEXT_PLAIN)
    public String update(@Body final ContextDto context) {
        final Workspace newWorkspace = Workspace.contextReload(context.getWorkspace(), context.getDatasetBase(), context.getResultBase());
        this.applicationContext.registerSingleton(Workspace.class, newWorkspace);
        return "success";
    }

}
