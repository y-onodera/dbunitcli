package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.Command;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.CommandRequestDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractCommandController implements ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommandController.class);

    private static final Set<String> SIDECAR_RESOLVE_PATHS = Set.of(
            "SETTING", "TEMPLATE", "JDBC", "XLSX_SCHEMA", "PARAMETERIZE_TEMPLATE");

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
            final Map<String, String> resolvedInput = this.resolveDefaultPaths(body.getInput());
            final CommandParameters parameters = new CommandParameters(this.getCommandType(), resolvedInput);
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

    private Map<String, String> resolveDefaultPaths(final Map<String, String> input) {
        final Map<String, Object> serialized = new CommandParameters(this.getCommandType(), input).serialize();
        final Map<String, String> resolved = new LinkedHashMap<>(input);
        applyResolvedPaths(serialized, resolved);
        return resolved;
    }

    @SuppressWarnings("unchecked")
    private static void applyResolvedPaths(final Map<String, Object> serialized, final Map<String, String> resolved) {
        final String prefix = (String) serialized.get("prefix");
        final List<Map<String, Object>> elements = (List<Map<String, Object>>) serialized.get("elements");
        if (elements != null) {
            for (final Map<String, Object> element : elements) {
                final String name = (String) element.get("name");
                final String value = (String) element.get("value");
                final Map<String, Object> attribute = (Map<String, Object>) element.get("attribute");
                if (attribute == null || value == null || value.isEmpty()) {
                    continue;
                }
                final String defaultPath = String.valueOf(attribute.get("defaultPath"));
                if (!SIDECAR_RESOLVE_PATHS.contains(defaultPath) || new File(value).isAbsolute()) {
                    continue;
                }
                final String key = Strings.isNotEmpty(prefix) ? "-" + prefix + "." + name : "-" + name;
                resolved.put(key, new File(Workspace.resolveBaseDir(defaultPath, null), value).getAbsolutePath());
            }
        }
        for (final Map.Entry<String, Object> entry : serialized.entrySet()) {
            if (!Set.of("prefix", "elements").contains(entry.getKey()) && entry.getValue() instanceof Map<?, ?> sub) {
                applyResolvedPaths((Map<String, Object>) sub, resolved);
            }
        }
    }

}
