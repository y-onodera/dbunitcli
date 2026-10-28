package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Map;

@Serdeable
public class DatasetTableConvertRequestDto extends DatasetTablePreviewRequestDto {

    /**
     * convertのresultフォームの値（-result.resultType等）
     */
    private Map<String, String> result;

    public Map<String, String> getResult() {
        return this.result;
    }

    public void setResult(final Map<String, String> result) {
        this.result = result;
    }
}
