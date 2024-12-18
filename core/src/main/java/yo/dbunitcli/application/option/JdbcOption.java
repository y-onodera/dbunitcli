package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.JdbcDto;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public record JdbcOption(
        String prefix
        , File jdbcProperties
        , String jdbcUrl
        , String jdbcUser
        , String jdbcPass
) implements Option {


    public JdbcOption(final String prefix) {
        this(prefix, new JdbcDto());
    }

    public JdbcOption(final String prefix, final JdbcDto dto) {
        this(prefix
                , Strings.isNotEmpty(dto.getJdbcProperties()) ? FileResources.searchInOrderWorkspace(dto.getJdbcProperties()) : null
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
                .putFile("-jdbcProperties", this.jdbcProperties)
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
        if (this.jdbcProperties != null) {
            jdbcProp.load(new FileInputStream(this.jdbcProperties));
        }
        if (!Optional.ofNullable(this.jdbcUrl).orElse("").isEmpty()) {
            jdbcProp.put("url", this.jdbcUrl);
        }
        if (!Optional.ofNullable(this.jdbcUser).orElse("").isEmpty()) {
            jdbcProp.put("user", this.jdbcUser);
        }
        if (!Optional.ofNullable(this.jdbcPass).orElse("").isEmpty()) {
            jdbcProp.put("pass", this.jdbcPass);
        }
        return jdbcProp;
    }

}
