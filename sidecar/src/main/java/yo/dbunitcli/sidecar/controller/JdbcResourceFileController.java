package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.option.JdbcOption;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.JdbcSavePropertiesRequestDto;
import yo.dbunitcli.sidecar.dto.JdbcTestRequestDto;
import yo.dbunitcli.sidecar.dto.ResourceSaveRequest;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Properties;

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
        final Properties props = new Properties();
        if (Strings.isNotEmpty(body.getProperties())) {
            final Optional<String> existing = this.getResourceFile().read(body.getProperties());
            if (existing.isPresent()) {
                props.load(new StringReader(existing.get()));
            }
        }
        if (Strings.isNotEmpty(body.getUrl())) {
            props.setProperty("url", body.getUrl());
        }
        if (Strings.isNotEmpty(body.getUser())) {
            props.setProperty("user", body.getUser());
        }
        if (Strings.isNotEmpty(body.getPass())) {
            props.setProperty("pass", body.getPass());
        }
        final StringWriter sw = new StringWriter();
        props.store(sw, null);
        this.getResourceFile().update(body.getName(), sw.toString());
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