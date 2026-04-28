package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class JdbcColumnsRequestDto {

    private String url;
    private String user;
    private String pass;
    private String properties;
    private String table;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getPass() {
        return this.pass;
    }

    public void setPass(final String pass) {
        this.pass = pass;
    }

    public String getProperties() {
        return this.properties;
    }

    public void setProperties(final String properties) {
        this.properties = properties;
    }

    public String getTable() {
        return this.table;
    }

    public void setTable(final String table) {
        this.table = table;
    }
}
