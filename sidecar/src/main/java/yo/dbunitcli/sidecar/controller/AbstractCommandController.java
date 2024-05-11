package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.Command;
import yo.dbunitcli.application.CommandDto;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.sidecar.domain.project.CommandType;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.io.IOException;
import java.nio.file.Files;

public abstract class AbstractCommandController<DTO extends CommandDto, OPTION extends CommandLineOption<DTO>, T extends Command<DTO, OPTION>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommandController.class);

    private final Workspace workspace;

    public AbstractCommandController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Get(uri = "list", produces = MediaType.APPLICATION_JSON)
    public String list() throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.workspace.parameterFiles(this.getCommandType())
                        .map(it -> it.toFile().getName())
                        .toList()
                );
    }

    @Post(uri = "load", produces = MediaType.APPLICATION_JSON)
    public String load(@Body final String parameterFile) {
        return this.workspace.parameterFiles(this.getCommandType())
                .filter(it -> it.toFile().getName().equals(parameterFile))
                .findFirst()
                .map(target -> {
                    try {
                        return ObjectMapper
                                .getDefault()
                                .writeValueAsString(this.getCommand()
                                        .createDto(Files.readAllLines(target)
                                                .toArray(new String[0])));
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse("{}");
    }

    @Post(uri = "save", produces = MediaType.TEXT_PLAIN)
    public String save(@Body final OptionDto<DTO> input) {
        try {
            final OPTION option = this.getCommand().parseOption(input.getValue());
            this.workspace.save(this.getCommandType(), input.getName(), option.toArgs(false));
        } catch (final Throwable th) {
            AbstractCommandController.LOGGER.error("cause:", th);
            return "failed";
        }
        return "success";
    }

    @Post(uri = "exec", produces = MediaType.TEXT_PLAIN)
    public String exec(@Body final DTO input) {
        try {
            final OPTION option = this.getCommand().parseOption(input);
            this.getCommand().exec(option);
        } catch (final Throwable th) {
            AbstractCommandController.LOGGER.error("cause:", th);
            return "failed";
        }
        return "success";
    }

    abstract protected T getCommand();

    abstract protected CommandType getCommandType();

}
