package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ColumnSettings;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.FromJsonColumnSettingsBuilder;

import java.io.File;
import java.io.IOException;

public class DataSetLoadOption extends PrefixArgumentsParser {

    @Option(name = "-src", usage = "export target", required = true)
    private File src;

    @Option(name = "-srcType", usage = "table | sql | csv | csvq | xls | xlsx | fixed | reg | file | dir")
    private String srcType = "csv";

    @Option(name = "-setting", usage = "file comparison settings")
    private File setting;

    @Option(name = "-loadData", usage = "default true. if false data row didn't load")
    private String loadData = "true";

    @Option(name = "-includeMetaData", usage = "whether param include tableName and columns or not ")
    private String includeMetaData = "false";

    @Option(name = "-encoding", usage = "csv file encoding")
    private String encoding = System.getProperty("file.encoding");

    @Option(name = "-fixedLength", usage = "comma separate column Lengths")
    private String fixedLength;

    @Option(name = "-regInclude", usage = "regex to include table")
    private String regInclude;

    @Option(name = "-regExclude", usage = "regex to exclude table")
    private String regExclude;

    private final TemplateRenderOption templateOption;

    private final HeaderNameOption headerNameOption;

    private final JdbcOption jdbcOption;

    private ColumnSettings columnSettings;

    private ComparableDataSetParam.Builder builder;

    public DataSetLoadOption(String prefix) {
        super(prefix);
        this.templateOption = new TemplateRenderOption(prefix);
        this.headerNameOption = new HeaderNameOption(prefix);
        this.jdbcOption = new JdbcOption(prefix);
        this.builder = ComparableDataSetParam.builder();
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] args) throws CmdLineException {
        this.assertFileExists(parser, this.src);
        this.populateSettings(parser);
        DataSourceType type = DataSourceType.fromString(this.srcType);
        this.builder.setSource(type)
                .setSrc(this.src)
                .setEncoding(this.encoding)
                .setColumnSettings(this.columnSettings)
                .setLoadData(Boolean.parseBoolean(this.loadData))
                .setMapIncludeMetaData(Boolean.parseBoolean(this.includeMetaData))
                .setRegInclude(this.regInclude)
                .setRegExclude(this.regExclude)
        ;
        if (type.isNoHeaderLoadable()) {
            this.headerNameOption.parseArgument(args);
            this.headerNameOption.populate(this.builder);
        }
        if (type.isUseQuery()) {
            this.templateOption.parseArgument(args);
            this.templateOption.populate(this.builder);
        }
        if (type.isUseDatabase()) {
            this.jdbcOption.parseArgument(args);
            this.jdbcOption.populate(this.builder);
        }
        ComparableDataSetParamOption option = new DataSourceTypeOptionFactory().create(this.getPrefix(), type);
        option.parseArgument(args);
        option.populate(this.builder);
    }

    public ComparableDataSetParam.Builder getParam() {
        return this.builder;
    }

    protected void assertFileExists(CmdLineParser parser, File file) throws CmdLineException {
        if (!file.exists()) {
            throw new CmdLineException(parser, file + " is not exist", new IllegalArgumentException(file.toString()));
        }
    }

    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        try {
            this.columnSettings = new FromJsonColumnSettingsBuilder().build(this.setting);
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }
}
