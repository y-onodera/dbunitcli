package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.cli.ArgumentFilter;
import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.cli.DefaultArgumentFilter;
import yo.dbunitcli.application.cli.DefaultArgumentMapper;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.resource.Files;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "parameterize", mixinStandardHelpOptions = true)
public class ParameterizeOption extends CommandLineOption<ParameterizeDto> {

    private static final DefaultArgumentMapper NONE_PARAM_MAPPER = new DefaultArgumentMapper() {
        @Override
        public String[] map(final String[] arguments, final String prefix) {
            final List<String> list = Arrays.asList(arguments);
            if (this.paramTypeIsNone(list)) {
                final List<String> newArg = Arrays.stream(arguments)
                        .filter(it -> this.isNotSrcArg(prefix, it))
                        .collect(Collectors.toList());
                newArg.add("-srcType=none");
                newArg.add("-src=.");
                if (list.stream().noneMatch(it -> it.startsWith("-parameterize="))) {
                    newArg.add("-parameterize=false");
                }
                return newArg.toArray(new String[0]);
            }
            return arguments;
        }

        private boolean isNotSrcArg(final String prefix, final String it) {
            return !it.startsWith("-" + prefix + ".src=") || !it.startsWith("-" + prefix + ".srcType=")
                    || !it.startsWith("-src=") || !it.startsWith("-srcType=");
        }

        private boolean paramTypeIsNone(final List<String> list) {
            return list.contains("-srcType=none") || list.contains("-param.srcType=none")
                    || list.stream().noneMatch(it -> it.startsWith("-srcType=") || it.startsWith("-param.srcType="));
        }
    };
    private static final ArgumentFilter ARGS_IGNORE_FILTER = new DefaultArgumentFilter("-P", "-A", "-arg");
    private final DataSetLoadOption param;
    private final TemplateRenderOption templateOption;
    private final Map<String, String> args = new HashMap<>();
    private final String cmd;
    private final String cmdParam;
    private boolean ignoreFail = false;
    private boolean parameterize = true;
    private File template;

    public static ParameterizeDto toDto(final String[] args) {
        final ParameterizeDto dto = new ParameterizeDto();
        new CommandLineParser("", CommandLineOption.DEFAULT_COMMANDLINE_MAPPER, ParameterizeOption.ARGS_IGNORE_FILTER)
                .parseArgument(args, dto);
        new CommandLineParser("param", ParameterizeOption.NONE_PARAM_MAPPER)
                .parseArgument(args, dto.getDateSetLoad());
        new CommandLineParser("template").parseArgument(args, dto.getTemplateRender());
        return dto;
    }

    public ParameterizeOption(final String resultFile, final ParameterizeDto dto, final Parameter param) {
        super(resultFile, dto, param);
        this.cmd = dto.getCmd();
        this.cmdParam = dto.getCmdParam();
        this.args.putAll(dto.getArg());
        if (Strings.isNotEmpty(dto.getIgnoreFail())) {
            this.ignoreFail = Boolean.parseBoolean(dto.getIgnoreFail());
        }
        if (Strings.isNotEmpty(dto.getParameterize())) {
            this.parameterize = Boolean.parseBoolean(dto.getParameterize());
        }
        if (Strings.isNotEmpty(dto.getTemplate())) {
            this.template = new File(dto.getTemplate());
        }
        this.param = new DataSetLoadOption("param", dto.getDateSetLoad());
        this.templateOption = new TemplateRenderOption("template", dto.getTemplateRender());
    }

    public boolean isIgnoreFail() {
        return this.ignoreFail;
    }

    public boolean isParameterize() {
        return this.parameterize;
    }

    @Override
    public ParameterizeDto toDto() {
        return ParameterizeOption.toDto(this.toArgs(true));
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs();
        result.putAll(this.param.toCommandLineArgs());
        result.put("-parameterize", this.parameterize);
        result.put("-ignoreFail", this.ignoreFail);
        result.put("-cmd", this.cmd);
        result.put("-cmdParam", this.cmdParam);
        result.putFile("-template", this.template, true);
        result.putAll(this.templateOption.toCommandLineArgs());
        return result;
    }

    public Stream<Map<String, Object>> loadParams() {
        return this.getComparableDataSetLoader().loadParam(this.param.getParam().build());
    }

    public String[] createArgs(final Parameter aParam) {
        final String parameterList = this.isParameterize()
                ? this.templateOption.getTemplateRender()
                .render(this.getTemplateArgs(aParam.getMap()), aParam.getMap())
                : this.getTemplateArgs(aParam.getMap());
        final String[] result = parameterList.split("\\r?\\n");
        if (this.args.size() == 0) {
            return result;
        }
        final Map<String, String> mergeResult = Arrays.stream(result)
                .collect(Collectors.toMap(ParameterizeOption.ARGS_IGNORE_FILTER.extractKey(), it -> it));
        mergeResult.putAll(this.args.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey() + "=", entry -> entry.getKey() + "=" + entry.getValue())));
        return mergeResult.values().toArray(new String[0]);
    }

    public Command<?, ?> createCommand(final Parameter aParam) {
        return this.createCommand(this.templateOption.getTemplateRender().render(this.cmd, aParam.getMap()));
    }

    protected Command<?, ?> createCommand(final String cmdType) {
        return switch (cmdType) {
            case "compare" -> new Compare();
            case "convert" -> new Convert();
            case "generate" -> new Generate();
            case "run" -> new Run();
            case "parameterize" -> new Parameterize();
            default -> throw new IllegalArgumentException("no executable command : " + cmdType);
        };
    }

    protected String getTemplateArgs(final Map<String, Object> aParam) {
        File template = this.template;
        if (!Optional.ofNullable(this.cmdParam).orElse("").isEmpty()) {
            template = new File(this.templateOption.getTemplateRender().render(this.cmdParam, aParam));
        } else if (template == null) {
            return "";
        }
        return Files.read(template, this.templateOption.getTemplateEncoding());
    }

}
