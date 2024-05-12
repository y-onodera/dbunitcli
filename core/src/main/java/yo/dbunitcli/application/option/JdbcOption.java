package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.JdbcDto;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class JdbcOption implements Option {

    private final String prefix;
    private final String jdbcUrl;
    private final String jdbcUser;
    private final String jdbcPass;
    private final File jdbcProperties;

    public JdbcOption(final String prefix) {
        this(prefix, new JdbcDto());
    }

    public JdbcOption(final String prefix, final JdbcDto dto) {
        this.prefix = prefix;
        if (Strings.isNotEmpty(dto.getJdbcProperties())) {
            this.jdbcProperties = new File(dto.getJdbcProperties());
        } else {
            this.jdbcProperties = null;
        }
        this.jdbcUrl = dto.getJdbcUrl();
        this.jdbcUser = dto.getJdbcUser();
        this.jdbcPass = dto.getJdbcPass();
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.putFile("-jdbcProperties", this.jdbcProperties);
        if (Optional.ofNullable(result.get("-jdbcProperties")).orElse("").isEmpty()) {
            result.put("-jdbcUrl", this.jdbcUrl);
            result.put("-jdbcUser", this.jdbcUser);
            result.put("-jdbcPass", this.jdbcPass);
        }
        return result;
    }

    public DatabaseConnectionLoader getDatabaseConnectionLoader() {
        try {
            return new DatabaseConnectionLoader(this.loadJdbcTemplate());
        } catch (final IOException e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    protected Properties loadJdbcTemplate() throws IOException {
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

    protected void validate() {
        if (Stream.of(this.jdbcUrl, this.jdbcUser, this.jdbcPass)
                .anyMatch(it -> Optional.ofNullable(it).orElse("").isEmpty())) {
            if (this.jdbcProperties == null) {
                throw new AssertionError("need jdbcProperties option", new IllegalArgumentException());
            }
            if (!this.jdbcProperties.exists()) {
                throw new AssertionError(this.jdbcProperties + " is not exist file", new IllegalArgumentException(this.jdbcProperties.toString()));
            }
        }
    }

}
