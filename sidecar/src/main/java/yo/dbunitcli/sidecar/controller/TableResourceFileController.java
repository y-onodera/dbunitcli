package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import jakarta.inject.Inject;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.nio.charset.Charset;

@Controller("table")
public class TableResourceFileController extends AbstractTextResourceFileController {

    @Inject
    public TableResourceFileController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().table();
    }

    @Override
    protected Charset defaultCharset() {
        return Charset.defaultCharset();
    }
}
