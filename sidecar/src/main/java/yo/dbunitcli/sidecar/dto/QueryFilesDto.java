package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

import java.util.ArrayList;
import java.util.List;

@Serdeable
@JsonInclude(JsonInclude.Include.ALWAYS)
public class QueryFilesDto {
    private List<String> csvq = new ArrayList<>();
    private List<String> sql = new ArrayList<>();
    private List<String> table = new ArrayList<>();

    public List<String> getCsvq() {
        return this.csvq;
    }

    public void setCsvq(final List<String> csvq) {
        this.csvq = csvq;
    }

    public List<String> getSql() {
        return this.sql;
    }

    public void setSql(final List<String> sql) {
        this.sql = sql;
    }

    public List<String> getTable() {
        return this.table;
    }

    public void setTable(final List<String> table) {
        this.table = table;
    }
}