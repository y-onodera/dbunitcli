package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.ScaffoldRequestDto;

import java.util.HashMap;
import java.util.Map;

@Controller("/scaffold")
public class ScaffoldController extends AbstractCommandController {

    public ScaffoldController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Type getCommandType() {
        return Type.scaffold;
    }

    @Post(uri = "exec-with-type", produces = MediaType.TEXT_PLAIN)
    public String execWithType(@Body final ScaffoldRequestDto body) {
        final Map<String, String> input = new HashMap<>(body.getInput() != null ? body.getInput() : Map.of());
        if (body.getGenerateTargets() != null && !body.getGenerateTargets().isEmpty()) {
            input.put("-generateTargets", String.join(",", body.getGenerateTargets()));
        }
        return this.execWithInput(Type.scaffold, input, body.getName());
    }
}
