package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class JdbcOption extends DefaultArgumentsParser {

    @Option(name = "-jdbcProperties", usage = "use connect database. [url=,user=,pass=]")
    private File jdbcProperties;

    @Option(name = "-jdbcUrl", usage = "use connect database. override jdbcProperties value")
    private String jdbcUrl;

    @Option(name = "-jdbcUser", usage = "use connect database. override jdbcProperties value")
    private String jdbcUser;

    @Option(name = "-jdbcPass", usage = "use connect database. override jdbcProperties value")
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
    public void setUpComponent(final CmdLineParser parser, final String[] args) throws CmdLineException {
        try {
            this.loadJdbcTemplate();
        } catch (final IOException e) {
            throw new CmdLineException(parser, e.getMessage(), e);
        }
        this.validate(parser);
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

    protected void validate(final CmdLineParser parser) throws CmdLineException {
        if (Stream.of(this.jdbcUrl, this.jdbcUser, this.jdbcPass)
                .anyMatch(it -> Optional.ofNullable(it).orElse("").isEmpty())) {
            if (this.jdbcProperties == null) {
                throw new CmdLineException(parser, "need jdbcProperties option", new IllegalArgumentException());
            }
            if (!this.jdbcProperties.exists()) {
                throw new CmdLineException(parser, this.jdbcProperties.toString() + " is not exist file", new IllegalArgumentException(this.jdbcProperties.toString()));
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
