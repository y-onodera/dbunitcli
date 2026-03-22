package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.Command;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.CommandRequestDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommandController implements ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommandController.class);

    private static final EnumSet<Option.BaseDir> SIDECAR_RESOLVE_BASEDIRS = EnumSet.of(
            Option.BaseDir.SETTING, Option.BaseDir.TEMPLATE, Option.BaseDir.JDBC,
            Option.BaseDir.XLSX_SCHEMA, Option.BaseDir.PARAMETERIZE_TEMPLATE);

    private final Workspace workspace;

    public AbstractCommandController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Post(uri = "load", produces = MediaType.APPLICATION_JSON)
    public String load(@Body final CommandRequestDto input) {
        return this.load(this.getCommandType(), input.getName());
    }

    @Get(uri = "reset", produces = MediaType.APPLICATION_JSON)
    public String reset() {
        try {
            return this.toJson(new CommandParameters(this.getCommandType(), new String[]{}).serialize());
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

    @Get(uri = "add", produces = MediaType.APPLICATION_JSON)
    public String add() throws IOException {
        try {
            this.workspace.options().newItem(this.getCommandType());
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.parameterNames();
    }

    @Post(uri = "copy", produces = MediaType.APPLICATION_JSON)
    public String copy(@Body final CommandRequestDto input) throws IOException {
        try {
            this.workspace.options().copy(this.getCommandType(), input.getName());
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.parameterNames();
    }

    @Post(uri = "delete", produces = MediaType.APPLICATION_JSON)
    public String delete(@Body final CommandRequestDto input) throws IOException {
        try {
            this.workspace.options().delete(this.getCommandType(), input.getName());
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.parameterNames();
    }

    @Post(uri = "rename", produces = MediaType.APPLICATION_JSON)
    public String rename(@Body final CommandRequestDto input) throws IOException {
        try {
            this.workspace.options().rename(this.getCommandType(), input.getOldName(), input.getNewName());
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.parameterNames();
    }

    @Post(uri = "save", produces = MediaType.TEXT_PLAIN)
    public String save(@Body final CommandRequestDto body) throws IOException {
        try {
            this.workspace.options().update(body.getName(), new CommandParameters(this.getCommandType(), body.getInput()).shrink());
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return "success";
    }

    @Post(uri = "shell", produces = MediaType.TEXT_PLAIN)
    public String shell(@Body final CommandRequestDto body) throws IOException {
        try {
            return Path.of(this.workspace.saveShell(this.getCommandType(), body.getName())).getParent().toString();
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "parameterize", produces = MediaType.APPLICATION_JSON)
    public String parameterize(@Body final CommandRequestDto input) throws IOException {
        try {
            return this.load(Type.parameterize, this.workspace.parameterize(this.getCommandType(), input.getName()));
        } catch (IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "exec", produces = MediaType.TEXT_PLAIN)
    public String exec(@Body final CommandRequestDto body) {
        try {
            LOGGER.info(System.getProperty(FileResources.PROPERTY_WORKSPACE));
            final CommandParameters parameters = new CommandParameters(this.getCommandType(), body.getInput())
                    .resolveFilePaths((baseDir, value) ->
                            SIDECAR_RESOLVE_BASEDIRS.contains(baseDir) && !value.isEmpty() && !new File(value).isAbsolute()
                                    ? new File(Workspace.resolveBaseDir(baseDir.name(), null), value).getAbsolutePath()
                                    : value);
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

    abstract protected Type getCommandType();

    protected String load(final yo.dbunitcli.application.CommandType commandType, final String name) {
        return this.workspace.options().select(commandType, name).map(target -> {
            try {
                return this.toJson(target.serialize());
            } catch (final IOException th) {
                AbstractCommandController.LOGGER.error("cause:", th);
                throw new ApplicationException(th);
            }
        }).orElse("{}");
    }

    protected String parameterNames() throws IOException {
        return this.toJson(this.workspace.parameterNames(this.getCommandType()).toList());
    }

    protected Map<String, Object> requestToResponse(final Map<String, String> input) {
        return new CommandParameters(this.getCommandType(), input).serialize();
    }

    protected String toJson(final Map<String, Object> object) throws IOException {
        return ObjectMapper.getDefault().writeValueAsString(object);
    }

    protected String toJson(final List<String> object) throws IOException {
        return ObjectMapper.getDefault().writeValueAsString(object);
    }

}
