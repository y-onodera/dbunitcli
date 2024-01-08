package yo.dbunitcli.application;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.argument.DataSetLoadOption;
import yo.dbunitcli.application.argument.TemplateRenderOption;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.resource.Files;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ParameterizeOption extends CommandLineOption {

    private final DataSetLoadOption param = new DataSetLoadOption("param");

    private final TemplateRenderOption templateOption = new TemplateRenderOption("template");

    @Option(name = "-cmd", usage = "data driven target cmd")
    private String cmd;

    @Option(name = "-cmdParam", usage = "columnName parameterFile for cmd. dynamically set fileName from param dataset")
    private String cmdParam;

    @Option(name = "-template", usage = "default template file. case when cmdParam exists,this option is ignore.")
    private File template;

    @Option(name = "-ignoreFail", usage = "data driven target cmd")
    private String ignoreFail = "false";

    public ParameterizeOption() {
        super(Parameter.none());
    }

    public boolean isIgnoreFail() {
        return Boolean.getBoolean(this.ignoreFail);
    }

    @Override
    public void setUpComponent(final CmdLineParser parser, final String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
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
        return this.templateOption.getTemplateRender()
                .render(this.getTemplateArgs(aParam.getMap()), aParam.getMap())
                .split("\\r?\\n");
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
