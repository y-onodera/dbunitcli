package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import jakarta.inject.Inject;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.ResourceSaveRequest;

@Controller("jdbc")
public class JdbcResourceFileController extends AbstractResourceFileController<ResourceSaveRequest<?>> {

    @Inject
    public JdbcResourceFileController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().jdbc();
    }
}