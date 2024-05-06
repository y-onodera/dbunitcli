package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.Parameterize;
import yo.dbunitcli.application.ParameterizeDto;
import yo.dbunitcli.application.ParameterizeOption;
import yo.dbunitcli.sidecar.domain.project.CommandType;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@Controller("/parameterize")
public class ParameterizeController extends AbstractCommandController<ParameterizeDto, ParameterizeOption, Parameterize> {

    public ParameterizeController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Parameterize getCommand() {
        return new Parameterize();
    }

    @Override
    protected ParameterizeOption getOption() {
        return new ParameterizeOption();
    }

    @Override
    protected CommandType getCommandType() {
        return CommandType.parameterize;
    }
}
