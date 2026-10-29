package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.OptionValidationException;
import yo.dbunitcli.application.command.Convert;
import yo.dbunitcli.application.command.GenerateType;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.ResultOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.ComparableTableJoin;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.TableSeparators;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.DatasetRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTableConvertRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTableNamesRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewResponseDto;
import yo.dbunitcli.sidecar.job.Interruptible;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Controller("dataset-setting")
public class DatasetSettingsController extends AbstractResourceFileController<DatasetRequestDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetSettingsController.class);
    static final int PREVIEW_ROW_LIMIT = 100;

    public DatasetSettingsController(final Workspace workspace) {
        super(workspace);
    }

    /**
     * データセットのカラムメタデータからdataset-settingテンプレートを生成して返す。
     * generateコマンド（generateType=settings）を一時ディレクトリへ実行し、
     * 出力された単一JSON（load応答と同形）をそのまま返す。保存は行わない。
     */
    @Interruptible
    @Post(uri = "generate", produces = MediaType.APPLICATION_JSON)
    public String generate(@Body final Map<String, String> input) throws IOException {
        if (Strings.isEmpty(input.get("-src.src"))) {
            throw new ApplicationException(new IllegalArgumentException("-src.src is required"));
        }
        final Path tempDir = GenerateTemplateSupport.execToTempDir(
                GenerateType.settings, input, "datasetSettingGenerate");
        try {
            final Path resultFile = tempDir.resolve("result");
            return Files.exists(resultFile) ? Files.readString(resultFile) : "{}";
        } catch (final IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        } finally {
            GenerateTemplateSupport.deleteRecursively(tempDir);
        }
    }

    @Interruptible
    @Post(uri = "table-names", produces = MediaType.APPLICATION_JSON)
    public String tableNames(@Body final DatasetTableNamesRequestDto request) {
        try {
            return ObjectMapper.getDefault().writeValueAsString(
                    new ComparableDataSetLoader(this.buildParameter(request)).loadDataSet(this.toParam(request, "false").build())
                                                                             .getTableNames());
        } catch (final Throwable th) {
            LOGGER.warn("Could not get table names", th);
            return "[]";
        }
    }

    /**
     * 選んだテーブルの先頭{@link #PREVIEW_ROW_LIMIT}行と全行数を返す。対象外のテーブルはなるべく読まないよう、
     * 絞り込みの強い順に試し、対象が見つからなければ絞り込みを緩めて読み直す（{@link Narrow}）。
     */
    @Interruptible
    @Post(uri = "table-preview", produces = MediaType.APPLICATION_JSON)
    public String tablePreview(@Body final DatasetTablePreviewRequestDto request) {
        try {
            final TableSeparators separators = this.toParam(request, "true").getTableSeparators();
            for (final Narrow narrow : Narrow.attempts(Strings.isEmpty(request.getSetting()), separators)) {
                final Optional<DatasetTablePreviewResponseDto> preview = this.loadPreview(request, narrow, separators);
                if (preview.isPresent()) {
                    return ObjectMapper.getDefault().writeValueAsString(preview.get());
                }
            }
            return ObjectMapper.getDefault().writeValueAsString(DatasetTablePreviewResponseDto.empty());
        } catch (final Throwable th) {
            LOGGER.warn("Could not get table preview", th);
            try {
                return ObjectMapper.getDefault().writeValueAsString(DatasetTablePreviewResponseDto.empty());
            } catch (final Exception ex) {
                return "{\"headers\":[],\"rows\":[],\"totalRows\":0}";
            }
        }
    }

    /**
     * 選んだテーブルだけをresultフォームの指定でconvertし、出力先ディレクトリを返す（DB出力なら空文字）。
     * 読込条件はプレビューと同じ。出力を読み直せないため、絞り込みは対象を取りこぼさない範囲に留め（{@link Narrow#singlePass}）、
     * 出力する側で対象テーブル以外を捨てる（{@link TargetTableConverter}）
     */
    @Interruptible
    @Post(uri = "table-convert", produces = MediaType.TEXT_PLAIN)
    public String tableConvert(@Body final DatasetTableConvertRequestDto request) {
        try {
            final String tableName = request.getTableName();
            if (Strings.isEmpty(tableName)) {
                throw new IllegalArgumentException("tableName is required");
            }
            final ResultOption result = new Convert().parseOption(tableName,
                    AbstractCommandController.resolveSidecarFilePaths(new CommandParameters(Type.convert,
                            Optional.ofNullable(request.getResult()).orElse(Map.of()))).args(),
                    Parameter.none()).result();
            final List<Option.Violation> violations = result.convertResult().validate();
            if (!violations.isEmpty()) {
                throw new OptionValidationException(violations);
            }
            final ComparableDataSetParam.Builder param = this.toParam(request, "true");
            final TableSeparators separators = param.getTableSeparators();
            Narrow.singlePass(Strings.isEmpty(request.getSetting()), DataSourceType.valueOf(request.getSrcType()), separators)
                  .applyTo(param, separators, tableName);
            FileResources.runInContext(() -> {
                try (final IDataSetConverter converter = new TargetTableConverter(tableName,
                        result.converter(it -> it.setResultPath(result.getResultPath())))) {
                    new ComparableDataSetLoader(this.buildParameter(request)).loadDataSet(param.setConverter(converter).build());
                }
            });
            return result.resultDir();
        } catch (final OptionValidationException ex) {
            throw ex;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    /**
     * 対象テーブルが読み込まれなければemptyを返す
     */
    private Optional<DatasetTablePreviewResponseDto> loadPreview(final DatasetTablePreviewRequestDto request,
                                                                 final Narrow narrow,
                                                                 final TableSeparators separators) throws Exception {
        final String tableName = request.getTableName();
        final ComparableDataSetParam.Builder param = this.toParam(request, "true");
        narrow.applyTo(param, separators, tableName);
        final PreviewCollector collector = new PreviewCollector();
        try (final ComparableDataSetProducer producer = new ComparableDataSetLoader(this.buildParameter(request))
                .getComparableDataSetProducer(param.setConverter(new TargetTableConverter(tableName, collector)).build())) {
            producer.produce();
        }
        return collector.toResponse();
    }

    /**
     * プレビューでの読込の絞り込み方
     */
    private enum Narrow {
        /**
         * 元テーブル名でも絞る（対象外のソースを開かない）。設定未適用時のみ。xlsxSchema等で名前が変わると見つからない
         */
        SOURCE_AND_OUTPUT,
        /**
         * 出力テーブル名をoutputFilterで絞る（対象外のテーブルは行を読まない）。分割（split）後の名前では見つからない
         */
        OUTPUT,
        /**
         * 絞らずに読む
         */
        NONE;

        /**
         * joinは元テーブルも出力として残す必要があるため、outputFilterで絞らない。
         * 絞らずに読み直すのは、outputFilterで見つからない名前（split後）がありうる場合だけ
         */
        static List<Narrow> attempts(final boolean settingEmpty, final TableSeparators separators) {
            if (!separators.joinConditions().isEmpty()) {
                return List.of(NONE);
            }
            final List<Narrow> result = new ArrayList<>();
            if (settingEmpty) {
                result.add(SOURCE_AND_OUTPUT);
            }
            result.add(OUTPUT);
            if (separators.hasSplitter()) {
                result.add(NONE);
            }
            return result;
        }

        /**
         * 元テーブル名がそのまま出力テーブル名になる形式（xlsx・xlsのxlsxSchemaなどで名前が変わりうるものは含めない）
         */
        private static final Set<DataSourceType> SOURCE_NAME_KEPT = EnumSet.of(DataSourceType.table, DataSourceType.sql, DataSourceType.csv);

        /**
         * 読み直さずに済む（対象を取りこぼさない）最も強い絞り込み。元テーブル名で絞るのは、設定未適用で名前が変わらない形式だけ
         */
        static Narrow singlePass(final boolean settingEmpty, final DataSourceType srcType, final TableSeparators separators) {
            if (!separators.joinConditions().isEmpty() || separators.hasSplitter()) {
                return NONE;
            }
            return settingEmpty && SOURCE_NAME_KEPT.contains(srcType) ? SOURCE_AND_OUTPUT : OUTPUT;
        }

        void applyTo(final ComparableDataSetParam.Builder param, final TableSeparators separators, final String tableName) {
            if (this == SOURCE_AND_OUTPUT) {
                param.setRegTableInclude("(?i)^" + Pattern.quote(tableName) + "$");
            }
            if (this != NONE) {
                param.setTableSeparators(separators.builder()
                                                   .addFilter(tableName::equals, TableSeparators.FilterCombine.AND)
                                                   .build());
            }
        }
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().datasetSetting();
    }

    private Parameter buildParameter(final DatasetTableNamesRequestDto request) {
        final Map<String, String> params = request.getParameters();
        if (params == null) {
            return Parameter.none();
        }
        return Parameter.none().addAll(params);
    }

    private ComparableDataSetParam.Builder toParam(final DatasetTableNamesRequestDto request, final String loadData) {
        final DataSetLoadDto dto = new DataSetLoadDto();
        dto.setSrc(request.getSrc());
        dto.setSrcType(DataSourceType.valueOf(request.getSrcType()));
        dto.setRegTableInclude(request.getRegTableInclude());
        dto.setRegTableExclude(request.getRegTableExclude());
        dto.setRecursive(String.valueOf(request.isRecursive()));
        dto.setRegInclude(request.getRegInclude());
        dto.setRegExclude(request.getRegExclude());
        dto.setExtension(request.getExtension());
        dto.setLoadData(loadData);
        dto.setXlsxSchemaSource(request.getXlsxSchema());
        dto.setFixedLength(request.getFixedLength());
        dto.setRegHeaderSplit(request.getRegHeaderSplit());
        dto.setRegDataSplit(request.getRegDataSplit());
        dto.setEncoding(request.getEncoding());
        dto.setDelimiter(request.getDelimiter());
        dto.setIgnoreQuoted(request.isIgnoreQuoted());
        dto.setHeaderName(request.getHeaderName());
        dto.setStartRow(request.getStartRow());
        dto.setAddFileInfo(request.isAddFileInfo());
        dto.getJdbc().setJdbcUrl(request.getJdbcUrl());
        dto.getJdbc().setJdbcUser(request.getJdbcUser());
        dto.getJdbc().setJdbcPass(request.getJdbcPass());
        dto.getJdbc().setJdbcProperties(request.getJdbcProperties());
        dto.setSetting(request.getSetting());
        dto.setSettingEncoding(request.getSettingEncoding());
        return new DataSetLoadOption("", dto).getParam();
    }

    /**
     * 対象テーブルのイベントだけを出力先へ渡す（プレビュー・convertで共通）
     */
    private static class TargetTableConverter implements IDataSetConverter {
        private final String tableName;
        private final IDataSetConverter delegate;
        private boolean processTarget;

        TargetTableConverter(final String tableName, final IDataSetConverter delegate) {
            this.tableName = tableName;
            this.delegate = delegate;
        }

        @Override
        public boolean isExportEmptyTable() {
            return this.delegate.isExportEmptyTable();
        }

        @Override
        public void startDataSet() {
            this.delegate.startDataSet();
        }

        @Override
        public void endDataSet() {
            this.delegate.endDataSet();
        }

        @Override
        public void close() {
            this.delegate.close();
        }

        @Override
        public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
            if (this.target(tableMetaData)) {
                this.delegate.reStartTable(tableMetaData, writeRows);
            }
        }

        @Override
        public IDataSetConverter split() {
            return new TargetTableConverter(this.tableName, this.delegate.split());
        }

        @Override
        public boolean isSplittable() {
            return this.delegate.isSplittable();
        }

        @Override
        public File getDir() {
            return this.delegate.getDir();
        }

        @Override
        public boolean isEnableRowProcessing(final AddSettingTableMetaData metaData, final List<ComparableTableJoin> joins) {
            return this.delegate.isEnableRowProcessing(metaData, joins);
        }

        @Override
        public void startTable(final ITableMetaData metaData) throws DataSetException {
            if (this.target(metaData)) {
                this.delegate.startTable(metaData);
            }
        }

        @Override
        public void endTable() throws DataSetException {
            if (this.processTarget) {
                this.delegate.endTable();
            }
        }

        @Override
        public void row(final Object[] values) throws DataSetException {
            if (this.processTarget) {
                this.delegate.row(values);
            }
        }

        private boolean target(final ITableMetaData metaData) {
            this.processTarget = metaData.getTableName().equals(this.tableName);
            return this.processTarget;
        }
    }

    /**
     * 渡されたテーブル（{@link TargetTableConverter}で対象だけに絞られる）の先頭{@link #PREVIEW_ROW_LIMIT}行と全行数を集める
     */
    private static class PreviewCollector implements IDataSetConverter {
        private ComparableTable.Builder builder;
        private int totalRows;

        Optional<DatasetTablePreviewResponseDto> toResponse() {
            if (this.builder == null) {
                return Optional.empty();
            }
            final ComparableTable table = this.builder.build();
            final String[] headers = Arrays.stream(table.getTableMetaData().getColumns())
                                           .map(Column::getColumnName)
                                           .toArray(String[]::new);
            return Optional.of(new DatasetTablePreviewResponseDto(headers, table.rows().rows(), this.totalRows));
        }

        @Override
        public boolean isExportEmptyTable() {
            return true;
        }

        @Override
        public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
            this.startTable(tableMetaData);
        }

        @Override
        public IDataSetConverter split() {
            return this;
        }

        @Override
        public void startTable(final ITableMetaData metaData) {
            if (this.builder == null) {
                this.builder = new ComparableTable.Builder(metaData);
            }
        }

        @Override
        public void endTable() {
            // nothing
        }

        @Override
        public void row(final Object[] values) {
            if (this.totalRows < PREVIEW_ROW_LIMIT) {
                this.builder.addRow(values);
            }
            this.totalRows++;
        }
    }
}
