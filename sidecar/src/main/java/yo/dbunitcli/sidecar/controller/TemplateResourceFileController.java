package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import jakarta.inject.Inject;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.ResourceSaveRequest;

@Controller("template")
public class TemplateResourceFileController extends AbstractResourceFileController<ResourceSaveRequest<?>> {

    @Inject
    public TemplateResourceFileController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().template();
    }
}