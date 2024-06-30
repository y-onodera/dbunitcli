package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.Command;
import yo.dbunitcli.application.CommandDto;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.sidecar.domain.project.CommandType;
import yo.dbunitcli.sidecar.domain.project.Workspace;
import yo.dbunitcli.sidecar.dto.OptionDto;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public abstract class AbstractCommandController<DTO extends CommandDto, OPTION extends CommandLineOption<DTO>, T extends Command<DTO, OPTION>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommandController.class);

    private final Workspace workspace;

    public AbstractCommandController(final Workspace workspace) {
        this.workspace = workspace;
    }

    @Get(uri = "add", produces = MediaType.APPLICATION_JSON)
    public String add() throws IOException {
        final List<String> alreadyExists = this.workspace.parameterNames(this.getCommandType())
                .filter(it -> Pattern.compile("new item(\\([0-9]+\\))*").matcher(it).matches())
                .toList();
        final String name = IntStream.iterate(1, it -> it + 1)
                .filter(it -> !alreadyExists.contains("new item(%s)".formatted(it)))
                .mapToObj("new item(%s)"::formatted)
                .findFirst()
                .get();
        this.workspace.save(this.getCommandType()
                , alreadyExists.size() == 0 ? "new item" : name
                , this.getCommand().parseOption(new String[]{}).toArgs(false)
        );
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.workspace.parameterNames(this.getCommandType()).toList());
    }

    @Post(uri = "copy", produces = MediaType.APPLICATION_JSON)
    public String copy(@Body final OptionDto<DTO> input) throws IOException {
        this.workspace.parameterFiles(this.getCommandType())
                .filter(it -> it.toFile().getName().equals(input.getName() + ".txt"))
                .findFirst()
                .ifPresent(target -> {
                    try {
                        this.workspace.save(this.getCommandType()
                                , target.getFileName().toString().replace(".txt", "") + "(1)"
                                , Files.readAllLines(target).toArray(new String[0])
                        );
                    } catch (final IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.workspace.parameterNames(this.getCommandType()).toList());
    }

    @Post(uri = "delete", produces = MediaType.APPLICATION_JSON)
    public String delete(@Body final OptionDto<DTO> input) throws IOException {
        this.workspace.delete(this.getCommandType(), input.getName());
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.workspace.parameterNames(this.getCommandType()).toList());
    }

    @Post(uri = "rename", produces = MediaType.APPLICATION_JSON)
    public String rename(@Body final OptionDto<DTO> input) throws IOException {
        this.workspace.rename(this.getCommandType(), input.getOldName(), input.getNewName());
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.workspace.parameterNames(this.getCommandType()).toList());
    }

    @Post(uri = "load", produces = MediaType.APPLICATION_JSON)
    public String load(@Body final OptionDto<DTO> input) {
        return this.workspace.parameterFiles(this.getCommandType())
                .filter(it -> it.toFile().getName().equals(input.getName() + ".txt"))
                .findFirst()
                .map(target -> {
                    try {
                        return ObjectMapper
                                .getDefault()
                                .writeValueAsString(this.getCommand()
                                        .parseOption(Files.readAllLines(target)
                                                .toArray(new String[0]))
                                        .toCommandLineArgs()
                                        .toMap());
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse("{}");
    }

    @Get(uri = "reset", produces = MediaType.APPLICATION_JSON)
    public String reset() throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.getCommand()
                        .parseOption(new String[]{})
                        .toCommandLineArgs()
                        .toMap());
    }

    @Post(uri = "refresh", produces = MediaType.APPLICATION_JSON)
    public String refresh(@Body final DTO input) throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.getCommand()
                        .parseOption(input)
                        .toCommandLineArgs()
                        .toMap());
    }

    @Post(uri = "refresh/{name}", produces = MediaType.APPLICATION_JSON)
    public String refreshComponent(@PathVariable final String name, @Body final DTO input) throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(this.getCommand()
                        .parseOption(input)
                        .toCommandLineArgs()
                        .toMap()
                        .get(name));
    }

    @Post(uri = "save", produces = MediaType.TEXT_PLAIN)
    public String save(@Body final OptionDto<DTO> input) {
        try {
            this.workspace.save(this.getCommandType()
                    , input.getName()
                    , this.getCommand().parseOption(input.getValue()).toArgs(false));
        } catch (final Throwable th) {
            AbstractCommandController.LOGGER.error("cause:", th);
            return "failed";
        }
        return "success";
    }

    @Post(uri = "exec", produces = MediaType.TEXT_PLAIN)
    public String exec(@Body final OptionDto<DTO> input) {
        try {
            this.getCommand().exec(this.getCommand()
                    .getOptions(input.getName(), input.getValue(), Parameter.none()));
        } catch (final Throwable th) {
            AbstractCommandController.LOGGER.error("cause:", th);
            return "failed";
        }
        return "success";
    }

    abstract protected T getCommand();

    abstract protected CommandType getCommandType();

}
