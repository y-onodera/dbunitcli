package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.JsonXlsxSchemaRequestDto;

@Controller("xlsx-schema")
public class XlsxSchemaController extends AbstractResourceFileController<JsonXlsxSchemaRequestDto> {

    public XlsxSchemaController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().xlsxSchema();
    }
}