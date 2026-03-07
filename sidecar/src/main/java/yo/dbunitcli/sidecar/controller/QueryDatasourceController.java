package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.sidecar.domain.project.Datasource;
import yo.dbunitcli.sidecar.dto.QueryDataSourceDto;

import java.io.IOException;

@Controller("/query-datasource")
public class QueryDatasourceController implements ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDatasourceController.class);

    @Get(uri = "list", produces = MediaType.APPLICATION_JSON)
    public String list(@QueryValue final DataSourceType type) {
        return this.currentFileList(type);
    }

    @Post(uri = "load", produces = MediaType.TEXT_PLAIN)
    public String load(@Body final QueryDataSourceDto request) {
        try {
            return new Datasource(request.getType()).read(request.getName());
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "save", produces = MediaType.APPLICATION_JSON)
    public String save(@Body final QueryDataSourceDto request) throws IOException {
        try {
            new Datasource(request.getType()).save(request.getName(), request.getContents());
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.currentFileList(request.getType());
    }

    @Post(uri = "delete", produces = MediaType.APPLICATION_JSON)
    public String delete(@Body final QueryDataSourceDto request) throws IOException {
        try {
            new Datasource(request.getType()).delete(request.getName());
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.currentFileList(request.getType());
    }

    private String currentFileList(final DataSourceType type) {
        try {
            return ObjectMapper
                    .getDefault()
                    .writeValueAsString(new Datasource(type).list());
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

}
