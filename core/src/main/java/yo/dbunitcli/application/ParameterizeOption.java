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
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ParameterizeOption extends CommandLineOption {

    private DataSetLoadOption param = new DataSetLoadOption("param");

    private TemplateRenderOption templateOption = new TemplateRenderOption("");

    @Option(name = "-cmd", usage = "data driven target cmd")
    private String cmd;

    @Option(name = "-cmdParam", usage = "parameterFile for cmd")
    private String cmdParam;

    public ParameterizeOption() {
        super(Parameter.none());
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.param.parseArgument(expandArgs);
        this.templateOption.parseArgument(expandArgs);
    }

    @Override
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putAll(this.param.expandOption(args));
        result.put("-cmd", this.cmd);
        result.put("-cmdParam", this.cmdParam);
        result.putAll(this.templateOption.expandOption(args));
        return result;
    }

    public List<Map<String, Object>> loadParams() throws DataSetException {
        return this.getComparableDataSetLoader().loadParam(this.param.getParam().build());
    }

    public String[] createArgs(Parameter aParam) throws IOException {
        aParam.getMap().put("rowNumber", aParam.getRowNumber());
        return this.templateOption.getTemplateRender()
                .render(getTemplateArgs(aParam.getMap()), aParam.getMap())
                .split("\\r?\\n");
    }

    public Command<?> createCommand(Map<String, Object> param) {
        return this.createCommand(this.templateOption.getTemplateRender().render(this.cmd, param));
    }

    protected Command<? extends CommandLineOption> createCommand(String cmdType) {
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

    protected String getTemplateArgs(Map<String, Object> param) throws IOException {
        File template = this.templateOption.getTemplate();
        if (!Strings.isNullOrEmpty(this.cmdParam)) {
            template = new File(this.templateOption.getTemplateRender().render(this.cmdParam, param));
        }
        return Files.read(template, this.templateOption.getTemplateEncoding());
    }

}
