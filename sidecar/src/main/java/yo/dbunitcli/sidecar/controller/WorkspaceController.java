package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.ContextDto;
import yo.dbunitcli.sidecar.dto.ResolvePathRequestDto;

import java.io.File;
import java.io.IOException;

@Controller("/workspace")
public class WorkspaceController implements ControllerExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceController.class);
    private final Workspace workspace;

    public WorkspaceController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Get(uri = "resources", produces = MediaType.APPLICATION_JSON)
    public String resources() {
        try {
            return this.currentResources();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "update", produces = MediaType.APPLICATION_JSON)
    public String update(@Body final ContextDto context) {
        try {
            this.workspace.contextReload(context.getWorkspace(), context.getDatasetBase(), context.getResultBase(), context.getEnv());
            return this.currentResources();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "resolve-path", produces = MediaType.TEXT_PLAIN)
    public String resolvePath(@Body final ResolvePathRequestDto request) {
        try {
            final String path = request.getPath();
            if (path == null || path.isEmpty()) {
                return "";
            }
            final File absolute = new File(path);
            if (absolute.isAbsolute() && absolute.exists()) {
                return absolute.getAbsolutePath();
            }
            // 実行時と同じ順に探し、見つからなければworkspace・起動ディレクトリからの相対パスも試す
            for (final File candidate : new File[]{
                    toBaseDir(request.getDefaultPath()).search(path),
                    new File(FileResources.baseDir(), path),
                    new File(System.getProperty("user.dir"), path)
            }) {
                if (candidate != null && candidate.exists()) {
                    return candidate.getAbsolutePath();
                }
            }
            return "";
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    private String currentResources() throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.workspace.toDto());
    }

    private static Option.BaseDir toBaseDir(final String defaultPath) {
        if (defaultPath == null) {
            return Option.BaseDir.WORKSPACE;
        }
        try {
            return Option.BaseDir.valueOf(defaultPath);
        } catch (final IllegalArgumentException e) {
            return Option.BaseDir.WORKSPACE;
        }
    }
}
