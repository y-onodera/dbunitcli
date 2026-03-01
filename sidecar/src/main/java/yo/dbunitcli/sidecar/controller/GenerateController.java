package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.command.Generate;
import yo.dbunitcli.application.command.GenerateDto;
import yo.dbunitcli.application.command.GenerateOption;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@Controller("/generate")
public class GenerateController extends AbstractCommandController<GenerateDto, GenerateOption, Generate> {

    public GenerateController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Type getCommandType() {
        return Type.generate;
    }

}
