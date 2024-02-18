package yo.dbunitcli.application.argument;

import picocli.CommandLine;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class JdbcOption extends DefaultArgumentsParser {

    @CommandLine.Option(names = "-jdbcProperties", description = "use connect database. [url=,user=,pass=]")
    private File jdbcProperties;

    @CommandLine.Option(names = "-jdbcUrl", description = "use connect database. override jdbcProperties value")
    private String jdbcUrl;

    @CommandLine.Option(names = "-jdbcUser", description = "use connect database. override jdbcProperties value")
    private String jdbcUser;

    @CommandLine.Option(names = "-jdbcPass", description = "use connect database. override jdbcProperties value")
    private String jdbcPass;

    private Properties jdbcProp;

    public JdbcOption(final String prefix) {
        super(prefix);
    }

    public Properties getJdbcProp() {
        return this.jdbcProp;
    }

    public DatabaseConnectionLoader getDatabaseConnectionLoader() {
        return new DatabaseConnectionLoader(this.getJdbcProp());
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
    public void setUpComponent(final CommandLine.ParseResult parser, final String[] args) {
        try {
            this.loadJdbcTemplate();
        } catch (final IOException e) {
            throw new AssertionError(e.getMessage(), e);
        }
        this.validate();
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
