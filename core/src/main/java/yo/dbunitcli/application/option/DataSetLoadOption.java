package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.dataset.TableSeparators;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class DataSetLoadOption implements OptionParser<DataSetLoadDto> {

    private final String prefix;
    private final ComparableDataSetParam.Builder builder;
    private File src;
    private DataSourceType srcType = DataSourceType.csv;
    private String setting;
    private String settingEncoding = System.getProperty("file.encoding");
    private String loadData = "true";
    private String includeMetaData = "false";
    private String regInclude;
    private String regExclude;
    private TableSeparators tableSeparators;

    public DataSetLoadOption(final String prefix) {
        this.prefix = prefix;
        this.builder = ComparableDataSetParam.builder();
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-srcType", this.srcType, DataSourceType.class, true);
        if (Optional.ofNullable(result.get("-srcType")).orElse("").isEmpty()) {
            return result;
        }
        result.putFileOrDir("-src", this.src, true);
        result.putFile("-setting", this.setting == null ? null : new File(this.setting));
        result.put("-settingEncoding", this.settingEncoding);
        result.put("-loadData", this.loadData);
        result.put("-includeMetaData", this.includeMetaData);
        result.put("-regInclude", this.regInclude);
        result.put("-regExclude", this.regExclude);
        try {
            final DataSourceType type = DataSourceType.valueOf(result.get("-srcType"));
            final ComparableDataSetParamOption option = new DataSourceTypeOptionFactory().create(this.getPrefix(), type);
            result.putAll(option.createOptionParam(args));
        } catch (final Throwable ignored) {
        }
        return result;
    }

    @Override
    public void setUpComponent(final DataSetLoadDto dto) {
        this.src = new File(dto.getSrc());
        if (dto.getSrcType() != null) {
            this.srcType = dto.getSrcType();
        }
        this.setting = dto.getSetting();
        if (Strings.isNotEmpty(dto.getSettingEncoding())) {
            this.settingEncoding = dto.getSettingEncoding();
        }
        if (Strings.isNotEmpty(dto.getLoadData())) {
            this.loadData = dto.getLoadData();
        }
        if (Strings.isNotEmpty(dto.getIncludeMetaData())) {
            this.includeMetaData = dto.getIncludeMetaData();
        }
        if (Strings.isNotEmpty(dto.getRegExclude())) {
            this.regExclude = dto.getRegExclude();
        }
        if (Strings.isNotEmpty(dto.getRegInclude())) {
            this.regInclude = dto.getRegInclude();
        }
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
        option.setUpComponent(dto);
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
