package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.converter.FixedColumnDef;
import yo.dbunitcli.dataset.converter.FixedColumnDefTemplate;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.FixedColumnDefRequestDto;

import java.io.IOException;
import java.util.List;

@Controller("fixed-column-def")
public class FixedColumnDefController extends AbstractResourceFileController<FixedColumnDefRequestDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixedColumnDefController.class);

    public FixedColumnDefController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().fixedColumnDef();
    }

    @Post(uri = "save")
    @Override
    public String save(@Body final FixedColumnDefRequestDto body) throws IOException {
        try {
            final List<FixedColumnDef> columns = body.getInput().getColumns().stream()
                    .map(col -> new FixedColumnDef(col.name(), col.length(),
                            !"right".equalsIgnoreCase(col.align()), col.pad()))
                    .toList();
            this.getResourceFile().update(body.getName(), new FixedColumnDefTemplate().render(columns));
        } catch (final IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.currentFileList();
    }
}
