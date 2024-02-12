package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.argument.*;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.resource.Files;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "parameterizeExecute", mixinStandardHelpOptions = true)
public class ParameterizeOption extends CommandLineOption {

    public static final DefaultArgumentMapper NONE_PARAM_MAPPER = new DefaultArgumentMapper() {
        @Override
        public String[] map(final String[] arguments, final String prefix, final CommandLine cmdLine) {
            final List<String> list = Arrays.asList(arguments);
            if (list.contains("-srcType=none") || list.stream().noneMatch(it -> it.startsWith("-srcType="))) {
                final List<String> newArg = Arrays.stream(arguments)
                        .filter(it -> !it.contains("-src=") || !it.contains("-srcType="))
                        .collect(Collectors.toList());
                newArg.add("-srcType=none");
                newArg.add("-src=.");
                return newArg.toArray(new String[0]);
            }
            return arguments;
        }
    };

    private final DataSetLoadOption param = new DataSetLoadOption("param");
    private final TemplateRenderOption templateOption = new TemplateRenderOption("template");
    @CommandLine.Option(names = "-ignoreFail", description = "case when cmd is compare and unexpected diff found, then continue other cmd")
    private String ignoreFail = "false";
    @CommandLine.Option(names = "-cmd", description = "data driven target cmd")
    private String cmd;
    @CommandLine.Option(names = "-cmdParam", description = "columnName parameterFile for cmd. dynamically set fileName from param dataset")
    private String cmdParam;
    @CommandLine.Option(names = "-cmdArg")
    private Map<String, String> cmdArgs = new HashMap<>();
    @CommandLine.Option(names = "-template", description = "default template file. case when cmdParam exists,this option is ignore.")
    private File template;

    public ParameterizeOption() {
        super(Parameter.none());
    }

    public boolean isIgnoreFail() {
        return Boolean.getBoolean(this.ignoreFail);
    }

    @Override
    public ArgumentFilter getArgumentFilter() {
        return new DefaultArgumentFilter("-P", "-cmdArg");
    }

    @Override
    public void setUpComponent(final CommandLine.ParseResult parser, final String[] expandArgs) {
        super.setUpComponent(parser, expandArgs);
        this.param.setArgumentMapper(ParameterizeOption.NONE_PARAM_MAPPER);
        this.param.parseArgument(expandArgs);
        this.templateOption.parseArgument(expandArgs);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putAll(this.param.createOptionParam(args));
        result.put("-cmd", this.cmd);
        result.put("-cmdParam", this.cmdParam);
        result.putFile("-template", this.template, true);
        result.putAll(this.templateOption.createOptionParam(args));
        return result;
    }

    public Stream<Map<String, Object>> loadParams() {
        return this.getComparableDataSetLoader().loadParam(this.param.getParam().build());
    }

    public String[] createArgs(final Parameter aParam) {
        final String[] result = this.templateOption.getTemplateRender()
                .render(this.getTemplateArgs(aParam.getMap()), aParam.getMap())
                .split("\\r?\\n");
        if (this.cmdArgs.size() == 0) {
            return result;
        }
        final Map<String, String> mergeResult = Arrays.stream(result)
                .collect(Collectors.toMap(this.getArgumentFilter().extractKey(), it -> it));
        mergeResult.putAll(this.cmdArgs.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey() + "=", entry -> entry.getKey() + "=" + entry.getValue())));
        return mergeResult.values().toArray(new String[0]);
    }

    public Command<?> createCommand(final Map<String, Object> param) {
        return this.createCommand(this.templateOption.getTemplateRender().render(this.cmd, param));
    }

    protected Command<? extends CommandLineOption> createCommand(final String cmdType) {
        return switch (cmdType) {
            case "compare" -> new Compare();
            case "convert" -> new Convert();
            case "generate" -> new Generate();
            case "run" -> new Run();
            default -> throw new IllegalArgumentException("no executable command : " + cmdType);
        };
    }

    protected String getTemplateArgs(final Map<String, Object> param) {
        File template = this.template;
        if (!Optional.ofNullable(this.cmdParam).orElse("").isEmpty()) {
            template = new File(this.templateOption.getTemplateRender().render(this.cmdParam, param));
        }
        return Files.read(template, this.templateOption.getTemplateEncoding());
    }

}
