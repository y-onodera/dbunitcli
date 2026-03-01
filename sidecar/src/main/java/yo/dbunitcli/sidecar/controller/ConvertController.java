package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.command.Convert;
import yo.dbunitcli.application.command.ConvertDto;
import yo.dbunitcli.application.command.ConvertOption;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@Controller("/convert")
public class ConvertController extends AbstractCommandController<ConvertDto, ConvertOption, Convert> {
    public ConvertController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Type getCommandType() {
        return Type.convert;
    }
}
