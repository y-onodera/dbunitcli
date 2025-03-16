package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.MetaDataRequestDto;

@Controller("metadata")
public class MetaDataSettingsController extends AbstractResourceFileController<MetaDataRequestDto> {

    public MetaDataSettingsController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().metadataSetting();
    }
}
