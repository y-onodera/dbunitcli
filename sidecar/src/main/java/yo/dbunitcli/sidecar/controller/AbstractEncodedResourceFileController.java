package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.EncodedResourceSaveRequest;
import yo.dbunitcli.sidecar.dto.ResourceLoadRequestDto;

import java.nio.charset.Charset;

/**
 * コマンド実行時に-settingEncoding等の文字コードで読まれるJSONのリソース。
 * GUIからも同じ文字コードで読み書きするため、load・saveでencodingを受け取る（空ならシステムの既定）
 */
public abstract class AbstractEncodedResourceFileController<DTO extends EncodedResourceSaveRequest<?>> extends AbstractResourceFileController<DTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEncodedResourceFileController.class);

    protected AbstractEncodedResourceFileController(final Workspace workspace) {
        super(workspace);
    }

    static Charset toCharset(final String encoding) {
        return Strings.isNotEmpty(encoding) ? Charset.forName(encoding) : Charset.defaultCharset();
    }

    @Post(uri = "load", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Override
    public String load(@Body final String body) {
        try {
            final ResourceLoadRequestDto request = ObjectMapper.getDefault().readValue(body, ResourceLoadRequestDto.class);
            return this.getResourceFile().read(request.getName(), toCharset(request.getEncoding())).orElse("{}");
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Override
    protected Charset charsetOf(final DTO body) {
        return toCharset(body.getEncoding());
    }
}
