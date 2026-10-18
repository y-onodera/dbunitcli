package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

/**
 * 他の設定ファイルの取り込み。outputFilterCombineは"and"(既定)または"or"。
 */
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportDto {
    private String path;
    private String outputFilterCombine;

    public String getPath() {
        return this.path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getOutputFilterCombine() {
        return this.outputFilterCombine;
    }

    public void setOutputFilterCombine(final String outputFilterCombine) {
        this.outputFilterCombine = outputFilterCombine;
    }
}
