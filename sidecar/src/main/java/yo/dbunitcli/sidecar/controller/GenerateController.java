package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/generate")
public class GenerateController extends AbstractCommandController {

    public GenerateController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Type getCommandType() {
        return Type.generate;
    }

}
