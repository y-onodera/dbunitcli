package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import jakarta.inject.Inject;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.nio.charset.Charset;

@Controller("parameterize/template")
public class ParameterizeTemplateResourceFileController extends AbstractTextResourceFileController {

    @Inject
    public ParameterizeTemplateResourceFileController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.options().templates();
    }

    @Override
    protected Charset defaultCharset() {
        return Charset.defaultCharset();
    }
}
