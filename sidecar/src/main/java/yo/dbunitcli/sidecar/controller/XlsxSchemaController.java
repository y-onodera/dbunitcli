package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.producer.ComparableXlsxDataSetProducer;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.JsonXlsxSchemaRequestDto;
import yo.dbunitcli.sidecar.dto.XlsxSheetsRequestDto;

import java.io.File;
import java.util.Arrays;

@Controller("xlsx-schema")
public class XlsxSchemaController extends AbstractResourceFileController<JsonXlsxSchemaRequestDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(XlsxSchemaController.class);

    public XlsxSchemaController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().xlsxSchema();
    }

    @Post(uri = "sheets", produces = MediaType.APPLICATION_JSON)
    public String sheets(@Body final XlsxSheetsRequestDto request) {
        try {
            final File src = Strings.isNotEmpty(request.getSrc())
                    ? FileResources.searchDatasetBase(request.getSrc())
                    : new File(".");
            final ComparableDataSetParam param = ComparableDataSetParam.builder()
                    .setSrc(src)
                    .setRegTableInclude(request.getRegTableInclude())
                    .setRegTableExclude(request.getRegTableExclude())
                    .setRecursive(request.isRecursive())
                    .setRegInclude(request.getRegInclude())
                    .setRegExclude(request.getRegExclude())
                    .setExtension(request.getExtension())
                    .setHeaderName("header")
                    .setLoadData(false)
                    .build();
            return ObjectMapper.getDefault().writeValueAsString(
                    Arrays.asList(new ComparableXlsxDataSetProducer(param).loadDataSet().getTableNames())
            );
        } catch (final Throwable th) {
            LOGGER.warn("Could not read sheet names from: {}", request.getSrc(), th);
            return "[]";
        }
    }
}