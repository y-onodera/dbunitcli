package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.TemplateRequestDto;

import java.io.IOException;
import java.io.StringWriter;

@Controller("parameterize/template")
public class ParameterizeTemplateResourceFileController extends AbstractResourceFileController<TemplateRequestDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterizeTemplateResourceFileController.class);

    @Inject
    public ParameterizeTemplateResourceFileController(final Workspace workspace) {
        super(workspace);
    }

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

    /** loadの{"content": ...}形式とは非対称に、contentを生テキストとして保存する。応答は更新後のファイル一覧 */
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

    @Override
    protected ResourceFile getResourceFile() {
        return this.workspace.options().templates();
    }
}
