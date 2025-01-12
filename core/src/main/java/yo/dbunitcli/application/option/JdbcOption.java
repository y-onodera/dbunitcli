package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.JdbcDto;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public record JdbcOption(
        String prefix
        , String jdbcProperties
        , String jdbcUrl
        , String jdbcUser
        , String jdbcPass
) implements Option {


    public JdbcOption(final String prefix) {
        this(prefix, new JdbcDto());
    }

    public JdbcOption(final String prefix, final JdbcDto dto) {
        this(prefix
                , dto.getJdbcProperties()
                , dto.getJdbcUrl()
                , dto.getJdbcUser()
                , dto.getJdbcPass());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .putFile("-jdbcProperties", this.jdbcProperties, BaseDir.JDBC)
                .put("-jdbcUrl", this.jdbcUrl)
                .put("-jdbcUser", this.jdbcUser)
                .put("-jdbcPass", this.jdbcPass);
    }

    public DatabaseConnectionLoader getDatabaseConnectionLoader() {
        try {
            return new DatabaseConnectionLoader(this.loadJdbcTemplate());
        } catch (final IOException e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    private Properties loadJdbcTemplate() throws IOException {
        final Properties jdbcProp = new Properties();
        if (Strings.isNotEmpty(this.jdbcProperties)) {
            jdbcProp.load(new FileInputStream(FileResources.searchJdbc(this.jdbcProperties)));
        }
        if (Strings.isNotEmpty(this.jdbcUrl)) {
            jdbcProp.put("url", this.jdbcUrl);
        }
        if (Strings.isNotEmpty(this.jdbcUser)) {
            jdbcProp.put("user", this.jdbcUser);
        }
        if (Strings.isNotEmpty(this.jdbcPass)) {
            jdbcProp.put("pass", this.jdbcPass);
        }
        return jdbcProp;
    }

}
