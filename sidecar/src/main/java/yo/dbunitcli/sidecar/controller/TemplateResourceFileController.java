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
import yo.dbunitcli.resource.poi.jxls.JxlsCompareTemplateGenerator;
import yo.dbunitcli.resource.poi.jxls.JxlsMetaDataTemplateGenerator;
import yo.dbunitcli.sidecar.dto.XlsxCompareTemplateRequestDto;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.job.Interruptible;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller("template")
public class TemplateResourceFileController extends AbstractTextResourceFileController {

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
    @Interruptible
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

    /**
     * データシート2枚と、複合キーで突合する比較シートからなるxlsxテンプレートを生成し、fileNameでtemplateリソースへ保存する。
     * 応答はsave同様に更新後のファイル一覧
     */
    @Post(uri = "generate-compare", produces = MediaType.APPLICATION_JSON)
    public String generateCompare(@Body final XlsxCompareTemplateRequestDto request) throws IOException {
        final String fileName = this.toXlsxFileName(request.fileName());
        final JxlsCompareTemplateGenerator generator = toGenerator(request);
        final Path tempDir = Files.createTempDirectory("xlsxCompareTemplateGenerate");
        try {
            generator.write(tempDir.resolve(GENERATED_FILE).toFile());
            this.getResourceFile().updateFrom(fileName, tempDir.resolve(GENERATED_FILE));
            return this.currentFileList();
        } finally {
            GenerateTemplateSupport.deleteRecursively(tempDir);
        }
    }

    /**
     * テーブル定義書のxlsxテンプレート（jxls）を生成し、-fileNameでtemplateリソースへ保存する。
     * 応答はsave同様に更新後のファイル一覧
     */
    @Post(uri = "generate-metadata", produces = MediaType.APPLICATION_JSON)
    public String generateMetaData(@Body final Map<String, String> input) throws IOException {
        final String fileName = this.toXlsxFileName(input.get("-fileName"));
        final Path tempDir = Files.createTempDirectory("xlsxMetaDataTemplateGenerate");
        try {
            JxlsMetaDataTemplateGenerator.write(tempDir.resolve(GENERATED_FILE).toFile());
            this.getResourceFile().updateFrom(fileName, tempDir.resolve(GENERATED_FILE));
            return this.currentFileList();
        } finally {
            GenerateTemplateSupport.deleteRecursively(tempDir);
        }
    }

    private static JxlsCompareTemplateGenerator toGenerator(final XlsxCompareTemplateRequestDto request) {
        try {
            if (request.first() == null || request.second() == null) {
                throw new IllegalArgumentException("first and second are required");
            }
            return new JxlsCompareTemplateGenerator(
                    new JxlsCompareTemplateGenerator.DataSheet(request.first().sheetName(), Objects.requireNonNullElse(request.first().columns(), List.of()))
                    , new JxlsCompareTemplateGenerator.DataSheet(request.second().sheetName(), Objects.requireNonNullElse(request.second().columns(), List.of()))
                    , toPairs(request.keys())
                    , toPairs(request.compares())
                    , request.compareSheetName());
        } catch (final IllegalArgumentException e) {
            throw new ApplicationException(e);
        }
    }

    private static List<JxlsCompareTemplateGenerator.ColumnPair> toPairs(final List<XlsxCompareTemplateRequestDto.ColumnPair> pairs) {
        return Objects.requireNonNullElse(pairs, List.<XlsxCompareTemplateRequestDto.ColumnPair>of()).stream()
                .map(pair -> new JxlsCompareTemplateGenerator.ColumnPair(pair.first(), pair.second()))
                .toList();
    }

    private String toXlsxFileName(final String name) {
        if (Strings.isEmpty(name) || name.contains("/") || name.contains("\\") || name.contains("..")) {
            throw new ApplicationException(new IllegalArgumentException("-fileName is required and must be a plain file name"));
        }
        return name.toLowerCase().endsWith(XLSX_EXTENSION) ? name : name + XLSX_EXTENSION;
    }
}
