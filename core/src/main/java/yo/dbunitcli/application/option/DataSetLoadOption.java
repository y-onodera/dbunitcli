package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.TableSeparators;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public record DataSetLoadOption(
        String prefix
        , boolean enableSrcTypeNone
        , DataSourceType srcType
        , String src
        , String setting
        , String settingEncoding
        , String settingTargetOnly
        , String regTableInclude
        , String regTableExclude
        , String loadData
        , String includeMetaData
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
                , Strings.isNotEmpty(dto.getSettingTargetOnly()) ? dto.getSettingTargetOnly() : "false"
                , dto.getRegTableInclude()
                , dto.getRegTableExclude()
                , Strings.isNotEmpty(dto.getLoadData()) ? dto.getLoadData() : "true"
                , Strings.isNotEmpty(dto.getIncludeMetaData()) ? dto.getIncludeMetaData() : "false"
                , new DataSourceTypeOptionFactory().create(prefix, DataSetLoadOption.getSrcType(dto), dto)
        );
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        final ParametersBuilder result = new ParametersBuilder(this.getPrefix());
        result.put("-srcType", this.srcType, DataSourceType.class
                , this.enableSrcTypeNone ? Filter.any() : Filter.exclude(DataSourceType.none), false);
        if (this.srcType == null || this.srcType == DataSourceType.none) {
            return result;
        }
        return result.putFileOrDir("-src", this.src, true, this.srcBaseDir())
                .putAll(this.dataSetParam.toParameters())
                .putFile("-setting", this.setting, BaseDir.SETTING)
                .put("-settingEncoding", this.settingEncoding)
                .putFlag("-settingTargetOnly", this.settingTargetOnly)
                .put("-regTableInclude", this.regTableInclude)
                .put("-regTableExclude", this.regTableExclude)
                .putFlag("-loadData", this.loadData)
                .putFlag("-includeMetaData", this.includeMetaData);
    }

    /**
     * srcType=noneは読み込まないため検証しない。-srcは未指定ならカレントディレクトリを読むため必須にしない
     */
    @Override
    public List<Violation> validate() {
        if (this.srcType == null || this.srcType == DataSourceType.none) {
            return List.of();
        }
        return Option.concat(this.validateFileExists("-src", this.src, this.srcBaseDir())
                , this.validateFilesExist("-setting", this.setting, BaseDir.SETTING)
                , this.validateEncoding("-settingEncoding", this.settingEncoding)
                , this.validateBoolean("-settingTargetOnly", this.settingTargetOnly)
                , this.validatePattern("-regTableInclude", this.regTableInclude)
                , this.validatePattern("-regTableExclude", this.regTableExclude)
                , this.validateBoolean("-loadData", this.loadData)
                , this.validateBoolean("-includeMetaData", this.includeMetaData)
                , this.dataSetParam.validate());
    }

    public ComparableDataSetParam.Builder getParam() {
        final File srcFile = Strings.isNotEmpty(this.src) ? this.srcBaseDir().search(this.src) : new File(".");
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
                .setLoadData(Boolean.parseBoolean(this.loadData))
                .setMapIncludeMetaData(Boolean.parseBoolean(this.includeMetaData))
        );
    }

    // sql/table/csvqの-srcはworkspaceのresources配下に置くリソースファイル。それ以外はデータセット
    private BaseDir srcBaseDir() {
        return switch (this.srcType) {
            case sql -> BaseDir.SQL;
            case table -> BaseDir.TABLE;
            case csvq -> BaseDir.CSVQ;
            case null, default -> BaseDir.DATASET;
        };
    }

    public void assertFileExists(final File file) {
        if (!file.exists()) {
            throw new AssertionError(file + " is not exist", new IllegalArgumentException(file.toString()));
        }
    }

    /**
     * -settingTargetOnly=trueなら設定の対象のテーブルだけを読み込む（設定が無ければ何も読み込まない）
     */
    public TableSeparators getTableSeparators() {
        try {
            final TableSeparators result = new FromJsonTableSeparatorsBuilder(this.settingEncoding).build(this.setting);
            return this.isSettingTargetOnly() ? result.onlySettingTargets() : result;
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    public boolean isSettingTargetOnly() {
        return Boolean.parseBoolean(this.settingTargetOnly);
    }

    public String getSetting() {
        return this.setting;
    }
}
