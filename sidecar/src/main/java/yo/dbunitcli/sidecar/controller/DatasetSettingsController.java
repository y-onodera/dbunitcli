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
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.DatasetRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTableNamesRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewResponseDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller("dataset-setting")
public class DatasetSettingsController extends AbstractResourceFileController<DatasetRequestDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetSettingsController.class);

    public DatasetSettingsController(final Workspace workspace) {
        super(workspace);
    }

    @Post(uri = "table-names", produces = MediaType.APPLICATION_JSON)
    public String tableNames(@Body final DatasetTableNamesRequestDto request) {
        try {
            return ObjectMapper.getDefault().writeValueAsString(
                    new ComparableDataSetLoader(Parameter.none()).loadDataSet(this.toParam(request, "false").build())
                                                                 .getTableNames());
        } catch (final Throwable th) {
            LOGGER.warn("Could not get table names", th);
            return "[]";
        }
    }

    @Post(uri = "table-preview", produces = MediaType.APPLICATION_JSON)
    public String tablePreview(@Body final DatasetTablePreviewRequestDto request) {
        try {
            List<ComparableTable.Builder> metaDataBuilder = new ArrayList<>();
            new ComparableDataSetLoader(Parameter.none()).getComparableDataSetProducer(
                    this.toParam(request, "true").setConverter(new TargetTableHandler(request, metaDataBuilder))
                        .build()).produce();
            if (metaDataBuilder.isEmpty()) {
                return ObjectMapper.getDefault()
                                   .writeValueAsString(new DatasetTablePreviewResponseDto(new String[0], List.of()));
            }
            ComparableTable table = metaDataBuilder.getFirst().build();
            final Column[] columns = table.getTableMetaData().getColumns();
            final String[] headers = Arrays.stream(columns).map(Column::getColumnName).toArray(String[]::new);
            final List<Object[]> rows = table.rows().rows();
            return ObjectMapper.getDefault().writeValueAsString(new DatasetTablePreviewResponseDto(headers, rows));
        } catch (final Throwable th) {
            LOGGER.warn("Could not get table preview", th);
            try {
                return ObjectMapper.getDefault()
                                   .writeValueAsString(new DatasetTablePreviewResponseDto(new String[0], List.of()));
            } catch (final Exception ex) {
                return "{\"headers\":[],\"rows\":[]}";
            }
        }
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().datasetSetting();
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
        private final DatasetTablePreviewRequestDto request;
        private final List<ComparableTable.Builder> metaDataBuilder;
        boolean processTarget;
        int handleRows;

        public TargetTableHandler(DatasetTablePreviewRequestDto request,
                                  List<ComparableTable.Builder> metaDataBuilder) {
            this.request = request;
            this.metaDataBuilder = metaDataBuilder;
            this.processTarget = false;
            this.handleRows = 0;
        }

        @Override
        public boolean isExportEmptyTable() {
            return true;
        }

        @Override
        public void reStartTable(AddSettingTableMetaData tableMetaData, Integer writeRows) {

        }

        @Override
        public IDataSetConverter split() {
            return this;
        }

        @Override
        public void startTable(ITableMetaData metaData) {
            if (metaData.getTableName().equals(this.request.getTableName())) {
                this.metaDataBuilder.add(new ComparableTable.Builder(metaData));
                this.processTarget = this.handleRows <= 5;
            }
        }

        @Override
        public void endTable() {
            this.processTarget = false;
        }

        @Override
        public void row(Object[] values) {
            if (this.processTarget) {
                this.metaDataBuilder.getFirst().addRow(values);
                this.handleRows++;
            }
        }
    }
}
