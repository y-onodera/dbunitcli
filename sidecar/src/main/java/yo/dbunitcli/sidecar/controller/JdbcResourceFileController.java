package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import yo.dbunitcli.application.option.JdbcOption;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.JdbcSavePropertiesRequestDto;
import yo.dbunitcli.sidecar.dto.JdbcDto;
import yo.dbunitcli.sidecar.dto.ResourceSaveRequest;

import java.io.IOException;
import java.io.StringWriter;

@Controller("jdbc")
public class JdbcResourceFileController extends AbstractResourceFileController<ResourceSaveRequest<?>> {

    @Inject
    public JdbcResourceFileController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().jdbc();
    }

    @Post(uri = "save-properties", produces = MediaType.APPLICATION_JSON)
    public String saveProperties(@Body final JdbcSavePropertiesRequestDto body) throws IOException {
        final JdbcDto jdbc = body.getInput();
        final JdbcOption option = new JdbcOption("jdbc",
                jdbc.getProperties(),
                jdbc.getUrl(),
                jdbc.getUser(),
                jdbc.getPass());
        final StringWriter sw = new StringWriter();
        option.loadJdbcTemplate().store(sw, null);
        this.getResourceFile().update(body.getName(), sw.toString());
        return this.currentFileList();
    }

    @Post(uri = "test", produces = MediaType.APPLICATION_JSON)
    public String test(@Body final JdbcDto body) {
        try {
            final JdbcOption option = new JdbcOption("jdbc",
                    body.getProperties(),
                    body.getUrl(),
                    body.getUser(),
                    body.getPass());
            option.getDatabaseConnectionLoader().loadConnection().getConnection().close();
            return "{\"success\":true,\"message\":\"connection opened\"}";
        } catch (final Throwable e) {
            final Throwable cause = e.getCause() != null ? e.getCause() : e;
            return "{\"success\":false,\"message\":" + toJsonString(cause.getMessage()) + "}";
        }
    }

    private static String toJsonString(final String s) {
        if (s == null) {
            return "\"\"";
        }
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r") + "\"";
    }
}
