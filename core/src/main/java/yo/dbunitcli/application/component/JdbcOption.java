package yo.dbunitcli.application.component;

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.stream.Stream;

public class JdbcOption extends PrefixArgumentsParser implements ComparableDataSetParamOption{

    @Option(name = "-jdbcProperties", usage = "use connect database. [url=,user=,pass=]")
    private File jdbcProperties;

    @Option(name = "-jdbcUrl", usage = "use connect database. override jdbcProperties value")
    private String jdbcUrl;

    @Option(name = "-jdbcUser", usage = "use connect database. override jdbcProperties value")
    private String jdbcUser;

    @Option(name = "-jdbcPass", usage = "use connect database. override jdbcProperties value")
    private String jdbcPass;

    @Option(name = "-useJdbcMetaData", usage = "default false. whether load metaData from jdbc or not")
    private String useJdbcMetaData = "false";

    @Option(name = "-op", usage = "import operation UPDATE | INSERT | DELETE | REFRESH | CLEAN_INSERT")
    private String operation;

    private Properties jdbcProp;

    public JdbcOption(String prefix) {
        super(prefix);
    }

    public String getUseJdbcMetaData() {
        return useJdbcMetaData;
    }

    public Properties getJdbcProp() {
        return jdbcProp;
    }

    public String getOperation() {
        return operation;
    }

    public DatabaseConnectionLoader getDatabaseConnectionLoader() {
        return new DatabaseConnectionLoader(this.getJdbcProp());
    }

    public void setUseJdbcMetaData(String useJdbcMetaData) {
        this.useJdbcMetaData = useJdbcMetaData;
    }

    @Override
    public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
        return builder.setUseJdbcMetaData(Boolean.parseBoolean(this.useJdbcMetaData))
                .setDatabaseConnectionLoader(this.getDatabaseConnectionLoader());
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] args) throws CmdLineException {
        try {
            this.loadJdbcTemplate();
        } catch (IOException e) {
            throw new CmdLineException(parser, e.getMessage(), e);
        }
    }

    public void validate(CmdLineParser parser) throws CmdLineException {
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
