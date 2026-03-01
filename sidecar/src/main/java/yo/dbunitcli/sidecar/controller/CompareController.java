package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.command.Compare;
import yo.dbunitcli.application.command.CompareDto;
import yo.dbunitcli.application.command.CompareOption;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@Controller("/compare")
public class CompareController extends AbstractCommandController<CompareDto, CompareOption, Compare> {

    public CompareController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Type getCommandType() {
        return Type.compare;
    }
}
