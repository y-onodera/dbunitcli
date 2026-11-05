package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import jakarta.json.Json;
import jakarta.json.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.TemplateRequestDto;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Optional;

/**
 * SQL等の生テキストのリソース。loadは{"content": ...}形式で返し、saveはcontentを生テキストとして保存する。応答は更新後のファイル一覧
 */
public abstract class AbstractTextResourceFileController extends AbstractEncodedResourceFileController<TemplateRequestDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTextResourceFileController.class);

    protected AbstractTextResourceFileController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected String toLoadResponse(final Optional<String> content) {
        final StringWriter sw = new StringWriter();
        try (final JsonWriter writer = Json.createWriter(sw)) {
            writer.writeObject(Json.createObjectBuilder().add("content", content.orElse("")).build());
        }
        return sw.toString();
    }

    @Post(uri = "save", produces = MediaType.APPLICATION_JSON)
    @Override
    public String save(@Body final TemplateRequestDto body) throws IOException {
        try {
            this.getResourceFile().update(body.getName(), body.getInput().getContent(), this.charsetOf(body));
        } catch (final IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.currentFileList();
    }
}
