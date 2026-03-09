package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.option.JdbcOption;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.JdbcSavePropertiesRequestDto;
import yo.dbunitcli.sidecar.dto.JdbcDto;
import yo.dbunitcli.sidecar.dto.ResourceSaveRequest;

import org.dbunit.database.IDatabaseConnection;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;

@Controller("jdbc")
public class JdbcResourceFileController extends AbstractResourceFileController<ResourceSaveRequest<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcResourceFileController.class);

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

    @Post(uri = "read-content", consumes = MediaType.TEXT_PLAIN, produces = MediaType.APPLICATION_JSON)
    public String readContent(@Body final String path) {
        try {
            final java.util.Properties props = new JdbcOption("jdbc", path, null, null, null).loadJdbcTemplate();
            final LinkedHashMap<String, String> map = new LinkedHashMap<>();
            for (final String key : props.stringPropertyNames()) {
                map.put(key, props.getProperty(key));
            }
            return ObjectMapper.getDefault().writeValueAsString(map);
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            return "{}";
        }
    }

    @Post(uri = "tables", produces = MediaType.APPLICATION_JSON)
    public String tables(@Body final JdbcDto body) {
        IDatabaseConnection conn = null;
        try {
            final JdbcOption option = new JdbcOption("jdbc",
                    body.getProperties(),
                    body.getUrl(),
                    body.getUser(),
                    body.getPass());
            conn = option.getDatabaseConnectionLoader().loadConnection();
            final String[] tableNames = conn.createDataSet().getTableNames();
            return ObjectMapper.getDefault().writeValueAsString(tableNames);
        } catch (final Throwable e) {
            LOGGER.error("Failed to get table list", e);
            return "[]";
        } finally {
            if (conn != null) {
                try {
                    conn.getConnection().close();
                } catch (final Exception ex) {
                    LOGGER.error("Failed to close connection", ex);
                }
            }
        }
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
