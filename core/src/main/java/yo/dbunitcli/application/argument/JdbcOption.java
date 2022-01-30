package yo.dbunitcli.application.argument;

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class JdbcOption extends PrefixArgumentsParser {

    @Option(name = "-jdbcProperties", usage = "use connect database. [url=,user=,pass=]")
    private File jdbcProperties;

    @Option(name = "-jdbcUrl", usage = "use connect database. override jdbcProperties value")
    private String jdbcUrl;

    @Option(name = "-jdbcUser", usage = "use connect database. override jdbcProperties value")
    private String jdbcUser;

    @Option(name = "-jdbcPass", usage = "use connect database. override jdbcProperties value")
    private String jdbcPass;

    private Properties jdbcProp;

    public JdbcOption(String prefix) {
        super(prefix);
    }

    public Properties getJdbcProp() {
        return jdbcProp;
    }

    public DatabaseConnectionLoader getDatabaseConnectionLoader() {
        return new DatabaseConnectionLoader(this.getJdbcProp());
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] args) throws CmdLineException {
        try {
            this.loadJdbcTemplate();
        } catch (IOException e) {
            throw new CmdLineException(parser, e.getMessage(), e);
        }
        this.validate(parser);
    }

    @Override
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = super.expandOption(args);
        result.put("-jdbcProperties", this.jdbcProperties);
        if (Strings.isNullOrEmpty(result.get("-jdbcProperties"))) {
            result.put("-jdbcUrl", this.jdbcUrl);
            result.put("-jdbcUser", this.jdbcUser);
            result.put("-jdbcPass", this.jdbcPass);
        }
        return result;
    }

    protected void validate(CmdLineParser parser) throws CmdLineException {
        if (Stream.of(this.jdbcUrl, this.jdbcUser, this.jdbcPass)
                .anyMatch(Strings::isNullOrEmpty)) {
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
        if (!Strings.isNullOrEmpty(this.jdbcUrl)) {
            this.jdbcProp.put("url", this.jdbcUrl);
        }
        if (!Strings.isNullOrEmpty(this.jdbcUser)) {
            this.jdbcProp.put("user", this.jdbcUser);
        }
        if (!Strings.isNullOrEmpty(this.jdbcPass)) {
            this.jdbcProp.put("pass", this.jdbcPass);
        }
    }

}
