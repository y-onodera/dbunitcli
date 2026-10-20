package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

/**
 * ジョブのSSEイベント。typeが"log"ならmessageにログ行、"done"ならstatus以降に実行結果を持つ。
 */
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobEventDto {
    public static final String TYPE_LOG = "log";
    public static final String TYPE_DONE = "done";

    private String type;
    private String message;
    private String status;
    private Integer httpStatus;
    private String reason;
    private String result;

    public static JobEventDto log(final String message) {
        final JobEventDto dto = new JobEventDto();
        dto.setType(TYPE_LOG);
        dto.setMessage(message);
        return dto;
    }

    public static JobEventDto done(final String status, final int httpStatus, final String reason,
                                   final String result, final String message) {
        final JobEventDto dto = new JobEventDto();
        dto.setType(TYPE_DONE);
        dto.setStatus(status);
        dto.setHttpStatus(httpStatus);
        dto.setReason(reason);
        dto.setResult(result);
        dto.setMessage(message);
        return dto;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public Integer getHttpStatus() {
        return this.httpStatus;
    }

    public void setHttpStatus(final Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(final String result) {
        this.result = result;
    }
}
