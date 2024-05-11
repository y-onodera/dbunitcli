package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.Convert;
import yo.dbunitcli.application.ConvertDto;
import yo.dbunitcli.application.ConvertOption;
import yo.dbunitcli.sidecar.domain.project.CommandType;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@Controller("/convert")
public class ConvertController extends AbstractCommandController<ConvertDto, ConvertOption, Convert> {
    public ConvertController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Convert getCommand() {
        return new Convert();
    }

    @Override
    protected CommandType getCommandType() {
        return CommandType.convert;
    }
}
