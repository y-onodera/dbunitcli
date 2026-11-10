package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.stream.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.sidecar.domain.project.ResourceFile;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.ResourceLoadRequestDto;
import yo.dbunitcli.sidecar.dto.ResourceSaveRequest;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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
     * encodingはコマンド実行時にそのファイルを読む文字コード。GUIからも同じ文字コードで読み書きする。
     * 応答は{"content": 内容, "location": 開いたファイルの場所}。見つからなければcontentは{@link #toContent}の空の値、locationはnull
     */
    @Post(uri = "load", produces = MediaType.APPLICATION_JSON)
    public String load(@Body final ResourceLoadRequestDto body) {
        try {
            final Optional<ResourceFile.Loaded> loaded = this.getResourceFile().load(body.getName(), body.getEncoding());
            final JsonObjectBuilder builder = Json.createObjectBuilder()
                    .add("content", this.toContent(loaded.map(ResourceFile.Loaded::content)));
            loaded.map(ResourceFile.Loaded::location).ifPresentOrElse(
                    it -> builder.add("location", Json.createObjectBuilder()
                            .add("folder", it.folder())
                            .add("defaultFolder", it.defaultFolder())
                            .add("fileName", it.fileName())
                            .add("defaultFolderHasFile", it.defaultFolderHasFile())),
                    () -> builder.addNull("location"));
            return builder.build().toString();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    /**
     * ファイルの内容をJSONの値にする。JSONのファイルはその値、無ければ空のオブジェクト
     */
    protected JsonValue toContent(final Optional<String> content) {
        return content.<JsonValue>map(it -> {
            try (final JsonReader reader = Json.createReader(new StringReader(it))) {
                return reader.readValue();
            }
        }).orElse(JsonValue.EMPTY_JSON_OBJECT);
    }

    @Post(uri = "save", produces = MediaType.APPLICATION_JSON)
    public String save(@Body final DTO body) throws IOException {
        try {
            this.saveJson(body.getName(), this.serializeJson(body), body.getEncoding(), body.isOverwrite());
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

    protected void saveJson(final String name, final String json, final String encoding, final boolean overwrite) throws IOException {
        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = Json.createWriterFactory(
                        Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true))
                .createWriter(stringWriter);
        jsonWriter.writeObject(Json.createReader(new StringReader(json)).readObject());
        jsonWriter.close();
        LOGGER.info(stringWriter.toString());
        this.getResourceFile().save(name, stringWriter.toString(), encoding, overwrite);
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