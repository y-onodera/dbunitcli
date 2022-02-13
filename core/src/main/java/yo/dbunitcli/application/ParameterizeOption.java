package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.argument.DataSetLoadOption;
import yo.dbunitcli.application.argument.TemplateRenderOption;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.resource.Files;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParameterizeOption extends CommandLineOption {

    private DataSetLoadOption param = new DataSetLoadOption("param");

    private TemplateRenderOption templateOption = new TemplateRenderOption("");

    @Option(name = "-cmd", usage = "data driven target cmd")
    private String cmd;

    private String templateArgs;

    public ParameterizeOption() {
        super(Parameter.none());
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.param.parseArgument(expandArgs);
        this.templateOption.parseArgument(expandArgs);
        this.populateSettings(parser);
    }

    @Override
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-cmd", this.cmd);
        result.putAll(this.param.expandOption(args));
        result.putAll(this.templateOption.expandOption(args));
        result.putAll(super.expandOption(args));
        return result;
    }

    public List<Map<String, Object>> loadParams() throws DataSetException {
        return this.getComparableDataSetLoader().loadParam(this.param.getParam().build());
    }

    public String[] createArgs(Parameter aParam) {
        aParam.getMap().put("rowNumber", aParam.getRowNumber());
        return this.templateOption.getTemplateRender()
                .render(this.templateArgs, aParam.getMap())
                .split("\\r?\\n");
    }

    public Command<?> createCommand(Map<String, Object> param) {
        String cmdType = Optional.of(this.cmd)
                .orElseGet(() -> param.get("-cmd").toString());
        return this.createCommand(cmdType);
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

    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        try {
            this.templateArgs = Files.read(this.templateOption.getTemplate(), this.templateOption.getTemplateEncoding());
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }
}
