package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.FixedColumnDefRequestDto;

@Controller("fixed-column-def")
public class FixedColumnDefController extends AbstractResourceFileController<FixedColumnDefRequestDto> {

    public FixedColumnDefController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().fixedColumnDef();
    }
}
