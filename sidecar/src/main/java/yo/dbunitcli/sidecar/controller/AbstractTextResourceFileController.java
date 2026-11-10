package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import jakarta.json.Json;
import jakarta.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.TemplateRequestDto;

import java.io.IOException;
import java.util.Optional;

/**
 * SQL等の生テキストのリソース。loadのcontentは文字列で返し、saveはcontentを生テキストとして保存する。応答は更新後のファイル一覧
 */
public abstract class AbstractTextResourceFileController extends AbstractResourceFileController<TemplateRequestDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTextResourceFileController.class);

    protected AbstractTextResourceFileController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected JsonValue toContent(final Optional<String> content) {
        return Json.createValue(content.orElse(""));
    }

    @Post(uri = "save", produces = MediaType.APPLICATION_JSON)
    @Override
    public String save(@Body final TemplateRequestDto body) throws IOException {
        try {
            this.getResourceFile().save(body.getName(), body.getInput().getContent(), body.getEncoding(), body.isOverwrite());
        } catch (final IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.currentFileList();
    }
}
