package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.Compare;
import yo.dbunitcli.application.CompareDto;
import yo.dbunitcli.application.CompareOption;
import yo.dbunitcli.sidecar.domain.project.CommandType;
import yo.dbunitcli.sidecar.domain.project.Workspace;

@Controller("/compare")
public class CompareController extends AbstractCommandController<CompareDto, CompareOption, Compare> {

    public CompareController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected String resultDir(final CompareOption options) {
        return options.result().convertResult().resultDir().getAbsoluteFile().getPath();
    }

    @Override
    protected Compare getCommand() {
        return new Compare();
    }

    @Override
    protected CommandType getCommandType() {
        return CommandType.compare;
    }
}
