package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import jakarta.inject.Inject;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.DatasetRequestDto;

/**
 * generateの-unitSettingのJSONをoption/generate/settingに保存・読込・削除する（形式はdataset-settingと同じ）
 */
@Controller("generate/setting")
public class GenerateSettingController extends AbstractEncodedResourceFileController<DatasetRequestDto> {

    @Inject
    public GenerateSettingController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.options().generateSettings();
    }
}
