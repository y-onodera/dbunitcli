package yo.dbunitcli.application;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.cli.ArgumentFilter;
import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.cli.DefaultArgumentFilter;
import yo.dbunitcli.application.cli.DefaultArgumentMapper;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record ParameterizeOption(
        Parameter parameter
        , String cmd
        , String cmdParam
        , Map<String, String> args
        , boolean ignoreFail
        , boolean parameterize
        , ParameterUnit unit
        , String template
        , DataSetLoadOption paramData
        , TemplateRenderOption templateOption
) implements CommandLineOption<ParameterizeDto> {

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

    public static ParameterizeDto toDto(final String[] args) {
        final ParameterizeDto dto = new ParameterizeDto();
        new CommandLineParser("", CommandLineOption.DEFAULT_COMMANDLINE_MAPPER, ParameterizeOption.ARGS_IGNORE_FILTER)
                .parseArgument(args, dto);
        new CommandLineParser("param", ParameterizeOption.NONE_PARAM_MAPPER)
                .parseArgument(args, dto.getParamData());
        new CommandLineParser("template").parseArgument(args, dto.getTemplateOption());
        return dto;
    }

    public ParameterizeOption(final ParameterizeDto dto, final Parameter param) {
        this(param
                , dto.getCmd()
                , dto.getCmdParam()
                , new HashMap<>(dto.getArg())
                , Strings.isNotEmpty(dto.getIgnoreFail()) && Boolean.parseBoolean(dto.getIgnoreFail())
                , !Strings.isNotEmpty(dto.getParameterize()) || Boolean.parseBoolean(dto.getParameterize())
                , dto.getUnit() != null ? dto.getUnit() : ParameterUnit.record
                , dto.getTemplate()
                , new DataSetLoadOption("param", dto.getParamData(), true)
                , new TemplateRenderOption("template", dto.getTemplateOption())
        );
    }

    @Override
    public ParameterizeDto toDto() {
        return ParameterizeOption.toDto(this.toArgs(true));
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder()
                .addComponent("paramData", this.paramData.toCommandLineArgs())
                .put("-unit", this.unit, ParameterUnit.class)
                .put("-parameterize", this.parameterize)
                .put("-ignoreFail", this.ignoreFail)
                .put("-cmd", this.cmd)
                .put("-cmdParam", this.cmdParam)
                .putFile("-template", this.template, true, BaseDir.TEMPLATE)
                .addComponent("templateOption", this.templateOption.toCommandLineArgs());
    }

    public Stream<Parameter> loadParams() {
        return this.unit.loadStream(this.getComparableDataSetLoader(), this.paramData.getParam().build());
    }

    public String[] createArgs(final Parameter aParam) {
        final String parameterList = this.parameterize()
                ? this.templateOption.getTemplateRender()
                .render(this.getTemplateArgs(aParam.getMap()), aParam.getMap())
                : this.getTemplateArgs(aParam.getMap());
        final String[] result = parameterList.split("\\r?\\n");
        if (this.args.isEmpty()) {
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

    private Command<?, ?> createCommand(final String cmdType) {
        return switch (cmdType) {
            case "compare" -> new Compare();
            case "convert" -> new Convert();
            case "generate" -> new Generate();
            case "run" -> new Run();
            case "parameterize" -> new Parameterize();
            default -> throw new IllegalArgumentException("no executable command : " + cmdType);
        };
    }

    private String getTemplateArgs(final Map<String, Object> aParam) {
        final File template;
        if (Strings.isNotEmpty(this.cmdParam)) {
            template = FileResources.searchTemplate(this.templateOption.getTemplateRender().render(this.cmdParam, aParam));
        } else if (Strings.isEmpty(this.template)) {
            return "";
        } else {
            template = FileResources.searchTemplate(this.template);
        }
        return FileResources.read(Objects.requireNonNull(template), this.templateOption.encoding());
    }

}
