package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.ScaffoldRequestDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller("/scaffold")
public class ScaffoldController extends AbstractCommandController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScaffoldController.class);

    public ScaffoldController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Type getCommandType() {
        return Type.scaffold;
    }

    @Post(uri = "exec-with-type", produces = MediaType.APPLICATION_JSON)
    public String execWithType(@Body final ScaffoldRequestDto body) {
        try {
            final List<String> targets = body.getGenerateTargets() != null ? body.getGenerateTargets() : List.of();
            final Map<String, Object> result = new LinkedHashMap<>();

            if (targets.contains("parameter") && body.getCommandType() != null) {
                final Type type = Type.valueOf(body.getCommandType());
                final Map<String, String> commandInput = body.getInput() != null ? body.getInput() : Map.of();
                result.put("parameterResult", new CommandParameters(type, commandInput).serialize());
            }

            final boolean runScaffold = targets.isEmpty()
                    || targets.contains("ddl") || targets.contains("javaBean");
            if (runScaffold) {
                final Map<String, String> scaffoldInput = new HashMap<>(
                        body.getScaffoldInput() != null ? body.getScaffoldInput() : Map.of());
                final List<String> scaffoldTargets = targets.stream()
                        .filter(t -> t.equals("ddl") || t.equals("javaBean"))
                        .toList();
                if (!scaffoldTargets.isEmpty()) {
                    scaffoldInput.put("-generateTargets", String.join(",", scaffoldTargets));
                }
                result.put("resultDir", this.execWithInput(Type.scaffold, scaffoldInput, body.getName()));
            }

            return this.toJson(result);
        } catch (final IOException e) {
            LOGGER.error("cause:", e);
            throw new ApplicationException(e);
        }
    }
}
