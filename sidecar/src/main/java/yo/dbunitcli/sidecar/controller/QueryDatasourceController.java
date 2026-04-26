package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.sidecar.domain.project.Datasource;
import yo.dbunitcli.sidecar.dto.QueryDataSourceDto;

import java.io.IOException;

@Controller("/query-datasource")
public class QueryDatasourceController implements ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDatasourceController.class);

    @Get(uri = "list", produces = MediaType.APPLICATION_JSON)
    public String list() {
        return this.currentFileList();
    }

    @Post(uri = "load", produces = MediaType.TEXT_PLAIN)
    public String load(@Body final QueryDataSourceDto request) {
        try {
            return new Datasource().read(request.getName());
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "save", produces = MediaType.APPLICATION_JSON)
    public String save(@Body final QueryDataSourceDto request) throws IOException {
        try {
            new Datasource().save(request.getName(), request.getContents());
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.currentFileList();
    }

    @Post(uri = "delete", produces = MediaType.APPLICATION_JSON)
    public String delete(@Body final QueryDataSourceDto request) throws IOException {
        try {
            new Datasource().delete(request.getName());
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.currentFileList();
    }

    private String currentFileList() {
        try {
            return ObjectMapper
                    .getDefault()
                    .writeValueAsString(new Datasource().list());
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

}
