package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import jakarta.inject.Inject;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.nio.charset.Charset;

@Controller("csvq")
public class CsvqResourceFileController extends AbstractTextResourceFileController {

    @Inject
    public CsvqResourceFileController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().csvq();
    }

    @Override
    protected Charset defaultCharset() {
        return Charset.defaultCharset();
    }
}
