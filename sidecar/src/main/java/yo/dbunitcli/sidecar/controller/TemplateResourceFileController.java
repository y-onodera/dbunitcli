package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.command.GenerateType;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.ResourceSaveRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Controller("template")
public class TemplateResourceFileController extends AbstractResourceFileController<ResourceSaveRequest<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateResourceFileController.class);
    private static final String GENERATED_FILE = "template.xlsx";
    private static final String XLSX_EXTENSION = ".xlsx";

    @Inject
    public TemplateResourceFileController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.resources().template();
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
