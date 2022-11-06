package yo.dbunitcli.application;

import com.google.common.base.Strings;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.argument.DataSetLoadOption;
import yo.dbunitcli.application.argument.TemplateRenderOption;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.resource.Files;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ParameterizeOption extends CommandLineOption {

    private final DataSetLoadOption param = new DataSetLoadOption("param");

    private final TemplateRenderOption templateOption = new TemplateRenderOption("template");

    @Option(name = "-cmd", usage = "data driven target cmd")
    private String cmd;

    @Option(name = "-cmdParam", usage = "parameterFile for cmd")
    private String cmdParam;

    @Option(name = "-template", usage = "template file")
    private File template;

    public ParameterizeOption() {
        super(Parameter.none());
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

    public List<Map<String, Object>> loadParams() throws DataSetException {
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
        switch (cmdType) {
            case "compare":
                return new Compare();
            case "convert":
                return new Convert();
            case "generate":
                return new Generate();
            case "run":
                return new Run();
            default:
                throw new IllegalArgumentException("no executable command : " + cmdType);
        }
    }

    protected String getTemplateArgs(final Map<String, Object> param) {
        File template = this.template;
        if (!Strings.isNullOrEmpty(this.cmdParam)) {
            template = new File(this.templateOption.getTemplateRender().render(this.cmdParam, param));
        }
        return Files.read(template, this.templateOption.getTemplateEncoding());
    }

}
