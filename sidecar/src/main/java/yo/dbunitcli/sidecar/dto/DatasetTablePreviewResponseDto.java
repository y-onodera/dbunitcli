package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class DatasetTablePreviewResponseDto {

    private String[] headers;
    private List<String[]> rows;

    public DatasetTablePreviewResponseDto(final String[] headers, final List<String[]> rows) {
        this.headers = headers;
        this.rows = rows;
    }

    public String[] getHeaders() {
        return this.headers;
    }

    public void setHeaders(final String[] headers) {
        this.headers = headers;
    }

    public List<String[]> getRows() {
        return this.rows;
    }

    public void setRows(final List<String[]> rows) {
        this.rows = rows;
    }
}
