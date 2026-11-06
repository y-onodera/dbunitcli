package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonWriter;
import jakarta.json.stream.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.ResourceLoadRequestDto;
import yo.dbunitcli.sidecar.dto.ResourceSaveRequest;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Optional;

public abstract class AbstractResourceFileController<DTO extends ResourceSaveRequest<?>> implements ControllerExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResourceFileController.class);
    protected final Workspace workspace;

    protected AbstractResourceFileController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Get(uri = "list", produces = MediaType.APPLICATION_JSON)
    public String list() {
        return this.currentFileList();
    }

    /**
     * encodingはコマンド実行時にそのファイルを読む文字コード。GUIからも同じ文字コードで読み書きする
     */
    @Post(uri = "load", produces = MediaType.APPLICATION_JSON)
    public String load(@Body final ResourceLoadRequestDto body) {
        try {
            return this.toLoadResponse(this.getResourceFile().read(body.getName(), this.charsetOf(body.getEncoding())));
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    protected String toLoadResponse(final Optional<String> content) {
        return content.orElse("{}");
    }

    @Post(uri = "save", produces = MediaType.APPLICATION_JSON)
    public String save(@Body final DTO body) throws IOException {
        try {
            this.saveJson(body.getName(), this.serializeJson(body), this.charsetOf(body.getEncoding()));
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.currentFileList();
    }

    protected String serializeJson(final DTO body) throws IOException {
        return JsonMapper.createDefault().writeValueAsString(body.getInput());
    }

    @Post(uri = "delete", consumes = MediaType.TEXT_PLAIN, produces = MediaType.APPLICATION_JSON)
    public String delete(@Body final String name) throws IOException {
        try {
            this.getResourceFile().delete(name);
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.currentFileList();
    }

    protected abstract ResourceFile getResourceFile();

    /**
     * load・saveでencodingが空のときに読み書きする文字コード
     */
    protected abstract Charset defaultCharset();

    protected Charset charsetOf(final String encoding) {
        return Strings.isNotEmpty(encoding) ? Charset.forName(encoding) : this.defaultCharset();
    }

    protected void saveJson(final String name, final String json, final Charset charset) throws IOException {
        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = Json.createWriterFactory(
                        Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true))
                .createWriter(stringWriter);
        jsonWriter.writeObject(Json.createReader(new StringReader(json)).readObject());
        jsonWriter.close();
        LOGGER.info(stringWriter.toString());
        this.getResourceFile().update(name, stringWriter.toString(), charset);
    }

    protected String currentFileList() {
        try {
            return ObjectMapper
                    .getDefault()
                    .writeValueAsString(this.getResourceFile().list());
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }
}