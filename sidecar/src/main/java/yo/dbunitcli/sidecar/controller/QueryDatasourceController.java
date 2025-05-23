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
import yo.dbunitcli.sidecar.dto.QueryDataSourceDto;

import java.io.IOException;

@Controller("/query-datasource")
public class QueryDatasourceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDatasourceController.class);

    @Get(uri = "list", produces = MediaType.APPLICATION_JSON)
    public String list(@QueryValue final DataSourceType type) throws IOException {
        return this.currentFileList(type);
    }

    @Post(uri = "load", produces = MediaType.TEXT_PLAIN)
    public String load(@Body final QueryDataSourceDto request) {
        return new Datasource(request.getType()).read(request.getName());
    }

    @Post(uri = "save", produces = MediaType.APPLICATION_JSON)
    public String save(@Body final QueryDataSourceDto request) throws IOException {
        try {
            new Datasource(request.getType()).save(request.getName(), request.getContents());
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
        }
        return this.currentFileList(request.getType());
    }

    @Post(uri = "delete", produces = MediaType.APPLICATION_JSON)
    public String delete(@Body final QueryDataSourceDto request) throws IOException {
        try {
            new Datasource(request.getType()).delete(request.getName());
        } catch (final IOException e) {
            LOGGER.error("Failed to delete file: {}", request.getName(), e);
        }
        return this.currentFileList(request.getType());
    }

    @Error
    public HttpResponse<JsonError> handleException(final HttpRequest<?> request, final ApplicationException ex) {
        return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, "Fix Input Parameter")
                .body(new JsonError("Execution failed. cause: " + ex.getMessage()));
    }

    private String currentFileList(final DataSourceType type) throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(new Datasource(type).list());
    }

}
