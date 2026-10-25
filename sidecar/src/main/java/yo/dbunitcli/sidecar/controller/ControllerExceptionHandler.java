package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.hateoas.JsonError;
import yo.dbunitcli.application.OptionValidationException;
import yo.dbunitcli.sidecar.dto.OptionValidationErrorDto;

import java.io.IOException;

public interface ControllerExceptionHandler {

    @Error
    default HttpResponse<?> handleException(final HttpRequest<?> request, final ApplicationException ex) {
        if (ex.getCause() instanceof final OptionValidationException validation) {
            return HttpResponse.status(HttpStatus.BAD_REQUEST, "Invalid Option")
                               .body(OptionValidationErrorDto.of(validation));
        }
        return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, "Fix Input Parameter")
                           .body(new JsonError("Execution failed. cause: " + ex.getMessage()));
    }

    @Error
    default HttpResponse<JsonError> handleIOException(final HttpRequest<?> request, final IOException ex) {
        return HttpResponse.<JsonError>status(HttpStatus.INTERNAL_SERVER_ERROR, "File Operation Failed")
                           .body(new JsonError("File operation failed. cause: " + ex.getMessage()));
    }
}
