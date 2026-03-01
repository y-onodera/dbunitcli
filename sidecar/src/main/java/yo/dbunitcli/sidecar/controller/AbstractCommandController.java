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
import yo.dbunitcli.application.Command;
import yo.dbunitcli.application.CommandDto;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.CommandParameters;
import yo.dbunitcli.sidecar.domain.project.CommandType;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.CommandRequestDto;

import java.io.IOException;
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
            this.workspace.options().newItem(this.getCommandType());
            return this.parameterNames();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "copy", produces = MediaType.APPLICATION_JSON)
    public String copy(@Body final CommandRequestDto input) {
        try {
            this.workspace.options().copy(this.getCommandType(), input.getName());
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
            this.workspace.options()
                    .rename(this.getCommandType(), input.getOldName(), input.getNewName());
            return this.parameterNames();
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "load", produces = MediaType.APPLICATION_JSON)
    public String load(@Body final CommandRequestDto input) {
        return this.load(this.getCommandType(), input.getName());
    }

    @Get(uri = "reset", produces = MediaType.APPLICATION_JSON)
    public String reset() {
        try {
            return this.toJson(this.toResponse(new CommandParameters(this.getCommandType(), new String[]{})));
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "refresh", produces = MediaType.APPLICATION_JSON)
    public String refresh(@Body final Map<String, String> input) {
        try {
            return this.toJson(this.requestToResponse(input));
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "save", produces = MediaType.TEXT_PLAIN)
    public String save(@Body final CommandRequestDto body) {
        try {
            this.workspace.options().update(body.getName()
                    , new CommandParameters(this.getCommandType(), body.getInput()).shrink());
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
            final CommandParameters parameters = new CommandParameters(this.getCommandType(), body.getInput());
            try {
                parameters.exec(body.getName());
            } catch (final Command.CommandFailException th) {
                LOGGER.info("cause:", th);
            }
            return parameters.resultDir(body.getName());
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "parameterize", produces = MediaType.APPLICATION_JSON)
    public String parameterize(@Body final CommandRequestDto input) {
        String parameterizeName = this.workspace.parameterize(this.getCommandType(), input.getName());
        return this.load(CommandType.parameterize, parameterizeName);
    }

    @Error
    public HttpResponse<JsonError> handleException(final HttpRequest<?> request, final ApplicationException ex) {
        return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, "Fix Input Parameter")
                           .body(new JsonError("Execution failed. cause: " + ex.getMessage()));
    }

    protected String load(final CommandType commandType, final String name) {
        return this.workspace.options().select(commandType, name)
                             .map(target -> {
                                 try {
                                     return this.toJson(this.toResponse(target));
                                 } catch (final IOException th) {
                                     AbstractCommandController.LOGGER.error("cause:", th);
                                     throw new ApplicationException(th);
                                 }
                             })
                             .orElse("{}");
    }

    abstract protected CommandType getCommandType();

    protected String parameterNames() throws IOException {
        return this.toJson(this.workspace.parameterNames(this.getCommandType()).toList());
    }

    protected Map<String, Object> requestToResponse(final Map<String, String> input) {
        return this.toResponse(new CommandParameters(this.getCommandType(), input));
    }

    protected Map<String, Object> toResponse(final CommandParameters commandParameters) {
        return commandParameters.toOptionParameters().serialize();
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
