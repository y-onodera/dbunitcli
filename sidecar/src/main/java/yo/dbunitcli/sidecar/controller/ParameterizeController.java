package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.CommandRequestDto;

import java.io.IOException;

@Controller("/parameterize")
public class ParameterizeController extends AbstractCommandController {

    public ParameterizeController(final Workspace workspace) {
        super(workspace);
    }

    @Post(uri = "files", produces = MediaType.APPLICATION_JSON)
    public String files(@Body final CommandRequestDto input) throws IOException {
        return this.toJson(this.workspace().parameterizeFiles(input.getName()));
    }

    @Override
    protected void deleteItem(final CommandRequestDto input) throws IOException {
        this.workspace().deleteParameterize(input.getName(), input.getDeleteFiles());
    }

    @Override
    protected Type getCommandType() {
        return Type.parameterize;
    }
}
