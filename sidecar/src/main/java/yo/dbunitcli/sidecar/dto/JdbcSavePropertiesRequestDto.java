package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class JdbcSavePropertiesRequestDto {

    private String name;

    private String url;

    private String user;

    private String pass;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

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
}
