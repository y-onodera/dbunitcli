package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/convert")
public class ConvertController extends AbstractCommandController {
    public ConvertController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Type getCommandType() {
        return Type.convert;
    }
}
