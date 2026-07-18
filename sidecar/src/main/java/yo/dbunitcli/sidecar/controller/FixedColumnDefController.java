package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.command.GenerateType;
import yo.dbunitcli.dataset.converter.FixedColumnDef;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.FixedColumnDefDto;
import yo.dbunitcli.sidecar.dto.FixedColumnDefRequestDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

@Controller("fixed-column-def")
public class FixedColumnDefController extends AbstractResourceFileController<FixedColumnDefRequestDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixedColumnDefController.class);

    public FixedColumnDefController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().fixedColumnDef();
    }

    @Override
    protected String serializeJson(final FixedColumnDefRequestDto body) {
        final ST st = new ST(GenerateType.fixedColumnDef.getStGroup(),
                GenerateType.fixedColumnDef.getTemplateString(null));
        st.add("rows", body.getInput().getColumns().stream()
                .map(col -> new FixedColumnDef(col.name(), col.length(),
                        col.align(), col.pad()))
                .toList());
        return st.render();
    }

    /**
     * データセットのカラムメタデータからfixedColumnDefテンプレートを生成して返す。
     * generateコマンド（generateType=fixedColumnDef）を一時ディレクトリへ実行し、
     * テーブル別に出力されたJSONのcolumnsをload応答と同形へマージする。保存は行わない。
     */
    @Post(uri = "generate", produces = MediaType.APPLICATION_JSON)
    public String generate(@Body final Map<String, String> input) throws IOException {
        if (Strings.isEmpty(input.get("-src.src"))) {
            throw new ApplicationException(new IllegalArgumentException("-src.src is required"));
        }
        final Path tempDir = GenerateTemplateSupport.execToTempDir(
                GenerateType.fixedColumnDef, input, "fixedColumnDefGenerate");
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

    private FixedColumnDefDto mergeGenerated(final Path tempDir) throws IOException {
        final FixedColumnDefDto merged = new FixedColumnDefDto();
        merged.setColumns(new ArrayList<>());
        try (final Stream<Path> files = Files.walk(tempDir)) {
            for (final Path path : files.filter(it -> it.toString().endsWith(".json"))
                    .sorted(Comparator.comparing(it -> it.getFileName().toString()))
                    .toList()) {
                final FixedColumnDefDto dto = ObjectMapper.getDefault()
                        .readValue(Files.readString(path), FixedColumnDefDto.class);
                if (dto.getColumns() != null) {
                    merged.getColumns().addAll(dto.getColumns());
                }
            }
        }
        return merged;
    }
}
