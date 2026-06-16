package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.dataset.converter.FixedColumnDef;
import yo.dbunitcli.dataset.converter.FixedColumnDefTemplate;
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

    @Override
    protected String serializeJson(final FixedColumnDefRequestDto body) {
        return new FixedColumnDefTemplate().render(
                body.getInput().getColumns().stream()
                        .map(col -> new FixedColumnDef(col.name(), col.length(),
                                !"right".equalsIgnoreCase(col.align()), col.pad()))
                        .toList());
    }
}
