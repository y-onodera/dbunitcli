package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonReader;
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
import yo.dbunitcli.application.command.ConvertOption;
import yo.dbunitcli.application.command.GenerateType;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.application.json.FromJsonXlsxSchemaBuilder;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.ResultOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.ComparableTableJoin;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.TableSeparators;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.poi.XlsxSchema;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.DatasetRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetSettingDto;
import yo.dbunitcli.sidecar.dto.DatasetTableConvertRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTableNamesRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewResponseDto;
import yo.dbunitcli.sidecar.dto.JsonXlsxSchemaDto;
import yo.dbunitcli.sidecar.job.Interruptible;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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

    /**
     * 読み込まれるテーブル名を返す。データ行は分割（split）後の名前を求める場合だけ読み、
     * その場合も行は保持せずテーブル名だけを集める（{@link TableNameCollector}）
     */
    @Interruptible
    @Post(uri = "table-names", produces = MediaType.APPLICATION_JSON)
    public String tableNames(@Body final DatasetTableNamesRequestDto request) {
        try {
            final ComparableDataSetParam.Builder param = this.toParam(request, "false").get();
            final ComparableDataSetLoader loader = new ComparableDataSetLoader(this.buildParameter(request));
            if (!param.getTableSeparators().isTableNameDependsOnRows()) {
                return ObjectMapper.getDefault().writeValueAsString(loader.loadDataSet(param.build()).getTableNames());
            }
            final TableNameCollector collector = new TableNameCollector();
            try (final ComparableDataSetProducer producer = loader.getComparableDataSetProducer(
                    param.setLoadData(true).setConverter(collector).build())) {
                producer.produce();
            }
            return ObjectMapper.getDefault().writeValueAsString(collector.tableNames);
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
            final Supplier<ComparableDataSetParam.Builder> params = this.toParam(request, "true");
            final TableSeparators separators = params.get().getTableSeparators();
            for (final Narrow narrow : Narrow.attempts(separators)) {
                final Optional<DatasetTablePreviewResponseDto> preview = this.loadPreview(request, params.get(), narrow, separators);
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
            final ComparableDataSetParam.Builder param = this.toParam(request, "true").get();
            final TableSeparators separators = param.getTableSeparators();
            Narrow.singlePass(separators).applyTo(param, separators, tableName);
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
                                                                 final ComparableDataSetParam.Builder param,
                                                                 final Narrow narrow,
                                                                 final TableSeparators separators) throws Exception {
        final String tableName = request.getTableName();
        narrow.applyTo(param, separators, tableName);
        final PreviewCollector collector = new PreviewCollector();
        try (final ComparableDataSetProducer producer = new ComparableDataSetLoader(this.buildParameter(request))
                .getComparableDataSetProducer(param.setConverter(new TargetTableConverter(tableName, collector)).build())) {
            producer.produce();
        }
        return collector.toResponse();
    }

    /**
     * プレビュー・convertでの読込の絞り込み方。出力テーブル名で絞ると、読込前に出力テーブル名が決まるソースのうち
     * 対象外のものは開かない（ComparableDataSetProducer#produce）。それ以外も対象外のテーブルは行を読まない
     */
    private enum Narrow {
        /**
         * 出力テーブル名をoutputFilterで絞る。分割（split）後の名前では見つからない
         */
        OUTPUT,
        /**
         * 絞らずに読む
         */
        NONE;

        /**
         * 出力テーブル名が読込前に決まれば（join・splitが無い）絞って読むだけでよい。
         * joinは元テーブルも出力として残す必要があるため絞らない。splitはoutputFilterで見つからなければ絞らずに読み直す
         */
        static List<Narrow> attempts(final TableSeparators separators) {
            if (separators.isIndependentTables()) {
                return List.of(OUTPUT);
            }
            return separators.joinConditions().isEmpty() ? List.of(OUTPUT, NONE) : List.of(NONE);
        }

        /**
         * 読み直さずに済む（対象を取りこぼさない）最も強い絞り込み
         */
        static Narrow singlePass(final TableSeparators separators) {
            return separators.isIndependentTables() ? OUTPUT : NONE;
        }

        void applyTo(final ComparableDataSetParam.Builder param, final TableSeparators separators, final String tableName) {
            if (this == OUTPUT) {
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

    /**
     * フォームの解釈とinline設定の読込はリクエストごとに1回だけ行い、読み直すたびに新しい読込パラメータを作る
     */
    private Supplier<ComparableDataSetParam.Builder> toParam(final DatasetTableNamesRequestDto request, final String loadData) throws IOException {
        final DataSetLoadDto dto = this.toLoadDto(request.getSrcOptions());
        dto.setLoadData(loadData);
        final UnaryOperator<ComparableDataSetParam.Builder> inlineXlsxSchema = this.inlineXlsxSchema(dto, request.getInlineXlsxSchema());
        final UnaryOperator<ComparableDataSetParam.Builder> inlineSetting = this.inlineSetting(dto, request.getInlineSetting());
        return () -> inlineSetting.apply(inlineXlsxSchema.apply(new DataSetLoadOption("", dto).getParam()));
    }

    /**
     * inlineの設定があれば、settingのファイルの代わりに使う
     */
    private UnaryOperator<ComparableDataSetParam.Builder> inlineSetting(final DataSetLoadDto dto, final DatasetSettingDto setting) throws IOException {
        if (setting == null) {
            return UnaryOperator.identity();
        }
        dto.setSetting("");
        final TableSeparators inline = this.inlineTableSeparators(setting);
        return param -> param.setTableSeparators(inline);
    }

    /**
     * inlineのxlsxスキーマがあれば、xlsxSchemaのファイルの代わりに使う
     */
    private UnaryOperator<ComparableDataSetParam.Builder> inlineXlsxSchema(final DataSetLoadDto dto, final JsonXlsxSchemaDto schema) throws IOException {
        if (schema == null) {
            return UnaryOperator.identity();
        }
        dto.setXlsxSchemaSource("");
        final XlsxSchema inline = new FromJsonXlsxSchemaBuilder().build(JsonMapper.createDefault().writeValueAsString(schema));
        return param -> param.setXlsxSchema(inline);
    }

    /**
     * フォームの値をコマンド実行時と同じく-src.&lt;オプション名&gt;の引数として解釈する（ファイルパスの解決も同じ）
     */
    private DataSetLoadDto toLoadDto(final Map<String, String> srcOptions) {
        final Map<String, String> args = new LinkedHashMap<>();
        Optional.ofNullable(srcOptions).orElse(Map.of()).forEach((key, value) -> args.put("-src." + key, value));
        return ConvertOption.toDto(AbstractCommandController.resolveSidecarFilePaths(
                new CommandParameters(Type.convert, args)).args()).getSrcData();
    }

    /**
     * 保存時と同じ形式のJSONにして、settingsにこの1件だけを持つ設定として読む。
     * 設定の対象外のテーブルは読み込まず、設定を適用した結果だけを返す
     */
    private TableSeparators inlineTableSeparators(final DatasetSettingDto setting) throws IOException {
        final String json = "{\"settings\":[" + JsonMapper.createDefault().writeValueAsString(setting) + "]}";
        try (final JsonReader reader = Json.createReader(new StringReader(json))) {
            return new FromJsonTableSeparatorsBuilder(StandardCharsets.UTF_8.name())
                    .configureSetting(reader.readObject())
                    .build()
                    .onlySettingTargets();
        }
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
     * 読み込んだテーブルを集める（分割後も同じインスタンスで受け、テーブルの終わりでは何もしない）
     */
    private abstract static class Collector implements IDataSetConverter {
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
        public abstract void startTable(ITableMetaData metaData);

        @Override
        public void endTable() {
            // nothing
        }
    }

    /**
     * 出力されるテーブル名だけを集める（行は捨てる）
     */
    private static class TableNameCollector extends Collector {
        private final Set<String> tableNames = new LinkedHashSet<>();

        @Override
        public void startTable(final ITableMetaData metaData) {
            this.tableNames.add(metaData.getTableName());
        }

        @Override
        public void row(final Object[] values) {
            // テーブル名だけを使う
        }
    }

    /**
     * 渡されたテーブル（{@link TargetTableConverter}で対象だけに絞られる）の先頭{@link #PREVIEW_ROW_LIMIT}行と全行数を集める
     */
    private static class PreviewCollector extends Collector {
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
        public void startTable(final ITableMetaData metaData) {
            if (this.builder == null) {
                this.builder = new ComparableTable.Builder(metaData);
            }
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
