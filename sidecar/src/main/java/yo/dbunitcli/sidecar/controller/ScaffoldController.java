package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@Controller("/scaffold")
public class ScaffoldController extends AbstractCommandController {

    public ScaffoldController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Type getCommandType() {
        return Type.scaffold;
    }
}
