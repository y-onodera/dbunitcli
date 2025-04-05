package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonWriter;
import jakarta.json.stream.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.ResourceSaveRequest;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

public abstract class AbstractResourceFileController<DTO extends ResourceSaveRequest<?>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResourceFileController.class);
    protected final Workspace workspace;

    protected AbstractResourceFileController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Get(uri = "list", produces = MediaType.APPLICATION_JSON)
    public String list() throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.getResourceFile().list());
    }

    @Post(uri = "load", consumes = MediaType.TEXT_PLAIN, produces = MediaType.APPLICATION_JSON)
    public String load(@Body final String name) {
        return this.getResourceFile().read(name).orElse("{}");
    }

    @Post(uri = "save", produces = MediaType.TEXT_PLAIN)
    public HttpResponse<String> save(@Body final DTO body) {
        try {
            this.saveJson(body.getName(), JsonMapper.createDefault().writeValueAsString(body.getInput()));
            return HttpResponse.ok("success");
        } catch (final IOException e) {
            LOGGER.error("Failed to save file: {}", body, e);
            return HttpResponse.serverError("Failed to save file: " + e.getMessage());
        }
    }

    @Post(uri = "delete", consumes = MediaType.TEXT_PLAIN)
    public HttpResponse<String> delete(@Body final String name) {
        try {
            this.getResourceFile().delete(name);
            return HttpResponse.ok("success");
        } catch (final IOException e) {
            LOGGER.error("Failed to delete file: {}", name, e);
            return HttpResponse.serverError("Failed to delete file: " + e.getMessage());
        }
    }

    @Error
    public HttpResponse<JsonError> handleException(final HttpRequest<?> request, final ApplicationException ex) {
        return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, "Fix Input Parameter")
                .body(new JsonError("Execution failed. cause: " + ex.getMessage()));
    }

    protected void saveJson(final String name, final String json) throws IOException {
        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = Json.createWriterFactory(
                        Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true))
                .createWriter(stringWriter);
        jsonWriter.writeObject(Json.createReader(new StringReader(json)).readObject());
        jsonWriter.close();
        LOGGER.info(stringWriter.toString());
        this.getResourceFile().update(name, stringWriter.toString());
    }

    protected abstract ResourceFile getResourceFile();
}