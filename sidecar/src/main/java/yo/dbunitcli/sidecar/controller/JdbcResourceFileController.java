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
import yo.dbunitcli.sidecar.dto.JdbcTestRequestDto;
import yo.dbunitcli.sidecar.dto.ResourceSaveRequest;

import java.io.IOException;

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
        final java.util.Properties props = new java.util.Properties();
        if (body.getProperties() != null && !body.getProperties().isEmpty()) {
            final java.util.Optional<String> existing = this.getResourceFile().read(body.getProperties());
            if (existing.isPresent()) {
                props.load(new java.io.StringReader(existing.get()));
            }
        }
        if (body.getUrl() != null && !body.getUrl().isEmpty()) {
            props.setProperty("url", body.getUrl());
        }
        if (body.getUser() != null && !body.getUser().isEmpty()) {
            props.setProperty("user", body.getUser());
        }
        if (body.getPass() != null && !body.getPass().isEmpty()) {
            props.setProperty("pass", body.getPass());
        }
        final StringBuilder sb = new StringBuilder();
        props.forEach((k, v) -> sb.append(k).append("=").append(v).append("\n"));
        this.getResourceFile().update(body.getName(), sb.toString());
        return this.currentFileList();
    }

    @Post(uri = "test", produces = MediaType.APPLICATION_JSON)
    public String test(@Body final JdbcTestRequestDto body) {
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