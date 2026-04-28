package yo.dbunitcli.sidecar.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dbunit.dataset.Column;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.DatasetRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTableNamesRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTablePreviewResponseDto;

@Controller("dataset-setting")
public class DatasetSettingsController extends AbstractResourceFileController<DatasetRequestDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetSettingsController.class);

    public DatasetSettingsController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().datasetSetting();
    }

    @Post(uri = "table-names", produces = MediaType.APPLICATION_JSON)
    public String tableNames(@Body final DatasetTableNamesRequestDto request) {
        try {
            final DataSetLoadDto dto = new DataSetLoadDto();
            dto.setSrc(request.getSrc());
            dto.setSrcType(yo.dbunitcli.dataset.DataSourceType.valueOf(request.getSrcType()));
            dto.setRegTableInclude(request.getRegTableInclude());
            dto.setRegTableExclude(request.getRegTableExclude());
            dto.setRecursive(String.valueOf(request.isRecursive()));
            dto.setRegInclude(request.getRegInclude());
            dto.setRegExclude(request.getRegExclude());
            dto.setExtension(request.getExtension());
            dto.setLoadData("false");
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

            final DataSetLoadOption option = new DataSetLoadOption(Objects.toString(request.getSetting(), ""), dto);
            final ComparableDataSetParam param = option.getParam().build();
            return ObjectMapper.getDefault().writeValueAsString(
                    new ComparableDataSetLoader(Parameter.none())
                            .loadDataSet(param).getTableNames()
            );
        } catch (final Throwable th) {
            LOGGER.warn("Could not get table names", th);
            return "[]";
        }
    }

    @Post(uri = "table-preview", produces = MediaType.APPLICATION_JSON)
    public String tablePreview(@Body final DatasetTablePreviewRequestDto request) {
        try {
            final DataSetLoadDto dto = new DataSetLoadDto();
            dto.setSrc(request.getSrc());
            dto.setSrcType(yo.dbunitcli.dataset.DataSourceType.valueOf(request.getSrcType()));
            dto.setRegTableInclude(request.getRegTableInclude());
            dto.setRegTableExclude(request.getRegTableExclude());
            dto.setRecursive(String.valueOf(request.isRecursive()));
            dto.setRegInclude(request.getRegInclude());
            dto.setRegExclude(request.getRegExclude());
            dto.setExtension(request.getExtension());
            dto.setLoadData("true");
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

            final DataSetLoadOption option = new DataSetLoadOption(Objects.toString(request.getSetting(), ""), dto);
            final ComparableDataSetParam param = option.getParam().build();
            final ComparableDataSet dataSet = new ComparableDataSetLoader(Parameter.none()).loadDataSet(param);
            final ComparableTable table = dataSet.getTable(request.getTableName());
            if (table == null) {
                return ObjectMapper.getDefault().writeValueAsString(new DatasetTablePreviewResponseDto(new String[0], List.of()));
            }
            final Column[] columns = table.getTableMetaData().getColumns();
            final String[] headers = Arrays.stream(columns).map(Column::getColumnName).toArray(String[]::new);
            final int rowCount = Math.min(table.getRowCount(), 5);
            final List<String[]> rows = new ArrayList<>(rowCount);
            for (int i = 0; i < rowCount; i++) {
                final Object[] row = table.getRow(i);
                final String[] strRow = new String[headers.length];
                for (int j = 0; j < headers.length; j++) {
                    final Object val = j < row.length ? row[j] : null;
                    strRow[j] = val == null ? "" : val.toString();
                }
                rows.add(strRow);
            }
            return ObjectMapper.getDefault().writeValueAsString(new DatasetTablePreviewResponseDto(headers, rows));
        } catch (final Throwable th) {
            LOGGER.warn("Could not get table preview", th);
            try {
                return ObjectMapper.getDefault().writeValueAsString(new DatasetTablePreviewResponseDto(new String[0], List.of()));
            } catch (final Exception ex) {
                return "{\"headers\":[],\"rows\":[]}";
            }
        }
    }
}
