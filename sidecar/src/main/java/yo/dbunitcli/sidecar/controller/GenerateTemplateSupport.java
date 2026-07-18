package yo.dbunitcli.sidecar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.command.Generate;
import yo.dbunitcli.application.command.GenerateType;
import yo.dbunitcli.application.command.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * リソース編集ダイアログ（xlsx-schema・dataset-setting等）から、既存データセットを元に
 * generateコマンドで雛型を生成するための共通処理。フォーム値の-src.*のみをgenerateへ渡し、
 * 一時ディレクトリへ出力・読み取り後にクリーンアップする。
 */
final class GenerateTemplateSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateTemplateSupport.class);

    private GenerateTemplateSupport() {
    }

    /** -src.*のみを引き継ぎ、generateType固定・一時ディレクトリ出力でgenerateを実行する */
    static Path execToTempDir(final GenerateType generateType, final Map<String, String> input,
                              final String tempDirPrefix) throws IOException {
        final Path tempDir = Files.createTempDirectory(tempDirPrefix);
        final Map<String, String> params = new LinkedHashMap<>(input);
        params.keySet().removeIf(key -> !key.startsWith("-src."));
        params.put("-generateType", generateType.name());
        params.put("-result", tempDir.toAbsolutePath().toString());
        final Generate generate = new Generate();
        generate.exec(generate.parseOption(
                AbstractCommandController.resolveSidecarFilePaths(
                        new CommandParameters(Type.generate, params)).args()));
        return tempDir;
    }

    static void deleteRecursively(final Path tempDir) {
        try (final Stream<Path> paths = Files.walk(tempDir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                if (!path.toFile().delete()) {
                    LOGGER.warn("failed to delete temp file: {}", path);
                }
            });
        } catch (final IOException ex) {
            LOGGER.warn("failed to clean up temp dir: {}", tempDir, ex);
        }
    }
}
