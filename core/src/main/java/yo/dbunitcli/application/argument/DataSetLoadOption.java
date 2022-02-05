package yo.dbunitcli.application.argument;

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ColumnSettings;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.FromJsonColumnSettingsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataSetLoadOption extends PrefixArgumentsParser {

    @Option(name = "-src", usage = "export target", required = true)
    private File src;

    @Option(name = "-srcType")
    private DataSourceType srcType = DataSourceType.csv;

    @Option(name = "-setting", usage = "file comparison settings")
    private File setting;

    @Option(name = "-loadData", usage = "if false data row didn't load")
    private String loadData = "true";

    @Option(name = "-includeMetaData", usage = "whether param include tableName and columns or not ")
    private String includeMetaData = "false";

    @Option(name = "-regInclude", usage = "regex to include table")
    private String regInclude;

    @Option(name = "-regExclude", usage = "regex to exclude table")
    private String regExclude;

    private ColumnSettings columnSettings;

    private ComparableDataSetParam.Builder builder;

    public DataSetLoadOption(String prefix) {
        super(prefix);
        this.builder = ComparableDataSetParam.builder();
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] args) throws CmdLineException {
        this.assertFileExists(parser, this.src);
        this.populateSettings(parser);
        this.builder.setSource(this.srcType)
                .setSrc(this.src)
                .setColumnSettings(this.columnSettings)
                .setLoadData(Boolean.parseBoolean(this.loadData))
                .setMapIncludeMetaData(Boolean.parseBoolean(this.includeMetaData))
                .setRegInclude(this.regInclude)
                .setRegExclude(this.regExclude)
        ;
        ComparableDataSetParamOption option = new DataSourceTypeOptionFactory().create(this.getPrefix(), this.srcType);
        option.parseArgument(args);
        option.populate(this.builder);
    }

    @Override
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = super.expandOption(args);
        result.put("-srcType", this.srcType, DataSourceType.class);
        result.putFirOrDir("-src", this.src);
        result.putFile("-setting", this.setting);
        result.put("-loadData", this.loadData);
        result.put("-includeMetaData", this.includeMetaData);
        result.put("-regInclude", this.regInclude);
        result.put("-regExclude", this.regExclude);
        if (Strings.isNullOrEmpty(result.get("-srcType"))) {
            return result;
        }
        try {
            DataSourceType type = DataSourceType.valueOf(result.get("-srcType"));
            ComparableDataSetParamOption option = new DataSourceTypeOptionFactory().create(this.getPrefix(), type);
            result.putAll(option.expandOption(args));
        } catch (Throwable th) {
        }
        return result;
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
