package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.sidecar.domain.project.Datasource;
import yo.dbunitcli.sidecar.dto.DataSourceDto;

@Controller("/datasource")
public class DatasourceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceController.class);

    @Post(uri = "save", produces = MediaType.TEXT_PLAIN)
    public String save(@Body final DataSourceDto request) {
        try {
            new Datasource().save(request.getType(), request.getFileName(), request.getContents());
        } catch (final Throwable th) {
            DatasourceController.LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return "success";
    }
}
