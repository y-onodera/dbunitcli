package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.DatasetRequestDto;
import yo.dbunitcli.sidecar.dto.DatasetTableNamesRequestDto;

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

            final DataSetLoadOption option = new DataSetLoadOption(request.getSetting() != null ? request.getSetting() : "", dto);
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
}
