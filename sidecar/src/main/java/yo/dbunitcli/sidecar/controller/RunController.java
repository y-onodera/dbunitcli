package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.command.Run;
import yo.dbunitcli.application.command.RunDto;
import yo.dbunitcli.application.command.RunOption;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@Controller("/run")
public class RunController extends AbstractCommandController<RunDto, RunOption, Run> {

    public RunController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Type getCommandType() {
        return Type.run;
    }
}
