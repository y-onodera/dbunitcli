package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.Command;
import yo.dbunitcli.application.CommandDto;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.CommandType;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.CommandRequestDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommandController<DTO extends CommandDto, OPTION extends CommandLineOption<DTO>, T extends Command<DTO, OPTION>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommandController.class);

    private final Workspace workspace;

    public AbstractCommandController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Get(uri = "add", produces = MediaType.APPLICATION_JSON)
    public String add() {
        try {
            this.workspace
                    .options()
                    .add(this.getCommandType()
                            , "new item"
                            , this.getCommand()
                                    .parseOption(new String[]{})
                                    .toArgs(false));
            return this.parameterNames();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "copy", produces = MediaType.APPLICATION_JSON)
    public String copy(@Body final CommandRequestDto input) {
        try {
            this.workspace.parameterFiles(this.getCommandType())
                    .filter(it -> it.toFile().getName().equals(input.getName() + ".txt"))
                    .findFirst()
                    .ifPresent(target -> {
                        try {
                            this.workspace
                                    .options()
                                    .add(this.getCommandType()
                                            , target.getFileName().toString().replaceAll(".txt", "")
                                            , this.readParameters(target));
                        } catch (final IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
            return this.parameterNames();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "delete", produces = MediaType.APPLICATION_JSON)
    public String delete(@Body final CommandRequestDto input) {
        try {
            this.workspace.options().delete(this.getCommandType(), input.getName());
            return this.parameterNames();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "rename", produces = MediaType.APPLICATION_JSON)
    public String rename(@Body final CommandRequestDto input) {
        try {
            this.workspace
                    .options()
                    .rename(this.getCommandType(), input.getOldName(), input.getNewName());
            return this.parameterNames();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "load", produces = MediaType.APPLICATION_JSON)
    public String load(@Body final CommandRequestDto input) {
        return this.workspace.parameterFiles(this.getCommandType())
                .filter(it -> it.toFile().getName().equals(input.getName() + ".txt"))
                .findFirst()
                .map(target -> {
                    try {
                        return this.toJson(this.argsToMap(this.readParameters(target)));
                    } catch (final IOException th) {
                        AbstractCommandController.LOGGER.error("cause:", th);
                        throw new ApplicationException(th);
                    }
                })
                .orElse("{}");
    }

    @Get(uri = "reset", produces = MediaType.APPLICATION_JSON)
    public String reset() {
        try {
            return this.toJson(this.argsToMap(new String[]{}));
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "refresh", produces = MediaType.APPLICATION_JSON)
    public String refresh(@Body final Map<String, String> input) {
        try {
            return this.toJson(this.requestToMap(input));
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "save", produces = MediaType.TEXT_PLAIN)
    public String save(@Body final CommandRequestDto body) {
        try {
            this.workspace.options().update(this.getCommandType()
                    , body.getName()
                    , this.getCommand().parseOption(this.requestToArgs(body.getInput()))
                            .toCommandLineArgs()
                            .toArgs(false));
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return "success";
    }

    @Post(uri = "exec", produces = MediaType.TEXT_PLAIN)
    public String exec(@Body final CommandRequestDto body) {
        try {
            LOGGER.info(System.getProperty(FileResources.PROPERTY_WORKSPACE));
            final OPTION options = this.getCommand()
                    .parseOption(body.getName()
                            , this.requestToArgs(body.getInput())
                            , Parameter.none());
            this.getCommand().exec(options);
            return this.resultDir(options);
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Error
    public HttpResponse<JsonError> handleException(final HttpRequest<?> request, final ApplicationException ex) {
        return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, "Fix Input Parameter")
                .body(new JsonError("Execution failed. cause: " + ex.getMessage()));
    }

    abstract protected T getCommand();

    abstract protected CommandType getCommandType();

    protected String resultDir(final OPTION options) {
        return "";
    }

    protected String parameterNames() throws IOException {
        return this.toJson(this.workspace.parameterNames(this.getCommandType()).toList());
    }

    protected String[] readParameters(final Path target) throws IOException {
        return Files.readAllLines(target).toArray(new String[0]);
    }

    protected Map<String, Object> requestToMap(final Map<String, String> input) {
        return this.argsToMap(this.requestToArgs(input));
    }

    protected Map<String, Object> argsToMap(final String[] args) {
        return this.getCommand()
                .parseOption(args)
                .toCommandLineArgs()
                .toMap();
    }

    protected String[] requestToArgs(final Map<String, String> input) {
        return input.entrySet()
                .stream()
                .filter(entry -> Strings.isNotEmpty(entry.getValue()))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toArray(String[]::new);
    }

    protected String toJson(final Map<String, Object> object) throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(object);
    }

    protected String toJson(final List<String> object) throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(object);
    }

}
