package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.command.Scaffold;
import yo.dbunitcli.application.command.ScaffoldOption;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.resource.FileResources;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Generate画面のScaffoldダイアログ用API。名前付きパラメータ管理（load/save等）を持たないため
 * AbstractCommandControllerは継承しない（WorkspaceのOptionsはscaffoldを保持しない）。
 */
@Controller("/scaffold")
public class ScaffoldController implements ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScaffoldController.class);

    /** 実行結果をgenerateType=txtへ反映できるtarget（parameterは対象外） */
    private static final Set<String> EXECUTABLE_TARGETS = Set.of("ddl", "javaBean", "xlsxSchema", "fixedColumnDef");

    /** generateフォームではsrc配下のオプションとして扱われるキーの読み替え（無印で返すと古い-src.*値が勝つ） */
    private static final Map<String, String> GENERATE_FORM_KEY_REMAP = Map.of(
            "-encoding", "-src.encoding",
            "-headerName", "-src.headerName");

    @Post(uri = "refresh", produces = MediaType.APPLICATION_JSON)
    public String refresh(@Body final Map<String, String> input) {
        try {
            return ObjectMapper.getDefault()
                               .writeValueAsString(new CommandParameters(Type.scaffold, input).serialize());
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "exec", produces = MediaType.APPLICATION_JSON)
    public String exec(@Body final Map<String, String> input) throws IOException {
        final ScaffoldOption option = this.parseOption(input);
        option.execute();
        try {
            return ObjectMapper.getDefault().writeValueAsString(this.generateFormParams(option));
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    private ScaffoldOption parseOption(final Map<String, String> input) {
        try {
            final Map<String, String> params = new LinkedHashMap<>(input);
            if (!EXECUTABLE_TARGETS.contains(params.get("-target")) || Strings.isEmpty(params.get("-template"))) {
                throw new IllegalArgumentException(
                        "scaffold requires -target=" + String.join(", ", EXECUTABLE_TARGETS) + " and -template");
            }
            params.remove("-parameter");
            params.remove("-commandType");
            params.put("-result", FileResources.baseDir().getAbsolutePath());
            return new Scaffold().parseOption(
                    AbstractCommandController.resolveSidecarFilePaths(new CommandParameters(Type.scaffold, params))
                                             .args());
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    private Map<String, String> generateFormParams(final ScaffoldOption option) {
        final Map<String, String> result = new LinkedHashMap<>();
        option.txtDrivenGenerateParams().toList(false).forEach(line -> {
            final int eqIdx = line.indexOf('=');
            final String key = line.substring(0, eqIdx);
            result.put(GENERATE_FORM_KEY_REMAP.getOrDefault(key, key), line.substring(eqIdx + 1));
        });
        return result;
    }
}
