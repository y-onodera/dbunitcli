package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.TableSeparators;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public record DataSetLoadOption(
        String prefix
        , boolean enableSrcTypeNone
        , DataSourceType srcType
        , String src
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
        this(prefix, dto, false);
    }

    public DataSetLoadOption(final String prefix, final DataSetLoadDto dto, final boolean enableSrcTypeNone) {
        this(prefix
                , enableSrcTypeNone
                , DataSetLoadOption.getSrcType(dto)
                , DataSetLoadOption.getSrcType(dto) != DataSourceType.none && Strings.isNotEmpty(dto.getSrc())
                        ? dto.getSrc()
                        : null
                , dto.getSetting()
                , Strings.isNotEmpty(dto.getSettingEncoding())
                        ? dto.getSettingEncoding()
                        : Charset.defaultCharset().displayName()
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
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        final CommandLineArgsBuilder result = new CommandLineArgsBuilder(this.getPrefix());
        result.put("-srcType", this.srcType, DataSourceType.class
                , this.enableSrcTypeNone ? Filter.any() : Filter.exclude(DataSourceType.none), false);
        if (this.srcType == null || this.srcType == DataSourceType.none) {
            return result;
        }
        return result.putFileOrDir("-src", this.src, true, BaseDir.DATASET)
                .putAll(this.dataSetParam.toCommandLineArgs())
                .putFile("-setting", this.setting, BaseDir.SETTING)
                .put("-settingEncoding", this.settingEncoding)
                .put("-regTableInclude", this.regTableInclude)
                .put("-regTableExclude", this.regTableExclude)
                .put("-loadData", this.loadData)
                .put("-includeMetaData", this.includeMetaData);
    }

    public ComparableDataSetParam.Builder getParam() {
        final File srcFile = Strings.isNotEmpty(this.src) ? FileResources.searchDatasetBase(this.src) : new File(".");
        if (this.srcType != DataSourceType.none) {
            this.assertFileExists(srcFile);
        }
        return this.dataSetParam.populate(ComparableDataSetParam
                .builder()
                .setSource(this.srcType)
                .setSrc(srcFile)
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

    public String getSetting() {
        return this.setting;
    }
}
