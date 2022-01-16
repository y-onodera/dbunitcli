package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.component.DataSetLoadOption;
import yo.dbunitcli.application.component.TemplateRenderOption;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.resource.Files;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ParameterizeOption extends CommandLineOption {

    private DataSetLoadOption param = new DataSetLoadOption("param");

    private TemplateRenderOption templateOption = new TemplateRenderOption("");

    @Option(name = "-cmd", usage = "compare | convert :data driven target cmd", required = true)
    private String cmd;

    private String templateArgs;

    public ParameterizeOption() {
        super(Parameter.none());
    }

    @Override
    protected void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.param.parseArgument(expandArgs);
        this.templateOption.parseArgument(expandArgs);
        this.populateSettings(parser);
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

    public Command<?> createCommand() {
        switch (this.cmd) {
            case "compare":
                return new Compare();
            case "convert":
                return new Convert();
            case "generate":
                return new Generate();
            case "run":
                return new Run();
            default:
                throw new IllegalArgumentException("no executable command : " + this.cmd);
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
