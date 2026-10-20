package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.job.JobManager;

@Controller("/compare")
public class CompareController extends AbstractCommandController {

    public CompareController(final Workspace workspace, final JobManager jobs) {
        super(workspace, jobs);
    }

    @Override
    protected Type getCommandType() {
        return Type.compare;
    }
}
