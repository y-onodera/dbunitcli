package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.Generate;
import yo.dbunitcli.application.GenerateOption;
import yo.dbunitcli.application.GenerateDto;
import yo.dbunitcli.sidecar.domain.project.CommandType;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@Controller("/generate")
public class GenerateController extends AbstractCommandController<GenerateDto, GenerateOption, Generate> {

    public GenerateController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Generate getCommand() {
        return new Generate();
    }

    @Override
    protected CommandType getCommandType() {
        return CommandType.generate;
    }
}
