package yo.dbunitcli.application.command;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.*;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.common.Parameter;
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
        , String ignoreFail
        , String parallel
        , String parameterize
        , ParameterUnit unit
        , String template
        , DataSetLoadOption paramData
        , TemplateRenderOption templateOption
) implements CommandLineOption<ParameterizeDto> {

    private static final ArgumentFunction NONE_PARAM_MAPPER = new ArgumentFunction() {
        @Override
        public String[] apply(final String[] arguments, final String prefix) {
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
            return !it.startsWith("-" + prefix + ".src=") && !it.startsWith("-" + prefix + ".srcType=")
                    && !it.startsWith("-src=") && !it.startsWith("-srcType=");
        }

        private boolean paramTypeIsNone(final List<String> list) {
            return list.contains("-srcType=none") || list.contains("-param.srcType=none")
                    || list.stream().noneMatch(it -> it.startsWith("-srcType=") || it.startsWith("-param.srcType="));
        }
    };
    private static final ArgumentFilter ARGS_IGNORE_FILTER = new DefaultArgumentFilter("-P", "-A", "-arg");

    // 行ごとに実行できるコマンド（scaffoldは対象外）
    private static final List<String> COMMANDS = Arrays.stream(Type.values())
                                                       .filter(it -> it != Type.scaffold)
                                                       .map(Enum::name)
                                                       .toList();

    public static ParameterizeDto toDto(final String[] args) {
        final ParameterizeDto dto = new ParameterizeDto();
        new ArgumentMapper("", CommandLineOption.ARGUMENT_FUNCTION, ParameterizeOption.ARGS_IGNORE_FILTER)
                .populate(args, dto);
        new ArgumentMapper("param", ParameterizeOption.NONE_PARAM_MAPPER)
                .populate(args, dto.getParamData());
        new ArgumentMapper("template").populate(args, dto.getTemplateOption());
        return dto;
    }

    public ParameterizeOption(final ParameterizeDto dto, final Parameter param) {
        this(param
                , dto.getCmd()
                , dto.getCmdParam()
                , new HashMap<>(dto.getArg())
                , Strings.isNotEmpty(dto.getIgnoreFail()) ? dto.getIgnoreFail() : "false"
                , DataSetConverterOption.parallel(dto.getParallel())
                , Strings.isNotEmpty(dto.getParameterize()) ? dto.getParameterize() : "true"
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
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder()
                .addComponent("paramData", this.paramData.toParameters())
                .put("-unit", this.unit, ParameterUnit.class)
                .putFlag("-parameterize", this.parameterize)
                .putFlag("-ignoreFail", this.ignoreFail)
                .put("-parallel", this.parallel)
                .put("-cmd", this.cmd)
                .put("-cmdParam", this.cmdParam)
                .putFile("-template", this.template, BaseDir.PARAMETERIZE_TEMPLATE)
                .addComponent("templateOption", this.templateOption.toParameters());
    }

    /**
     * cmdParamは行ごとに展開するテンプレートのため存在確認しない。cmdParamがあるとtemplateは使われないため同時に指定させない
     */
    @Override
    public List<Violation> validate() {
        return Option.concat(this.validateRequired("-cmd", this.cmd)
                , this.isTemplate(this.cmd) ? List.of() : this.validateOneOf("-cmd", this.cmd, COMMANDS)
                , this.validateExclusive("-cmdParam", this.cmdParam, "-template", this.template)
                , this.validateFileExists("-template", this.template, BaseDir.PARAMETERIZE_TEMPLATE)
                , this.validateBoolean("-ignoreFail", this.ignoreFail)
                , this.validateBoolean("-parameterize", this.parameterize)
                , this.validateParallel(this.parallel)
                , this.paramData.validate()
                , this.templateOption.validate());
    }

    // 行ごとにテンプレートで展開する値は、展開後でないと検証できない
    private boolean isTemplate(final String value) {
        return Strings.isNotEmpty(value) && value.contains(this.templateOption.templateVarStart());
    }

    /**
     * @return 並列数。未指定なら1（行ごとのコマンドは実行順に依存しやすいため、runと同じく既定は逐次）
     */
    public int parallelism() {
        return DataSetConverterOption.parallelism(this.parallel, 1);
    }

    public Stream<Parameter> loadParams() {
        return this.unit.dataSetToStream(this.getComparableDataSetLoader(), this.paramData.getParam().build());
    }

    public String[] createArgs(final Parameter aParam) {
        final String parameterList = Boolean.parseBoolean(this.parameterize())
                ? this.templateOption.getTemplateRender()
                .render(this.getTemplateArgs(aParam), aParam)
                : this.getTemplateArgs(aParam);
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
        return this.createCommand(this.templateOption.getTemplateRender().render(this.cmd, aParam));
    }

    private Command<?, ?> createCommand(final String cmdType) {
        if (!COMMANDS.contains(cmdType)) {
            throw new IllegalArgumentException("no executable command : " + cmdType);
        }
        return Type.valueOf(cmdType).getCommand();
    }

    private String getTemplateArgs(final Parameter aParam) {
        final File template;
        if (Strings.isNotEmpty(this.cmdParam)) {
            template = FileResources.searchTemplate(this.templateOption.getTemplateRender().render(this.cmdParam, aParam));
        } else if (Strings.isEmpty(this.template)) {
            return "";
        } else {
            template = FileResources.searchParameterizeTemplate(this.template);
        }
        if(template == null) {
            return "";
        }
        return FileResources.read(template, this.templateOption.encoding());
    }

}
