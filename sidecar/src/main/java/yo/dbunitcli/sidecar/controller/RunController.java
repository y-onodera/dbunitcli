package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.Run;
import yo.dbunitcli.application.RunOption;
import yo.dbunitcli.application.RunDto;
import yo.dbunitcli.sidecar.domain.project.CommandType;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@Controller("/run")
public class RunController extends AbstractCommandController<RunDto, RunOption, Run> {

    public RunController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Run getCommand() {
        return new Run();
    }

    @Override
    protected CommandType getCommandType() {
        return CommandType.run;
    }
}
