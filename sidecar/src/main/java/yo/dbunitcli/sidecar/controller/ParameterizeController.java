package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.CommandRequestDto;

import java.io.IOException;

@Controller("/parameterize")
public class ParameterizeController extends AbstractCommandController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterizeController.class);

    private final Workspace workspace;

    public ParameterizeController(final Workspace workspace) {
        super(workspace);
        this.workspace = workspace;
    }

    @Post(uri = "files", produces = MediaType.APPLICATION_JSON)
    public String files(@Body final CommandRequestDto input) {
        try {
            return ObjectMapper.getDefault().writeValueAsString(this.workspace.parameterizeFiles(input.getName()));
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
    }

    @Post(uri = "delete-with-files", produces = MediaType.APPLICATION_JSON)
    public String deleteWithFiles(@Body final CommandRequestDto input) throws IOException {
        try {
            this.workspace.deleteParameterize(input.getName(), input.getDeleteFiles());
        } catch (final IOException e) {
            throw e;
        } catch (final Throwable th) {
            LOGGER.error("cause:", th);
            throw new ApplicationException(th);
        }
        return this.parameterNames();
    }

    @Override
    protected Type getCommandType() {
        return Type.parameterize;
    }
}
