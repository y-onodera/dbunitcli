package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.command.GenerateType;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.producer.ComparableXlsxDataSetProducer;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.JsonXlsxSchemaDto;
import yo.dbunitcli.sidecar.dto.JsonXlsxSchemaRequestDto;
import yo.dbunitcli.sidecar.dto.XlsxSheetsRequestDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

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

    /**
     * データセットのカラムメタデータからxlsxSchemaテンプレートを生成して返す。
     * generateコマンド（generateType=xlsxSchema）を一時ディレクトリへ実行し、
     * テーブル別に出力されたJSONをload応答と同形（rows/cells）へマージする。保存は行わない。
     */
    @Post(uri = "generate", produces = MediaType.APPLICATION_JSON)
    public String generate(@Body final Map<String, String> input) throws IOException {
        if (Strings.isEmpty(input.get("-src.src"))) {
            throw new ApplicationException(new IllegalArgumentException("-src.src is required"));
        }
        final Path tempDir = GenerateTemplateSupport.execToTempDir(
                GenerateType.xlsxSchema, input, "xlsxSchemaGenerate");
        try {
            return ObjectMapper.getDefault().writeValueAsString(this.mergeGenerated(tempDir));
        } catch (final IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        } finally {
            GenerateTemplateSupport.deleteRecursively(tempDir);
        }
    }

    private JsonXlsxSchemaDto mergeGenerated(final Path tempDir) throws IOException {
        final JsonXlsxSchemaDto merged = new JsonXlsxSchemaDto();
        merged.setRows(new ArrayList<>());
        merged.setCells(new ArrayList<>());
        try (final Stream<Path> files = Files.walk(tempDir)) {
            for (final Path path : files.filter(it -> it.toString().endsWith(".json"))
                    .sorted(Comparator.comparing(it -> it.getFileName().toString()))
                    .toList()) {
                final JsonXlsxSchemaDto dto = ObjectMapper.getDefault()
                        .readValue(Files.readString(path), JsonXlsxSchemaDto.class);
                if (dto.getRows() != null) {
                    merged.getRows().addAll(dto.getRows());
                }
                if (dto.getCells() != null) {
                    merged.getCells().addAll(dto.getCells());
                }
            }
        }
        return merged;
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