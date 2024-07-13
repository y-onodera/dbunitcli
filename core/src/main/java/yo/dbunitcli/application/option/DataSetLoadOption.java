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

public record DataSetLoadOption(
        String prefix
        , DataSourceType srcType
        , File src
        , String setting
        , String settingEncoding
        , String regTableInclude
        , String regTableExclude
        , boolean loadData
        , boolean includeMetaData
        , ComparableDataSetParamOption dataSetParam
) implements Option {


    private static DataSourceType getSrcType(final DataSetLoadDto dto) {
        return dto.getSrcType() != null ? dto.getSrcType() : DataSourceType.csv;
    }

    public DataSetLoadOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix
                , DataSetLoadOption.getSrcType(dto)
                , DataSetLoadOption.getSrcType(dto) != DataSourceType.none && Strings.isNotEmpty(dto.getSrc())
                        ? new File(dto.getSrc())
                        : null
                , dto.getSetting()
                , Strings.isNotEmpty(dto.getSettingEncoding())
                        ? dto.getSettingEncoding()
                        : System.getProperty("file.encoding")
                , dto.getRegTableInclude()
                , dto.getRegTableExclude()
                , !Strings.isNotEmpty(dto.getLoadData()) || Boolean.parseBoolean(dto.getLoadData())
                , Strings.isNotEmpty(dto.getIncludeMetaData()) && Boolean.parseBoolean(dto.getIncludeMetaData())
                , new DataSourceTypeOptionFactory().create(prefix, DataSetLoadOption.getSrcType(dto), dto)
        );
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-srcType", this.srcType
                , DataSourceType.class, Filter.exclude(DataSourceType.none), true);
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

    public void assertFileExists(final File file) {
        if (!file.exists()) {
            throw new AssertionError(file + " is not exist", new IllegalArgumentException(file.toString()));
        }
    }

    public TableSeparators getTableSeparators() {
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
