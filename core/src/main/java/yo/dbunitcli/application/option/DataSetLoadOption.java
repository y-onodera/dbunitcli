package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.dataset.TableSeparators;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class DataSetLoadOption implements Option {

    private final String prefix;
    private final DataSourceType srcType;
    private final String setting;
    private final String settingEncoding;
    private final String regTableInclude;
    private final String regTableExclude;
    private final boolean loadData;
    private final boolean includeMetaData;
    private final ComparableDataSetParamOption dataSetParam;
    private final File src;

    public DataSetLoadOption(final String prefix) {
        this(prefix, new DataSetLoadDto());
    }

    public DataSetLoadOption(final String prefix, final DataSetLoadDto dto) {
        this.prefix = prefix;
        if (dto.getSrcType() != null) {
            this.srcType = dto.getSrcType();
        } else {
            this.srcType = DataSourceType.csv;
        }
        if (this.srcType != DataSourceType.none && Strings.isNotEmpty(dto.getSrc())) {
            this.src = new File(dto.getSrc());
        } else {
            this.src = null;
        }
        this.setting = dto.getSetting();
        if (Strings.isNotEmpty(dto.getSettingEncoding())) {
            this.settingEncoding = dto.getSettingEncoding();
        } else {
            this.settingEncoding = System.getProperty("file.encoding");
        }
        this.regTableInclude = dto.getRegTableInclude();
        this.regTableExclude = dto.getRegTableExclude();
        if (Strings.isNotEmpty(dto.getLoadData())) {
            this.loadData = Boolean.parseBoolean(dto.getLoadData());
        } else {
            this.loadData = true;
        }
        if (Strings.isNotEmpty(dto.getIncludeMetaData())) {
            this.includeMetaData = Boolean.parseBoolean(dto.getIncludeMetaData());
        } else {
            this.includeMetaData = false;
        }
        this.dataSetParam = new DataSourceTypeOptionFactory().create(this.getPrefix(), this.srcType, dto);
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-srcType", this.srcType, DataSourceType.class, true);
        if (Optional.ofNullable(result.get("-srcType")).orElse("").isEmpty()) {
            return result;
        }
        result.putFileOrDir("-src", this.src, true);
        result.putFile("-setting", this.setting == null ? null : new File(this.setting));
        result.put("-settingEncoding", this.settingEncoding);
        result.put("-regTableInclude", this.regTableInclude);
        result.put("-regTableExclude", this.regTableExclude);
        result.put("-loadData", this.loadData);
        result.put("-includeMetaData", this.includeMetaData);
        try {
            if (this.dataSetParam == null) {
                final DataSourceType type = DataSourceType.valueOf(result.get("-srcType"));
                final ComparableDataSetParamOption option = new DataSourceTypeOptionFactory()
                        .create(this.getPrefix(), type, new DataSetLoadDto());
                result.putAll(option.toCommandLineArgs());
            } else {
                result.putAll(this.dataSetParam.toCommandLineArgs());
            }
        } catch (final Throwable ignored) {
        }
        return result;
    }

    public ComparableDataSetParam.Builder getParam() {
        if (this.srcType != DataSourceType.none) {
            this.assertFileExists(this.src);
        }
        return this.dataSetParam.populate(ComparableDataSetParam
                .builder()
                .setSource(this.srcType)
                .setSrc(this.src)
                .setTableSeparators(this.getTableSeparators())
                .setRegTableInclude(this.regTableInclude)
                .setRegTableExclude(this.regTableExclude)
                .setLoadData(this.loadData)
                .setMapIncludeMetaData(this.includeMetaData)
        );
    }

    protected void assertFileExists(final File file) {
        if (!file.exists()) {
            throw new AssertionError(file + " is not exist", new IllegalArgumentException(file.toString()));
        }
    }

    protected TableSeparators getTableSeparators() {
        try {
            return new FromJsonTableSeparatorsBuilder(this.settingEncoding).build(this.setting);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    public File getSrc() {
        return this.src;
    }

    public String getSetting() {
        return this.setting;
    }
}
