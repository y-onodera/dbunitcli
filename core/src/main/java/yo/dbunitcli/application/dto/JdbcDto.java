package yo.dbunitcli.application.dto;

import picocli.CommandLine;

public class JdbcDto {
    @CommandLine.Option(names = "-jdbcProperties", description = "use connect database. [url=,user=,pass=]")
    private String jdbcProperties;

    @CommandLine.Option(names = "-jdbcUrl", description = "use connect database. override jdbcProperties value")
    private String jdbcUrl;

    @CommandLine.Option(names = "-jdbcUser", description = "use connect database. override jdbcProperties value")
    private String jdbcUser;

    @CommandLine.Option(names = "-jdbcPass", description = "use connect database. override jdbcProperties value")
    private String jdbcPass;

    public String getJdbcProperties() {
        return this.jdbcProperties;
    }

    public void setJdbcProperties(final String jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }

    public String getJdbcUrl() {
        return this.jdbcUrl;
    }

    public void setJdbcUrl(final String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUser() {
        return this.jdbcUser;
    }

    public void setJdbcUser(final String jdbcUser) {
        this.jdbcUser = jdbcUser;
    }

    public String getJdbcPass() {
        return this.jdbcPass;
    }

    public void setJdbcPass(final String jdbcPass) {
        this.jdbcPass = jdbcPass;
    }
}
