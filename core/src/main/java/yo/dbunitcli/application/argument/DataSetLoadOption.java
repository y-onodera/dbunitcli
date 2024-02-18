package yo.dbunitcli.application.argument;

import picocli.CommandLine;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.dataset.TableSeparators;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class DataSetLoadOption extends DefaultArgumentsParser {

    private final ComparableDataSetParam.Builder builder;
    @CommandLine.Option(names = "-src", description = "resource to load", required = true)
    private File src;
    @CommandLine.Option(names = "-srcType")
    private DataSourceType srcType = DataSourceType.csv;
    @CommandLine.Option(names = "-setting", description = "file comparison settings")
    private String setting;
    @CommandLine.Option(names = "-settingEncoding", description = "settings encoding")
    private String settingEncoding = System.getProperty("file.encoding");
    @CommandLine.Option(names = "-loadData", description = "if false data row didn't load")
    private String loadData = "true";
    @CommandLine.Option(names = "-includeMetaData", description = "whether param include tableName and columns or not ")
    private String includeMetaData = "false";
    @CommandLine.Option(names = "-regInclude", description = "regex to include table")
    private String regInclude;
    @CommandLine.Option(names = "-regExclude", description = "regex to exclude table")
    private String regExclude;
    private TableSeparators tableSeparators;

    public DataSetLoadOption(final String prefix) {
        super(prefix);
        this.builder = ComparableDataSetParam.builder();
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-srcType", this.srcType, DataSourceType.class, true);
        result.putFileOrDir("-src", this.src, true);
        result.putFile("-setting", this.setting == null ? null : new File(this.setting));
        result.put("-settingEncoding", this.settingEncoding);
        result.put("-loadData", this.loadData);
        result.put("-includeMetaData", this.includeMetaData);
        result.put("-regInclude", this.regInclude);
        result.put("-regExclude", this.regExclude);
        if (Optional.ofNullable(result.get("-srcType")).orElse("").isEmpty()) {
            return result;
        }
        try {
            final DataSourceType type = DataSourceType.valueOf(result.get("-srcType"));
            final ComparableDataSetParamOption option = new DataSourceTypeOptionFactory().create(this.getPrefix(), type);
            result.putAll(option.createOptionParam(args));
        } catch (final Throwable ignored) {
        }
        return result;
    }

    @Override
    public void setUpComponent(final CommandLine.ParseResult parser, final String[] args) {
        this.assertFileExists(this.src);
        this.populateSettings();
        this.builder.setSource(this.srcType)
                .setSrc(this.src)
                .setTableSeparators(this.tableSeparators)
                .setLoadData(Boolean.parseBoolean(this.loadData))
                .setMapIncludeMetaData(Boolean.parseBoolean(this.includeMetaData))
                .setRegInclude(this.regInclude)
                .setRegExclude(this.regExclude)
        ;
        final ComparableDataSetParamOption option = new DataSourceTypeOptionFactory().create(this.getPrefix(), this.srcType);
        option.parseArgument(args);
        option.populate(this.builder);
    }

    public ComparableDataSetParam.Builder getParam() {
        return this.builder;
    }

    protected void assertFileExists(final File file) {
        if (!file.exists()) {
            throw new AssertionError(file + " is not exist", new IllegalArgumentException(file.toString()));
        }
    }

    protected void populateSettings() {
        try {
            this.tableSeparators = new FromJsonTableSeparatorsBuilder(this.settingEncoding).build(this.setting);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }
}
