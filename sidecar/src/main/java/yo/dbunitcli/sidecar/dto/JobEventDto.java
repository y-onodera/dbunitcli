package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

/**
 * ジョブのSSEイベント。typeが"log"ならmessageにログ行、"done"ならstatus以降に実行結果を持つ。
 */
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobEventDto(String type, String message, String status, Integer httpStatus, String reason, String result) {
    public static final String TYPE_LOG = "log";
    public static final String TYPE_DONE = "done";

    public static JobEventDto log(final String message) {
        return new JobEventDto(TYPE_LOG, message, null, null, null, null);
    }

    public static JobEventDto done(final String status, final int httpStatus, final String reason,
                                   final String result, final String message) {
        return new JobEventDto(TYPE_DONE, message, status, httpStatus, reason, result);
    }
}
