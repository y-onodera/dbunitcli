package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.hateoas.JsonError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("dataset")
public class DatasetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);

    @Error
    public HttpResponse<JsonError> handleException(final HttpRequest<?> request, final ApplicationException ex) {
        return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, "Fix Input Parameter")
                .body(new JsonError("Execution failed. cause: " + ex.getMessage()));
    }


}
