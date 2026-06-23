package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import org.stringtemplate.v4.ST;
import yo.dbunitcli.application.command.GenerateType;
import yo.dbunitcli.dataset.converter.FixedColumnDef;
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
        final ST st = new ST(GenerateType.fixedColumnDef.getStGroup(),
                GenerateType.fixedColumnDef.getTemplateString(null));
        st.add("columns", body.getInput().getColumns().stream()
                .map(col -> new FixedColumnDef(col.name(), col.length(),
                        col.align(), col.pad()))
                .toList());
        return st.render();
    }
}
