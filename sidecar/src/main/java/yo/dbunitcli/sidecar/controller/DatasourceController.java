package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.sidecar.domain.project.Datasource;
import yo.dbunitcli.sidecar.dto.DataSourceDto;

import java.io.IOException;

@Controller("/datasource")
public class DatasourceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceController.class);

    @Get(uri = "list", produces = MediaType.APPLICATION_JSON)
    public String list(@QueryValue final DataSourceType type) throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(new Datasource(type).list());
    }

    @Post(uri = "load", produces = MediaType.TEXT_PLAIN)
    public String load(@Body final DataSourceDto request) {
        return new Datasource(request.getType()).read(request.getFileName());
    }

    @Post(uri = "save")
    public HttpResponse<String> save(@Body final DataSourceDto request) {
        try {
            new Datasource(request.getType()).save(request.getFileName(), request.getContents());
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            return HttpResponse.serverError("Failed to save file: " + th.getMessage());
        }
        return HttpResponse.ok("success");
    }

    @Post(uri = "delete")
    public HttpResponse<String> delete(@Body final DataSourceDto request) {
        try {
            new Datasource(request.getType()).delete(request.getFileName());
            return HttpResponse.ok("success");
        } catch (final IOException e) {
            LOGGER.error("Failed to delete file: {}", request.getFileName(), e);
            return HttpResponse.serverError("Failed to delete file: " + e.getMessage());
        }
    }

    @Error
    public HttpResponse<JsonError> handleException(final HttpRequest<?> request, final ApplicationException ex) {
        return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, "Fix Input Parameter")
                .body(new JsonError("Execution failed. cause: " + ex.getMessage()));
    }

}
