package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.ITableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.command.GenerateType;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.TableSeparators;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.DatasetRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTableNamesRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewResponseDto;
import yo.dbunitcli.sidecar.job.Interruptible;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
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
     * 絞り込みの強い順に試し、対象が見つからなければ絞り込みを緩めて読み直す。
     * <ol>
     * <li>設定未適用なら元テーブル名でも絞る（対象外のソースを開かない）。xlsxSchema等で名前が変わると見つからない</li>
     * <li>出力テーブル名をoutputFilterで絞る（対象外のテーブルは行を読まない）。joinは元テーブルも残す必要があるため使わない。
     * 分割（split）後の名前では見つからない</li>
     * <li>絞らずに読む</li>
     * </ol>
     */
    @Interruptible
    @Post(uri = "table-preview", produces = MediaType.APPLICATION_JSON)
    public String tablePreview(@Body final DatasetTablePreviewRequestDto request) {
        try {
            final boolean narrowSource = Strings.isEmpty(request.getSetting());
            Optional<DatasetTablePreviewResponseDto> preview = narrowSource
                    ? this.loadPreview(request, true, true)
                    : Optional.empty();
            if (preview.isEmpty()) {
                preview = this.loadPreview(request, false, true);
            }
            if (preview.isEmpty()) {
                preview = this.loadPreview(request, false, false);
            }
            return ObjectMapper.getDefault().writeValueAsString(preview.orElseGet(DatasetTablePreviewResponseDto::empty));
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
     * 対象テーブルが読み込まれなければemptyを返す。outputFilterが使えない（joinがある）場合もemptyを返す
     */
    private Optional<DatasetTablePreviewResponseDto> loadPreview(final DatasetTablePreviewRequestDto request,
                                                                 final boolean narrowSource,
                                                                 final boolean narrowOutput) throws Exception {
        final String tableName = request.getTableName();
        final ComparableDataSetParam.Builder param = this.toParam(request, "true");
        if (narrowSource) {
            param.setRegTableInclude("(?i)^" + Pattern.quote(tableName) + "$");
        }
        if (narrowOutput) {
            final TableSeparators separators = param.getTableSeparators();
            if (!separators.joinConditions().isEmpty()) {
                return Optional.empty();
            }
            param.setTableSeparators(separators.builder()
                                               .addFilter(tableName::equals, TableSeparators.FilterCombine.AND)
                                               .build());
        }
        final TargetTableHandler handler = new TargetTableHandler(tableName);
        try (final ComparableDataSetProducer producer = new ComparableDataSetLoader(this.buildParameter(request))
                .getComparableDataSetProducer(param.setConverter(handler).build())) {
            producer.produce();
        }
        return handler.toResponse();
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
        dto.setSrcType(yo.dbunitcli.dataset.DataSourceType.valueOf(request.getSrcType()));
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

    private static class TargetTableHandler implements IDataSetConverter {
        private final String tableName;
        private ComparableTable.Builder builder;
        private boolean processTarget;
        private int totalRows;

        TargetTableHandler(final String tableName) {
            this.tableName = tableName;
        }

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
            this.processTarget = metaData.getTableName().equals(this.tableName);
            if (this.processTarget && this.builder == null) {
                this.builder = new ComparableTable.Builder(metaData);
            }
        }

        @Override
        public void endTable() {
            this.processTarget = false;
        }

        @Override
        public void row(final Object[] values) {
            if (this.processTarget) {
                if (this.totalRows < PREVIEW_ROW_LIMIT) {
                    this.builder.addRow(values);
                }
                this.totalRows++;
            }
        }
    }
}
