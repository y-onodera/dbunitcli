package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import jakarta.inject.Inject;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.DatasetRequestDto;

import java.nio.charset.Charset;

/**
 * compareの-setting（比較キーの設定）のJSONをoption/compare/settingに保存・読込・削除する（形式はdataset-settingと同じ）
 */
@Controller("compare/setting")
public class CompareSettingController extends AbstractResourceFileController<DatasetRequestDto> {

    @Inject
    public CompareSettingController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.options().compareSettings();
    }

    @Override
    protected Charset defaultCharset() {
        return Charset.defaultCharset();
    }
}
