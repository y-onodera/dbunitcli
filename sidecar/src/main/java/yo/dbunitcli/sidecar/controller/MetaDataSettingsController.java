package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.json.stream.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.MetaDataRequestDto;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

@Controller("metadata")
public class MetaDataSettingsController {


    private static final Logger LOGGER = LoggerFactory.getLogger(MetaDataSettingsController.class);

    private final Workspace workspace;

    public MetaDataSettingsController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Get(uri = "list", produces = MediaType.APPLICATION_JSON)
    public String list() throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.workspace.metadataSettings());
    }

    @Post(uri = "load", consumes = MediaType.TEXT_PLAIN, produces = MediaType.APPLICATION_JSON)
    public String load(@Body final String name) {
        return this.workspace.metadataSetting(name);
    }

    @Post(uri = "save", produces = MediaType.TEXT_PLAIN)
    public String save(@Body final MetaDataRequestDto body) throws IOException {
        JsonObject jsonObject = Json.createReader(new StringReader(JsonMapper.createDefault()
                        .writeValueAsString(body.getInput())))
                .readObject();
        // test parse success
        new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jsonObject)
                .build();
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = Json.createWriterFactory(
                        Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true))
                .createWriter(stringWriter);
        jsonWriter.writeObject(jsonObject);
        jsonWriter.close();
        LOGGER.info(stringWriter.toString());
        this.workspace.resources().update(body.getName(), stringWriter.toString());
        return "success";
    }

    @Error
    public HttpResponse<JsonError> handleException(final HttpRequest<?> request, final ApplicationException ex) {
        return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, "Fix Input Parameter")
                .body(new JsonError("Execution failed. cause: " + ex.getMessage()));
    }


}
