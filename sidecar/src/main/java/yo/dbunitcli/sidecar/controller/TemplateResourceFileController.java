package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.command.GenerateType;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.TemplateRequestDto;
import yo.dbunitcli.sidecar.job.JobManager;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Map;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("template")
public class TemplateResourceFileController extends AbstractResourceFileController<TemplateRequestDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateResourceFileController.class);
    private static final String GENERATED_FILE = "template.xlsx";
    private static final String XLSX_EXTENSION = ".xlsx";

    private final JobManager jobs;

    @Inject
    public TemplateResourceFileController(final Workspace workspace, final JobManager jobs) {
        super(workspace);
        this.jobs = jobs;
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().template();
    }

    /**
     * templateはscaffold等で書き出された生のテキスト（.stg/.txt）や既存のjsonファイルを含むため、
     * エディタが期待する{"content": ...}形式へ包んで返す（生テキストをそのまま返すとJSONとして解釈できない）。
     */
    @Post(uri = "load", consumes = MediaType.TEXT_PLAIN, produces = MediaType.APPLICATION_JSON)
    @Override
    public String load(@Body final String name) {
        try {
            final String content = this.getResourceFile().read(name).orElse("");
            final StringWriter sw = new StringWriter();
            try (final JsonWriter writer = Json.createWriter(sw)) {
                writer.writeObject(Json.createObjectBuilder().add("content", content).build());
            }
            return sw.toString();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    /** テンプレートは生のテキストとして保存する（loadの{"content": ...}形式とは非対称）。応答は更新後のファイル一覧 */
    @Post(uri = "save", produces = MediaType.APPLICATION_JSON)
    @Override
    public String save(@Body final TemplateRequestDto body) throws IOException {
        try {
            this.getResourceFile().update(body.getName(), body.getInput().getContent());
        } catch (final IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.currentFileList();
    }

    /** generate をジョブとして開始し、jobIdを返す。進捗・結果は /job/{id}/events で受け取る */
    @Post(uri = "generate/job", produces = MediaType.APPLICATION_JSON)
    public String generateJob(@Body final Map<String, String> input) throws IOException {
        return this.jobs.start(() -> this.generate(input));
    }

    /**
     * データセットのカラムメタデータからxlsxテンプレート（jxls）を生成し、-fileNameでtemplateリソースへ保存する。
     * generateコマンド（generateType=xlsxTemplate）を一時ディレクトリへ実行して取り込む。
     * xlsxTemplateは常に.xlsx形式で出力するため、拡張子が無ければ.xlsxを補う。応答はsave同様に更新後のファイル一覧。
     */
    @Post(uri = "generate", produces = MediaType.APPLICATION_JSON)
    public String generate(@Body final Map<String, String> input) throws IOException {
        if (Strings.isEmpty(input.get("-src.src"))) {
            throw new ApplicationException(new IllegalArgumentException("-src.src is required"));
        }
        final String fileName = this.toXlsxFileName(input.get("-fileName"));
        final Path tempDir = GenerateTemplateSupport.execToTempDir(
                GenerateType.xlsxTemplate, input, "xlsxTemplateGenerate", Map.of("-resultPath", GENERATED_FILE));
        try {
            this.getResourceFile().updateFrom(fileName, tempDir.resolve(GENERATED_FILE));
            return this.currentFileList();
        } catch (final IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        } finally {
            GenerateTemplateSupport.deleteRecursively(tempDir);
        }
    }

    private String toXlsxFileName(final String name) {
        if (Strings.isEmpty(name) || name.contains("/") || name.contains("\\") || name.contains("..")) {
            throw new ApplicationException(new IllegalArgumentException("-fileName is required and must be a plain file name"));
        }
        return name.toLowerCase().endsWith(XLSX_EXTENSION) ? name : name + XLSX_EXTENSION;
    }
}
