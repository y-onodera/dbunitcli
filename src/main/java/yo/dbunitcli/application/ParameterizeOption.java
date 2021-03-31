package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ParameterizeOption extends CommandLineOption {

    @Option(name = "-param", usage = "directory or file extract data driven parameter at", required = true)
    private File param;

    @Option(name = "-paramType", usage = "table | sql | csv | csvq | xls | xlsx | fixed | reg | file | dir")
    private String paramType = "csv";

    @Option(name = "-includeMetaData", usage = "whether param include tableName and columns or not ")
    private String includeMetaData = "false";

    @Option(name = "-cmd", usage = "compare | convert :data driven target cmd", required = true)
    private String cmd;

    private String templateArgs;

    public ParameterizeOption() {
        super(Parameter.none());
    }

    public List<Map<String, Object>> loadParams() throws DataSetException {
        return this.getComparableDataSetLoader().loadParam(
                this.getDataSetParamBuilder()
                        .setSrc(this.param)
                        .setSource(DataSourceType.fromString(this.paramType))
                        .setMapIncludeMetaData(Boolean.parseBoolean(this.includeMetaData))
                        .build()
        );
    }

    public String[] createArgs(Parameter aParam) {
        aParam.getMap().put("rowNumber", aParam.getRowNumber());
        return this.getTemplateRender()
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

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        this.assertFileParameter(parser, this.paramType, this.param, "param");
    }

    @Override
    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        super.populateSettings(parser);
        try {
            this.templateArgs = this.loadTemplateString();
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }
}
