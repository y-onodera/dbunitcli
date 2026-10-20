package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.hateoas.JsonError;

import java.io.IOException;

public interface ControllerExceptionHandler {

    @Error
    default HttpResponse<JsonError> handleException(final HttpRequest<?> request, final ApplicationException ex) {
        return ErrorResult.of(ex).toResponse();
    }

    @Error
    default HttpResponse<JsonError> handleIOException(final HttpRequest<?> request, final IOException ex) {
        return ErrorResult.of(ex).toResponse();
    }

    /** 例外→応答の対応。同期APIのエラーハンドラとジョブの完了イベントで共有する */
    record ErrorResult(HttpStatus status, String reason, String message) {

        public static ErrorResult of(final ApplicationException ex) {
            return new ErrorResult(HttpStatus.BAD_REQUEST, "Fix Input Parameter", "Execution failed. cause: " + ex.getMessage());
        }

        public static ErrorResult of(final IOException ex) {
            return new ErrorResult(HttpStatus.INTERNAL_SERVER_ERROR, "File Operation Failed", "File operation failed. cause: " + ex.getMessage());
        }

        HttpResponse<JsonError> toResponse() {
            return HttpResponse.<JsonError>status(this.status, this.reason).body(new JsonError(this.message));
        }
    }
}
