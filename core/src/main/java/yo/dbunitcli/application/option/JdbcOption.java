package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.JdbcDto;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class JdbcOption implements OptionParser<JdbcDto> {

    private final String prefix;
    private File jdbcProperties;

    private String jdbcUrl;

    private String jdbcUser;

    private String jdbcPass;

    private Properties jdbcProp;

    public JdbcOption(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putFile("-jdbcProperties", this.jdbcProperties);
        if (Optional.ofNullable(result.get("-jdbcProperties")).orElse("").isEmpty()) {
            result.put("-jdbcUrl", this.jdbcUrl);
            result.put("-jdbcUser", this.jdbcUser);
            result.put("-jdbcPass", this.jdbcPass);
        }
        return result;
    }

    @Override
    public void setUpComponent(final JdbcDto dto) {
        if (Strings.isNotEmpty(dto.getJdbcProperties())) {
            this.jdbcProperties = new File(dto.getJdbcProperties());
        }
        this.jdbcUrl = dto.getJdbcUrl();
        this.jdbcUser = dto.getJdbcUser();
        this.jdbcPass = dto.getJdbcPass();
        try {
            this.loadJdbcTemplate();
        } catch (final IOException e) {
            throw new AssertionError(e.getMessage(), e);
        }
        this.validate();
    }

    public Properties getJdbcProp() {
        return this.jdbcProp;
    }

    public DatabaseConnectionLoader getDatabaseConnectionLoader() {
        return new DatabaseConnectionLoader(this.getJdbcProp());
    }

    protected void validate() {
        if (Stream.of(this.jdbcUrl, this.jdbcUser, this.jdbcPass)
                .anyMatch(it -> Optional.ofNullable(it).orElse("").isEmpty())) {
            if (this.jdbcProperties == null) {
                throw new AssertionError("need jdbcProperties option", new IllegalArgumentException());
            }
            if (!this.jdbcProperties.exists()) {
                throw new AssertionError(this.jdbcProperties.toString() + " is not exist file", new IllegalArgumentException(this.jdbcProperties.toString()));
            }
        }
    }

    protected void loadJdbcTemplate() throws IOException {
        this.jdbcProp = new Properties();
        if (this.jdbcProperties != null) {
            this.jdbcProp.load(new FileInputStream(this.jdbcProperties));
        }
        if (!Optional.ofNullable(this.jdbcUrl).orElse("").isEmpty()) {
            this.jdbcProp.put("url", this.jdbcUrl);
        }
        if (!Optional.ofNullable(this.jdbcUser).orElse("").isEmpty()) {
            this.jdbcProp.put("user", this.jdbcUser);
        }
        if (!Optional.ofNullable(this.jdbcPass).orElse("").isEmpty()) {
            this.jdbcProp.put("pass", this.jdbcPass);
        }
    }

}
